package com.nedap.archie.flattener;

public class FlattenerConfiguration {

    private boolean createOperationalTemplate = false;
    private boolean removeLanguagesFromMetaData = false;
    private boolean useComplexObjectForArchetypeSlotReplacement = false;
    private boolean removeZeroOccurrencesObjects = false;

    private String[] languagesToKeep = null;

    public boolean isCreateOperationalTemplate() {
        return createOperationalTemplate;
    }

    public void setCreateOperationalTemplate(boolean createOperationalTemplate) {
        this.createOperationalTemplate = createOperationalTemplate;
    }

    public boolean isRemoveLanguagesFromMetaData() {
        return removeLanguagesFromMetaData;
    }

    public void setRemoveLanguagesFromMetaData(boolean removeLanguagesFromMetaData) {
        this.removeLanguagesFromMetaData = removeLanguagesFromMetaData;
    }

    public boolean isUseComplexObjectForArchetypeSlotReplacement() {
        return useComplexObjectForArchetypeSlotReplacement;
    }

    public void setUseComplexObjectForArchetypeSlotReplacement(boolean useComplexObjectForArchetypeSlotReplacement) {
        this.useComplexObjectForArchetypeSlotReplacement = useComplexObjectForArchetypeSlotReplacement;
    }

    public boolean isRemoveZeroOccurrencesObjects() {
        return removeZeroOccurrencesObjects;
    }

    public void setRemoveZeroOccurrencesObjects(boolean removeZeroOccurrencesObjects) {
        this.removeZeroOccurrencesObjects = removeZeroOccurrencesObjects;
    }

    public String[] getLanguagesToKeep() {
        return languagesToKeep;
    }

    public void setLanguagesToKeep(String[] languagesToKeep) {
        this.languagesToKeep = languagesToKeep;
    }

    public FlattenerConfiguration clone() {
        FlattenerConfiguration cloned = new FlattenerConfiguration();
        cloned.setCreateOperationalTemplate(createOperationalTemplate);
        cloned.setRemoveLanguagesFromMetaData(removeLanguagesFromMetaData);
        cloned.setUseComplexObjectForArchetypeSlotReplacement(useComplexObjectForArchetypeSlotReplacement);
        cloned.setRemoveZeroOccurrencesObjects(removeZeroOccurrencesObjects);
        cloned.setLanguagesToKeep(languagesToKeep.clone());
        return cloned;
    }
}
