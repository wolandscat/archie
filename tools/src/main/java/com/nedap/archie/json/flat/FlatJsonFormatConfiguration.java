package com.nedap.archie.json.flat;

public class FlatJsonFormatConfiguration {
    private boolean writePipesForPrimitiveTypes = false;
    private boolean humanReadableFormat = false;
    private IndexNotation indexNotation = IndexNotation.AFTER_A_COLON;
    private String typeIdPropertyName = "_type";

    public FlatJsonFormatConfiguration() {

    }

    public static FlatJsonFormatConfiguration nedapInternalFormat() {
        FlatJsonFormatConfiguration config = new FlatJsonFormatConfiguration();
        config.setWritePipesForPrimitiveTypes(false);
        config.setHumanReadableFormat(false);
        config.setIndexNotation(IndexNotation.BRACKETED);
        config.setTypeIdPropertyName("@type");
        return config;
    }

    public static FlatJsonFormatConfiguration standardFormatInDevelopment() {
        FlatJsonFormatConfiguration config = new FlatJsonFormatConfiguration();
        config.setWritePipesForPrimitiveTypes(true);
        config.setHumanReadableFormat(false);
        config.setIndexNotation(IndexNotation.AFTER_A_COLON);
        return config;
    }

    public static FlatJsonFormatConfiguration humanReadableStandardFormatInDevelopment() {
        FlatJsonFormatConfiguration config = new FlatJsonFormatConfiguration();
        config.setWritePipesForPrimitiveTypes(true);
        config.setHumanReadableFormat(true);
        config.setIndexNotation(IndexNotation.AFTER_A_COLON);
        return config;
    }

    public boolean isWritePipesForPrimitiveTypes() {
        return writePipesForPrimitiveTypes;
    }

    public void setWritePipesForPrimitiveTypes(boolean writePipesForPrimitiveTypes) {
        this.writePipesForPrimitiveTypes = writePipesForPrimitiveTypes;
    }

    public boolean isHumanReadableFormat() {
        return humanReadableFormat;
    }

    public void setHumanReadableFormat(boolean humanReadableFormat) {
        this.humanReadableFormat = humanReadableFormat;
    }

    public IndexNotation getIndexNotation() {
        return indexNotation;
    }

    public void setIndexNotation(IndexNotation indexNotation) {
        this.indexNotation = indexNotation;
    }

    public String getTypeIdPropertyName() {
        return typeIdPropertyName;
    }

    public void setTypeIdPropertyName(String typeIdPropertyName) {
        this.typeIdPropertyName = typeIdPropertyName;
    }
}
