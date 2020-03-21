package com.nedap.archie.opt_marand;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nedap.archie.aom.TranslationDetails;

import java.util.Map;

public interface TerminologyIdParsingTerminologyCodeMixin {
    @JsonDeserialize(converter = TerminologyIdConverter.class)
    void setTerminologyId(String terminologyId);
}
