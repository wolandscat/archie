package com.nedap.archie.rules.evaluation.evaluators.functions;

import com.nedap.archie.rules.PrimitiveType;
import com.nedap.archie.rules.evaluation.FunctionCallException;
import com.nedap.archie.rules.evaluation.FunctionImplementation;
import com.nedap.archie.rules.evaluation.ValueList;
import com.nedap.archie.rules.evaluation.Value;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import static com.nedap.archie.rules.evaluation.evaluators.FunctionUtil.checkAndHandleNull;
import static com.nedap.archie.rules.evaluation.evaluators.FunctionUtil.checkEqualLength;
import static com.nedap.archie.rules.evaluation.evaluators.FunctionUtil.castToDouble;

/**
 * Created by pieter.bos on 07/04/2017.
 */
public class Sum implements FunctionImplementation {
    @Override
    public String getName() {
        return "sum";
    }

    @Override
    public ValueList evaluate(List<ValueList> arguments) throws FunctionCallException {
        //if one of the values is null, return null.
        ValueList possiblyNullResult = checkAndHandleNull(arguments);
        if(possiblyNullResult != null) {
            possiblyNullResult.setType(PrimitiveType.Real);
            return possiblyNullResult;
        }

        Double sum = 0.0;
        ValueList result = new ValueList();
        List<String> paths = new ArrayList<>();

        for (ValueList argument: arguments) {
            List<Value> values = argument.getValues();
            for (Value val: values) {
                if (!val.isNull()) {
                    sum += castToDouble(val);
                }
                paths.addAll(val.getPaths());
            }
        }
        result.addValue(sum, paths);
        result.setType(PrimitiveType.Real);
        return result;
    }
}