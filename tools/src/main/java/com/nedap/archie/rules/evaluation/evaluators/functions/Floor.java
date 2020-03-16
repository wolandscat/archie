package com.nedap.archie.rules.evaluation.evaluators.functions;

import com.nedap.archie.rules.PrimitiveType;
import com.nedap.archie.rules.evaluation.FunctionCallException;
import com.nedap.archie.rules.evaluation.FunctionImplementation;
import com.nedap.archie.rules.evaluation.Value;
import com.nedap.archie.rules.evaluation.ValueList;

import java.util.List;

import static com.nedap.archie.rules.evaluation.evaluators.FunctionUtil.checkAndHandleNull;

public class Floor implements FunctionImplementation {
    @Override
    public String getName() {
        return "floor";
    }

    @Override
    public ValueList evaluate(List<ValueList> arguments) throws FunctionCallException {
        if(arguments.size() != 1) {
            throw new FunctionCallException("floor expects one argument, but got " + arguments.size());
        }

        //if one of the values is null, return null.
        ValueList possiblyNullResult = checkAndHandleNull(arguments);
        if(possiblyNullResult != null) {
            possiblyNullResult.setType(PrimitiveType.Real);
            return possiblyNullResult;
        }

        ValueList result = new ValueList();
        result.setType(PrimitiveType.Integer);

        for(Value valueObject : arguments.get(0).getValues()) {
            // QUESTION: What should the type be? Int/Double/Long/Real?
            result.addValue(new Value((int) Math.floor((Double) valueObject.getValue())));
        }

        return result;
    }
}
