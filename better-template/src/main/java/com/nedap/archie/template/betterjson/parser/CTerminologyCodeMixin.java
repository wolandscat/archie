package com.nedap.archie.template.betterjson.parser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(converter=CTerminologyCodeConverter.class)
public interface CTerminologyCodeMixin {
}
