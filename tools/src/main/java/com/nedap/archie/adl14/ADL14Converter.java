package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.utils.ArchetypeParsePostProcesser;

import java.util.List;

public class ADL14Converter {

    public ADL2ConversionResult convert(Archetype archetype, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionLog previousConversion) {
        Archetype result = archetype.clone();
        new ADL14DescriptionConverter().convert(result);

        ADL14NodeIDConverter adl14NodeIDConverter = new ADL14NodeIDConverter(result, conversionConfiguration, previousConversion);
        ADL2ConversionLog convert = adl14NodeIDConverter.convert();//fixes archetype in place

        setCorrectVersions(result);
        //set some values that are not directly in ODIN or ADL
        ArchetypeParsePostProcesser.fixArchetype(result);

        return new ADL2ConversionResult(result, convert);
    }

    private void setCorrectVersions(Archetype result) {
        result.setAdlVersion("2.0.6");
        result.setRmRelease("1.0.4");
    }
}
