package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.adl14.log.ADL2ConversionRunLog;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.ArchetypeHRID;
import com.nedap.archie.aom.utils.ArchetypeParsePostProcesser;
import com.nedap.archie.diff.Differentiator;
import com.nedap.archie.flattener.Flattener;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;
import com.nedap.archie.rminfo.MetaModels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ADL14Converter {

    /*
     * TODO: this should be a private method
     */
    public ADL2ConversionResult convert(Archetype archetype, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionRunLog previousConversion) {
        return convert(archetype, null, conversionConfiguration, previousConversion);
    }

    /*
     * TODO: this should be a private method
     */
    public ADL2ConversionResult convert(Archetype archetype, Archetype flatParent, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionRunLog previousConversion) {
        ADL2ConversionLog previousLog = previousConversion == null ? null : previousConversion.getConversionLog(archetype.getArchetypeId().getSemanticId());
        Archetype result = archetype.clone();
        new ADL14DescriptionConverter().convert(result);

        ADL14NodeIDConverter adl14NodeIDConverter = new ADL14NodeIDConverter(result, flatParent, conversionConfiguration, previousLog);
        ADL2ConversionLog convert = adl14NodeIDConverter.convert();//fixes archetype in place

        setCorrectVersions(result);
        //set some values that are not directly in ODIN or ADL
        ArchetypeParsePostProcesser.fixArchetype(result);

        return new ADL2ConversionResult(result, convert);
    }


    public ADL2ConversionResultList convert(MetaModels metaModels,
                                              List<Archetype> archetypes,
                                              ADL14ConversionConfiguration conversionConfiguration,
                                              ADL2ConversionRunLog previousConversion) {
        ADL2ConversionResultList resultList = new ADL2ConversionResultList();

        InMemoryFullArchetypeRepository repository = new InMemoryFullArchetypeRepository();
        List<Archetype> unprocessed = new ArrayList<>(archetypes);

        //process the archetypes ordered by specialization level
        unprocessed.sort(Comparator.comparingInt(a -> a.specializationDepth()));

        Differentiator differentiator = new Differentiator(metaModels);
        for(Archetype archetype:unprocessed) {

            ADL2ConversionResult result = null;
            try {
                if (archetype.getParentArchetypeId() != null) {

                        Archetype parent = repository.getArchetype(archetype.getParentArchetypeId());
                        Archetype flatParent = new Flattener(repository, metaModels).flatten(parent);
                        result = convert(archetype, flatParent, conversionConfiguration, previousConversion);
                        if (result.getArchetype() != null) {
                            result.setArchetype(differentiator.differentiate(result.getArchetype(), flatParent));
                        }
                        resultList.addConversionResult(result);
                } else {
                    result = convert(archetype, conversionConfiguration, previousConversion);
                    resultList.addConversionResult(result);
                }
                if(result != null && result.getArchetype() != null && result.getArchetype().getArchetypeId() != null) {
                    repository.addArchetype(result.getArchetype());
                }
            } catch (Exception e) {
                result = new ADL2ConversionResult(archetype.getArchetypeId().toString(), e);
                resultList.addConversionResult(result);
            }
        }


        return resultList;
    }

    private void setCorrectVersions(Archetype result) {
        result.setAdlVersion("2.0.6");
        result.setRmRelease("1.0.4");
    }
}
