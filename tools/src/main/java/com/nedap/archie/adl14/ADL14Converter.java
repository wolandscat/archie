package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
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

    public ADL2ConversionResult convert(Archetype archetype, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionLog previousConversion) {
        Archetype result = archetype.clone();
        new ADL14DescriptionConverter().convert(result);

        ADL14NodeIDConverter adl14NodeIDConverter = new ADL14NodeIDConverter(result, null, conversionConfiguration, previousConversion);
        ADL2ConversionLog convert = adl14NodeIDConverter.convert();//fixes archetype in place

        setCorrectVersions(result);
        //set some values that are not directly in ODIN or ADL
        ArchetypeParsePostProcesser.fixArchetype(result);

        return new ADL2ConversionResult(result, convert);
    }

    public ADL2ConversionResult convert(Archetype archetype, Archetype flatParent, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionLog previousConversion) {
        Archetype result = archetype.clone();
        new ADL14DescriptionConverter().convert(result);

        ADL14NodeIDConverter adl14NodeIDConverter = new ADL14NodeIDConverter(result, flatParent, conversionConfiguration, previousConversion);
        ADL2ConversionLog convert = adl14NodeIDConverter.convert();//fixes archetype in place

        setCorrectVersions(result);
        //set some values that are not directly in ODIN or ADL
        ArchetypeParsePostProcesser.fixArchetype(result);

        return new ADL2ConversionResult(result, convert);
    }


    public List<ADL2ConversionResult> convert(MetaModels metaModels, List<Archetype> archetypes, ADL14ConversionConfiguration conversionConfiguration) {
        InMemoryFullArchetypeRepository repository = new InMemoryFullArchetypeRepository();
        Map<String, ADL2ConversionResult> convertedArchetypes = new LinkedHashMap<>();
        List<Archetype> unprocessed = new ArrayList<>(archetypes);

        //process the archetypes ordered by specialization level
        //TODO: this is very ugly! Just sort the list on specialization level,
        //then process the list in that order
        unprocessed.sort(Comparator.comparingInt(a -> a.specializationDepth()));

        Differentiator differentiator = new Differentiator(metaModels);
        for(Archetype archetype:unprocessed) {

            //convert!
            ADL2ConversionResult result = null;
            if (archetype.getParentArchetypeId() != null) {
                try {
                    Archetype parent = repository.getArchetype(archetype.getParentArchetypeId());
                    Archetype flatParent = new Flattener(repository, metaModels).flatten(parent);
                    result = convert(archetype, flatParent, conversionConfiguration, null);
                    if (result.getArchetype() != null) {
                        result.setArchetype(differentiator.differentiate(result.getArchetype(), flatParent));
                    }
                } catch (Exception e) {
                    //TODO: add exception to result
                }
            } else {
                result = convert(archetype, conversionConfiguration, null);
            }
            if(result != null && result.getArchetype() != null && result.getArchetype().getArchetypeId() != null) {
                convertedArchetypes.put(result.getArchetype().getArchetypeId().getSemanticId(), result);

                repository.addArchetype(result.getArchetype());
            }

        }



        return new ArrayList<>(convertedArchetypes.values());
    }

    private void setCorrectVersions(Archetype result) {
        result.setAdlVersion("2.0.6");
        result.setRmRelease("1.0.4");
    }
}
