package com.nedap.archie.adl14.terms;

/**
 * Conversion template for a terminology into a URI. TemplateString must be a string possibly containing the following variables:
 *
 * $terminology_id
 * $code_string
 * $terminology_version
 *
 * They will be replaced with the correct values
 */
public class TerminologyUriTemplate {

    public final static String TERMINOLOGY_ID_TEMPLATE_STRING = "$terminology_id";
    public final static String CODE_STRING_TEMPLATE_STRING = "$code_string";
    public final static String TERMINOLOGY_VERSION_TEMPLATE_STRING = "$terminology_version";

    private String terminologyId;
    private String terminologyVersion;
    private String template;

    public String getTerminologyId() {
        return terminologyId;
    }

    public void setTerminologyId(String terminologyId) {
        this.terminologyId = terminologyId;
    }

    public String getTerminologyVersion() {
        return terminologyVersion;
    }

    public void setTerminologyVersion(String terminologyVersion) {
        this.terminologyVersion = terminologyVersion;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
