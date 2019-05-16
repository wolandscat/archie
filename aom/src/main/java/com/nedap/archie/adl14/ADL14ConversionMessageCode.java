package com.nedap.archie.adl14;

import org.openehr.utils.message.MessageCode;

public enum ADL14ConversionMessageCode implements MessageCode {
    WARNING_SPECIALIZED_FIRST_MATCHING_CHILD("Found two matching children. Only picked one"),
    WARNING_UNKNOWN_CODE_TYPE_IN_TERMBINDING("Termbindings contains at code {0}, but this is unused in the archetype. This cannot be automatically converted"),
    INFO_PREVIOUSLY_CONVERTED_CODE_DELETED("A previously converted path no longer exists. This usually means the source ADL 1.4 has been edited, in that case there's no problem.");

    private String template;


    ADL14ConversionMessageCode(String template) {
        this.template = template;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessageTemplate() {
        return template;
    }
}
