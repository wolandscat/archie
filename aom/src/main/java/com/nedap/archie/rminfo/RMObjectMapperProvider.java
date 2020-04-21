package com.nedap.archie.rminfo;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface RMObjectMapperProvider {

    ObjectMapper getInputOdinObjectMapper();

    ObjectMapper getOutputOdinObjectMapper();
}
