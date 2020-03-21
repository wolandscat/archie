package com.nedap.archie.opt_marand;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(converter=CTerminologyCodeConverter.class)
public interface CTerminologyCodeMixin {
}
