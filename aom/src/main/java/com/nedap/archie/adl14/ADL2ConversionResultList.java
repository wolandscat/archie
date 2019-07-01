package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionRunLog;

import java.util.ArrayList;
import java.util.List;

public class ADL2ConversionResultList {

    //TODO: should this contain an archetype repository as well?
    private List<ADL2ConversionResult> conversionResults = new ArrayList<>();
    private ADL2ConversionRunLog conversionLog = new ADL2ConversionRunLog();

    public List<ADL2ConversionResult> getConversionResults() {
        return conversionResults;
    }

    public void setConversionResults(List<ADL2ConversionResult> conversionResults) {
        this.conversionResults = conversionResults;
    }

    public ADL2ConversionRunLog getConversionLog() {
        return conversionLog;
    }

    public void setConversionLog(ADL2ConversionRunLog conversionLog) {
        this.conversionLog = conversionLog;
    }

    public void addConversionResult(ADL2ConversionResult result) {
        conversionResults.add(result);
        if(result.getArchetype() != null) {
            conversionLog.addLog(result.getArchetypeId(), result.getConversionLog());
        }
    }
}
