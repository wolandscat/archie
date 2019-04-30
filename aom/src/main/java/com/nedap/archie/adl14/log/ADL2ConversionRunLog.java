package com.nedap.archie.adl14.log;

import com.nedap.archie.aom.ArchetypeHRID;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A log file of one ADL 1.4 to ADL 2 conversion log. This log file can be used to create repeatable conversions
 * between ADL 1.4 and ADL 2, even when the ADL 1.4 files are edited between conversion. Be sure to store this
 * log with the conversion of your archetypes to enable proper conversion
 */
public class ADL2ConversionRunLog {

    private Map<String, ADL2ConversionLog> convertedArchetypes = new LinkedHashMap<>();

    public Map<String, ADL2ConversionLog> getConvertedArchetypes() {
        return convertedArchetypes;
    }

    public void setConvertedArchetypes(Map<String, ADL2ConversionLog> convertedArchetypes) {
        this.convertedArchetypes = convertedArchetypes;
    }

    public ADL2ConversionLog getConversionLog(String archetypeId) {
        return convertedArchetypes.get(archetypeId);
    }

    public void addLog(String archetypeId, ADL2ConversionLog conversionLog) {
        convertedArchetypes.put(new ArchetypeHRID(archetypeId).getSemanticId(), conversionLog);
    }
}
