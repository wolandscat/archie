package com.nedap.archie.adl14;

import org.openehr.utils.message.MessageCode;

public enum ADL14ConversionMessageCode implements MessageCode {
    WARNING_SPECIALIZED_FIRST_MATCHING_CHILD("Found two matching children. Only picked one");

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
