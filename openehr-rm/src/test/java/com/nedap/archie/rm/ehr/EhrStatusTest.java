package com.nedap.archie.rm.ehr;

import com.nedap.archie.json.JacksonUtil;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class EhrStatusTest {

    @Test
    public void testBooleanSerialization() throws IOException {
        EhrStatus status = new EhrStatus();
        StringWriter stringWriter = new StringWriter();
        JacksonUtil.getObjectMapper().writeValue(stringWriter, status);
        String expected = "{\n" +
                "  \"@type\" : \"EHR_STATUS\",\n" +
                "  \"links\" : [ ],\n" +
                "  \"path\" : \"/\",\n" +
                "  \"is_modifiable\" : false,\n" +
                "  \"is_queryable\" : false\n" +
                "}";
        assertEquals(removeWhiteSpaces(expected), removeWhiteSpaces(stringWriter.toString()));
    }

    private String removeWhiteSpaces(String input) {
        return input.replaceAll("\\s+", "");
    }
}