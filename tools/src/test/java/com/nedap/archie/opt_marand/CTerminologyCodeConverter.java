package com.nedap.archie.opt_marand;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.google.common.collect.Lists;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.rm.support.identification.TerminologyId;

public class CTerminologyCodeConverter implements Converter<TemplateCTerminologyCode, CTerminologyCode> {
    @Override
    public CTerminologyCode convert(TemplateCTerminologyCode value) {
        if(value.getTerminologyId() != null && value.getIncludedExternalTerminologyCodes() != null) {
            //convert external term codes to the non-parsed format. The converter will handle that
            String termCode = "[" + value.getTerminologyId().getValue() + "::";
            //assuming only one terminology id for now - might not be correct!
            boolean first = true;
            for(TemplateTermCode templateTermCode:value.getIncludedExternalTerminologyCodes()) {
                if(first) {
                    first = false;
                } else {
                    termCode = termCode + ", ";
                }
                termCode = termCode + templateTermCode.getCode();

            }
            termCode = termCode + "]";
            CTerminologyCode result = new CTerminologyCode();

            result.setConstraint(Lists.newArrayList(termCode));
            result.setAssumedValue(value.getAssumedValue());
            return result;
            //TODO: check if it's possible that this is just a term binding to a terminology id?
        }
        CTerminologyCode result = new CTerminologyCode();

        result.setConstraint(value.getConstraint());
        result.setAssumedValue(value.getAssumedValue());
        return result;

    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(TemplateCTerminologyCode.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(CTerminologyCode.class);
    }
}
