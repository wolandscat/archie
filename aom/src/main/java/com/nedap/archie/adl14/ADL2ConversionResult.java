package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.aom.Archetype;

public class ADL2ConversionResult {

    private Archetype archetype;
    private ADL2ConversionLog conversionLog;

    public ADL2ConversionResult(Archetype archetype, ADL2ConversionLog conversionLog) {
        this.archetype = archetype;
        this.conversionLog = conversionLog;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public ADL2ConversionLog getConversionLog() {
        return conversionLog;
    }

}
