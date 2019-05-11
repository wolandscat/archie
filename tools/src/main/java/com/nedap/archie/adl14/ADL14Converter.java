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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ADL14Converter {

    private final MetaModels metaModels;
    private final ADL14ConversionConfiguration conversionConfiguration;

    public ADL14Converter(MetaModels metaModels, ADL14ConversionConfiguration conversionConfiguration) {
        this.metaModels = metaModels;
        this.conversionConfiguration = conversionConfiguration;
    }


    public ADL2ConversionResultList convert(List<Archetype> archetypes) {
        return convert(archetypes, null);
    }

    public ADL2ConversionResultList convert(List<Archetype> archetypes, ADL2ConversionRunLog previousConversion) {
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
                    if(parent == null) {
                        throw new RuntimeException(MessageFormat.format("Cannot find parent {0} for archetype {1}", archetype.getParentArchetypeId(), archetype.getArchetypeId()));
                    }
                    Archetype flatParent = new Flattener(repository, metaModels).flatten(parent);
                    result = convert(archetype, flatParent, previousConversion);
                    if (result.getArchetype() != null) {
                        result.setArchetype(differentiator.differentiate(result.getArchetype(), flatParent));
                    }
                    resultList.addConversionResult(result);
                } else {
                    result = convert(archetype, previousConversion);
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


    private ADL2ConversionResult convert(Archetype archetype,  ADL2ConversionRunLog previousConversion) {
        return convert(archetype, null, previousConversion);
    }

    private ADL2ConversionResult convert(Archetype archetype, Archetype flatParent, ADL2ConversionRunLog previousConversion) {
        ADL2ConversionLog previousLog = previousConversion == null ? null : previousConversion.getConversionLog(archetype.getArchetypeId().getSemanticId());
        Archetype convertedArchetype = archetype.clone();
        new ADL14DescriptionConverter().convert(convertedArchetype);
        ADL2ConversionResult result = new ADL2ConversionResult(convertedArchetype);
        ADL14NodeIDConverter adl14NodeIDConverter = new ADL14NodeIDConverter(this.metaModels, convertedArchetype, flatParent, conversionConfiguration, previousLog, result);
        ADL2ConversionLog conversionLog = adl14NodeIDConverter.convert();//fixes archetype in place
        result.setConversionLog(conversionLog);
        setCorrectVersions(convertedArchetype);
        //set some values that are not directly in ODIN or ADL
        ArchetypeParsePostProcesser.fixArchetype(convertedArchetype);
        return result;

    }



    private void setCorrectVersions(Archetype result) {
        result.setAdlVersion("2.0.6");
        result.setRmRelease("1.0.4");
    }
}
