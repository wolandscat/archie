package com.nedap.archie.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.nedap.archie.rminfo.RMObjectMapperProvider;
import org.openehr.odin.jackson.ODINMapper;

import java.io.IOException;

public class ArchieRMObjectMapperProvider implements RMObjectMapperProvider {

    @Override
    public ObjectMapper getInputOdinObjectMapper() {
        ObjectMapper odinMapper = new ObjectMapper();
        JacksonUtil.configureObjectMapper(odinMapper);
        odinMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        //keywords = <"value"> is indistinguishable from keywords = <"value1", "value2">
        odinMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        //odin sometimes does <> where it can mean either an empty array OR a null object. Nastyness
        odinMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
//        if(!allowDuplicates) {
//            odinMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
//        } else {
            odinMapper.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
//        }

        //ignore the @type field when not needed
        odinMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
                if (propertyName.equalsIgnoreCase("@type")) {
                    return true;
                }
                return super.handleUnknownProperty(ctxt, p, deserializer, beanOrClass, propertyName);
            }
        });
        return odinMapper;
    }

    @Override
    public ObjectMapper getOutputOdinObjectMapper() {
        ODINMapper odinMapper = new ODINMapper();
        JacksonUtil.configureObjectMapper(odinMapper);
        return odinMapper;
    }

    @Override
    public ObjectMapper getJsonObjectMapper() {
        return JacksonUtil.getObjectMapper(RMJacksonConfiguration.createStandardsCompliant());
    }
}
