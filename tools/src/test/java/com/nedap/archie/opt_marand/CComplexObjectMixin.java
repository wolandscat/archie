package com.nedap.archie.opt_marand;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nedap.archie.base.OpenEHRBase;

@JsonIgnoreProperties(value = {"attributeCustomizations"})
public interface CComplexObjectMixin {

}
