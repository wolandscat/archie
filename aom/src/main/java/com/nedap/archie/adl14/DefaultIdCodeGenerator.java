package com.nedap.archie.adl14;

import com.nedap.archie.aom.Archetype;

public class DefaultIdCodeGenerator implements IdCodeGenerator {

    private final Archetype archetype;

    public DefaultIdCodeGenerator(Archetype archetype) {
        this.archetype = archetype;
    }

    @Override
    public String generateNextIdCode() {
        return archetype.generateNextIdCode();
    }

    @Override
    public String generateNextValueCode() {
        return archetype.generateNextValueCode();
    }

    @Override
    public String generateNextValueSetCode() {
        return archetype.generateNextValueSetCode();
    }

    @Override
    public String generateNextSpecializedIdCode(String nodeId) {
        return archetype.generateNextSpecializedIdCode(nodeId);
    }
}
