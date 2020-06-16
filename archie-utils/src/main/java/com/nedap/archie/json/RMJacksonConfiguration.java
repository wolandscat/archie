package com.nedap.archie.json;

import java.util.Objects;

public class RMJacksonConfiguration {
    private String typePropertyName = "@type";
    private boolean alwaysIncludeTypeProperty = true;
    private boolean addPathProperty = true;
    private boolean addExtraFieldsInArchetypeId = true;
    private boolean failOnUnknownProperties = false;
    private boolean serializeEmptyCollections = true;

    public RMJacksonConfiguration() {

    }

    public static RMJacksonConfiguration createStandardsCompliant() {
        RMJacksonConfiguration configuration = new RMJacksonConfiguration();
        configuration.setTypePropertyName("_type");
        configuration.setAlwaysIncludeTypeProperty(false);
        configuration.setAddPathProperty(false);
        configuration.setAddExtraFieldsInArchetypeId(false);
        return configuration;
    }

    public static RMJacksonConfiguration createConfigForJavascriptUsage() {
        RMJacksonConfiguration configuration = new RMJacksonConfiguration();
        configuration.setTypePropertyName("_type");
        configuration.setAlwaysIncludeTypeProperty(true);
        configuration.setAddPathProperty(true);
        configuration.setAddExtraFieldsInArchetypeId(true);
        return configuration;
    }

    public String getTypePropertyName() {
        return typePropertyName;
    }

    public void setTypePropertyName(String typePropertyName) {
        this.typePropertyName = typePropertyName;
    }

    public boolean isAlwaysIncludeTypeProperty() {
        return alwaysIncludeTypeProperty;
    }

    public void setAlwaysIncludeTypeProperty(boolean alwaysIncludeTypeProperty) {
        this.alwaysIncludeTypeProperty = alwaysIncludeTypeProperty;
    }

    public boolean isAddPathProperty() {
        return addPathProperty;
    }

    public void setAddPathProperty(boolean addPathProperty) {
        this.addPathProperty = addPathProperty;
    }

    public boolean isAddExtraFieldsInArchetypeId() {
        return addExtraFieldsInArchetypeId;
    }

    public void setAddExtraFieldsInArchetypeId(boolean addExtraFieldsInArchetypeId) {
        this.addExtraFieldsInArchetypeId = addExtraFieldsInArchetypeId;
    }

    public boolean isFailOnUnknownProperties() {
        return failOnUnknownProperties;
    }

    public void setFailOnUnknownProperties(boolean failOnUnknownProperties) {
        this.failOnUnknownProperties = failOnUnknownProperties;
    }

    public boolean isSerializeEmptyCollections() {
        return serializeEmptyCollections;
    }

    public void setSerializeEmptyCollections(boolean serializeEmptyCollections) {
        this.serializeEmptyCollections = serializeEmptyCollections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RMJacksonConfiguration)) return false;
        RMJacksonConfiguration that = (RMJacksonConfiguration) o;
        return alwaysIncludeTypeProperty == that.alwaysIncludeTypeProperty &&
                addPathProperty == that.addPathProperty &&
                addExtraFieldsInArchetypeId == that.addExtraFieldsInArchetypeId &&
                failOnUnknownProperties == that.failOnUnknownProperties &&
                serializeEmptyCollections == that.serializeEmptyCollections &&
                Objects.equals(typePropertyName, that.typePropertyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typePropertyName, alwaysIncludeTypeProperty, addPathProperty, addExtraFieldsInArchetypeId, failOnUnknownProperties, serializeEmptyCollections);
    }
}
