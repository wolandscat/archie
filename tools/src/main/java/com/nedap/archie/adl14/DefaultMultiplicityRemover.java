package com.nedap.archie.adl14;

import com.nedap.archie.adlparser.modelconstraints.BMMConstraintImposer;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CAttributeTuple;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.base.MultiplicityInterval;
import com.nedap.archie.rminfo.MetaModels;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes any non-default cardinality or existence. Useful in ADL 1.4 to ADL 2 conversion, but could also
 * be used in archetype modeling tools.
 */
public class DefaultMultiplicityRemover {

    private final MetaModels metaModels;
    private BMMConstraintImposer constraintImposer;

    private boolean removeEmptyAttributes = false;

    public DefaultMultiplicityRemover(MetaModels metaModels) {
        this(metaModels, false);
    }

    public DefaultMultiplicityRemover(MetaModels metaModels, boolean removeEmptyAttributes) {
        this.metaModels = metaModels;
        this.removeEmptyAttributes = removeEmptyAttributes;
    }

    public void setRemoveEmptyAttributes(boolean removeEmptyAttributes) {
        this.removeEmptyAttributes = removeEmptyAttributes;
    }

    public void removeDefaultMultiplicity(Archetype archetype) {
        this.metaModels.selectModel(archetype);
        if(metaModels.getSelectedModel() == null) {
            throw new IllegalArgumentException("cannot find model for argument, so cannot remove default multiplicity");
        }

        this.constraintImposer = new BMMConstraintImposer(metaModels.getSelectedBmmModel());
        removeMultiplicities(archetype.getDefinition());
    }

    private void removeMultiplicities(CObject object) {
        if(object.getOccurrences() != null) {
            MultiplicityInterval defaultRMOccurrences = object.getDefaultRMOccurrences(metaModels::referenceModelPropMultiplicity);
            if(defaultRMOccurrences.equals(object.getOccurrences())) {
                object.setOccurrences(null);
            }
        }
        if(object instanceof CComplexObject) {
            CComplexObject complexObject = (CComplexObject) object;

            List<CAttribute> attributesToRemove = new ArrayList<>();
            for (CAttribute attribute : object.getAttributes()) {
                removeMultiplicities(attribute);
                if(removeEmptyAttributes) {
                    //remove all empty attributes. They are 'attribute matches {*}' in ADL 1.4, and should not be present in ADL 2
                    if (attribute.getCardinality() == null && attribute.getExistence() == null && (attribute.getChildren() == null || attribute.getChildren().isEmpty())) {
                        if (!isInTuple(complexObject, attribute)) {
                            attributesToRemove.add(attribute);
                        }
                    }
                }
            }

            for (CAttribute attributeToRemove : attributesToRemove) {
                complexObject.removeAttribute(attributeToRemove);
            }
        }

    }

    private boolean isInTuple(CComplexObject complexObject, CAttribute attribute) {
        if(complexObject.getAttributeTuples() == null) {
            return false;
        }
        for(CAttributeTuple tuple:complexObject.getAttributeTuples()) {
            if(tuple.getMember(attribute.getRmAttributeName()) != null) {
                return true;
            }
        }
        return false;
    }

    private void removeMultiplicities(CAttribute attribute) {
        CAttribute defaultAttribute = constraintImposer.getDefaultAttribute(attribute.getParent().getRmTypeName(), attribute.getRmAttributeName());
        if(attribute.getExistence() != null) {
            if(defaultAttribute.getExistence() != null && defaultAttribute.getExistence().equals(attribute.getExistence())) {
                attribute.setExistence(null);
            }
        }
        if(attribute.getCardinality() != null) {
            if(defaultAttribute.getCardinality() != null) {
                if(defaultAttribute.getCardinality().equals(attribute.getCardinality())) {
                    attribute.setCardinality(null);
                }
            }
        }
        for(CObject child:attribute.getChildren()) {
            removeMultiplicities(child);
        }

    }
}
