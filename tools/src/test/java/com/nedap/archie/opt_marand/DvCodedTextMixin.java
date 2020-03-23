package com.nedap.archie.opt_marand;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.nedap.archie.rm.datatypes.CodePhrase;

public interface DvCodedTextMixin {

    @JsonAlias("defining_code")
    void setDefiningCode(CodePhrase definingCode);
}
