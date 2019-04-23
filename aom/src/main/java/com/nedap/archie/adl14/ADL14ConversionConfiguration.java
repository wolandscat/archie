package com.nedap.archie.adl14;

import com.nedap.archie.adl14.terms.TerminologyUriTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ADL14ConversionConfiguration {

    private List<TerminologyUriTemplate> terminologyConversionTemplates = new ArrayList<>();

    public List<TerminologyUriTemplate> getTerminologyConversionTemplates() {
        return terminologyConversionTemplates;
    }

    public void setTerminologyConversionTemplates(List<TerminologyUriTemplate> terminologyConversionTemplates) {
        this.terminologyConversionTemplates = terminologyConversionTemplates;
    }

    public TerminologyUriTemplate getTerminologyUriTemplate(String terminologyId, String version) {
        Optional<TerminologyUriTemplate> result = terminologyConversionTemplates.stream().filter(template ->
                template.getTerminologyId().equalsIgnoreCase(terminologyId) &&
                        (version == null || version.equalsIgnoreCase(template.getTerminologyVersion()))).findFirst();
        if(result.isPresent() || version == null) {
            return result.orElse(null);
        }
        return terminologyConversionTemplates.stream().filter(template ->
                template.getTerminologyId().equalsIgnoreCase(terminologyId) &&
                        (template.getTerminologyVersion() == null)).findFirst().orElse(null);
    }
}
