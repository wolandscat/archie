package org.openehr.utils.error;

import org.openehr.utils.message.MessageCode;

public enum TestErrorCode implements MessageCode {

    ErrorKey, code, code0, code1, code2, code3, code4;


    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessageTemplate() {
        return "Error is {0}";
    }
}
