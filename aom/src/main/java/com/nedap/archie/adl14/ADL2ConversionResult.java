package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.aom.Archetype;

/**
 * ADL 2 conversion result. Always has the archetypeId field set.
 * Either has archetype and conversionLog non-null in case of a succesful conversion, or
 * exception non-null in case of an unexpected Exception
 */
public class ADL2ConversionResult {

    private String archetypeId;
    private Archetype archetype;
    private ADL2ConversionLog conversionLog;
    private Exception exception;

    public ADL2ConversionResult(Archetype archetype, ADL2ConversionLog conversionLog) {
        this.archetypeId = archetype.getArchetypeId().getFullId();
        this.archetype = archetype;
        this.conversionLog = conversionLog;
    }

    public ADL2ConversionResult(String archetypeId, Exception exception) {
        this.archetypeId = archetypeId;
        this.exception = exception;
    }

    public String getArchetypeId() {
        return archetypeId;
    }

    public void setArchetypeId(String archetypeId) {
        this.archetypeId = archetypeId;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public ADL2ConversionLog getConversionLog() {
        return conversionLog;
    }

    public void setArchetype(Archetype archetype) {
        this.archetype = archetype;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
