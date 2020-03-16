package com.nedap.archie.rules.evaluation;

import com.nedap.archie.rules.PrimitiveType;
import com.nedap.archie.rules.evaluation.evaluators.FunctionUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FunctionUtilTest {

    @Test
    public void checkEqualLengthOrOne() {
        List<ValueList> arguments = new ArrayList<>();
        ValueList valueList1 = new ValueList(PrimitiveType.Real);
        Value value = new Value<>(1d);
        valueList1.addValue(value);
        valueList1.addValue(value);
        ValueList valueList2 = new ValueList(PrimitiveType.Real);
        valueList2.addValue(value);
        valueList2.addValue(value);
        valueList2.addValue(value);
        arguments.add(valueList1);
        arguments.add(valueList2);

        int result = FunctionUtil.checkEqualLengthOrOne(arguments);
        assertEquals(-1, result);
    }
}
