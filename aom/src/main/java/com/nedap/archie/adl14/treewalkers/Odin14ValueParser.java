package com.nedap.archie.adl14.treewalkers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nedap.archie.adl14.ADL14ConversionConfiguration;
import com.nedap.archie.adlparser.antlr.Adl14Parser;
import com.nedap.archie.adlparser.antlr.AdlLexer;
import com.nedap.archie.adlparser.antlr.AdlParser;
import com.nedap.archie.serializer.odin.AdlOdinToJsonConverter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;

/**
 * Configurable ODIN parser for use in ADL 1.4 conversion.
 *
 * Created by pieter.bos on 15/10/15.
 */
public class Odin14ValueParser {

    private ObjectMapper objectMapper;

    public Odin14ValueParser(ADL14ConversionConfiguration configuration) {
        objectMapper = new ObjectMapper();
        AdlOdinToJsonConverter.configureObjectMapper(objectMapper, configuration.isAllowDuplicateFieldNames());
    }

    public <T> T convert(AdlParser.Odin_textContext odin, Class<T> clazz) {
        try {
            return objectMapper.readValue(new AdlOdinToJsonConverter().convert(odin), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T convert(String odin, Class<T> clazz) {
        AdlLexer adlLexer = new AdlLexer(CharStreams.fromString(odin));
        AdlParser parser = new AdlParser(new CommonTokenStream(adlLexer));
        return convert(parser.odin_text(), clazz);
    }

    public static String parseOdinStringValue(Adl14Parser.String_valueContext context) {
        if(context == null) {
            return null;
        }
        String text = context.getText();
        //regexps
        if(text.startsWith("/")) {
            return text;
        }
        if(text.startsWith("^")) {
            return text;
        }

        if(!text.startsWith("\"")) {
            throw new IllegalArgumentException("text should start with '/', '^' or '\"'");
        }
        //strip the quotes
        if(text.length() == 2) { // empty string, ""
            return "";
        }
        return StringEscapeUtils.unescapeJava(text.substring(1, text.length() - 1));
    }
}