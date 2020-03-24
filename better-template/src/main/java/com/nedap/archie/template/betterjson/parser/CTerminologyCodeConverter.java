package com.nedap.archie.template.betterjson.parser;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.google.common.collect.Lists;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.rm.support.identification.TerminologyId;

import java.util.ArrayList;
import java.util.List;

public class CTerminologyCodeConverter implements Converter<TemplateCTerminologyCode, CTerminologyCode> {
    @Override
    public CTerminologyCode convert(TemplateCTerminologyCode value) {
        if(value.getTerminologyId() != null && value.getIncludedExternalTerminologyCodes() != null) {
            //convert external term codes to the non-parsed format. The converter will handle that
            String termCode = "[" + value.getTerminologyId().getValue() + "::";
            //assuming only one terminology id for now - might not be correct!
            List<String> constraints = new ArrayList<>();
            boolean first = true;
            for(TemplateTermCode templateTermCode:value.getIncludedExternalTerminologyCodes()) {
                if(first) {
                    constraints.add("[" + value.getTerminologyId().getValue() + "::" + templateTermCode.getCode() + "]");
                    first = false;
                } else {
                    constraints.add( templateTermCode.getCode());
                }
            }
            CTerminologyCode result = new CTerminologyCode();

            result.setConstraint(constraints);
            result.setAssumedValue(value.getAssumedValue());
            return result;
            //TODO: check if it's possible that this is just a term binding to a terminology id?
        } else if (value.getTerminologyId() != null && !value.getTerminologyId().getValue().equalsIgnoreCase("local") && value.getConstraint() != null && !value.getConstraint().isEmpty()) {
            List<String> constraints = new ArrayList<>();
            constraints.addAll(value.getConstraint());
            String first = "[" + value.getTerminologyId() + "::" + constraints.get(0) + "]";
            constraints.set(0, first);
            CTerminologyCode result = new CTerminologyCode();
            result.setConstraint(constraints);
            result.setAssumedValue(value.getAssumedValue());
            return result;
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
