package com.nedap.archie.archetypevalidator.validations;

import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.aom.CComplexObjectProxy;
import com.nedap.archie.aom.terminology.ArchetypeTerminology;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.archetypevalidator.ErrorType;
import com.nedap.archie.archetypevalidator.ValidatingVisitor;
import com.nedap.archie.base.Cardinality;
import com.nedap.archie.query.ComplexObjectProxyReplacement;
import org.openehr.utils.message.I18n;

import java.net.URI;
import java.util.Map;

public class FlatFormValidation extends ValidatingVisitor {

    @Override
    protected void validate(CComplexObjectProxy cObject) {
        //validate that CComplexObjectProxy nodes have a valid path
        ComplexObjectProxyReplacement complexObjectProxyReplacement = ComplexObjectProxyReplacement.getComplexObjectProxyReplacement(cObject);
        if(complexObjectProxyReplacement == null) {
            addMessageWithPath(ErrorType.VUNP, cObject.path(), I18n.t("Use node (C_COMPLEX_OBJECT_PROXY) points to a path that cannot be found: {0}", cObject.getTargetPath()));
        } else {
            CComplexObject replacement = complexObjectProxyReplacement.getReplacement();
            if(!combinedModels.rmTypesConformant(replacement.getRmTypeName(), cObject.getRmTypeName())) {
                addMessageWithPath(ErrorType.VUNT, cObject.path(), I18n.t("Use node (C_COMPLEX_OBJECT_PROXY) points to type {0}, which does not conform to type {1}", replacement.getRmTypeName(), cObject.getRmTypeName()));
            }
        }
    }

    @Override
    protected void validate(CAttribute cAttribute) {
        Cardinality cardinality = cAttribute.getCardinality();
        if(cardinality != null && !cardinality.getInterval().isUpperUnbounded()) {
            if(cAttribute.getAggregateOccurrencesLowerSum() > cardinality.getInterval().getUpper()) {
                addWarningWithPath(ErrorType.WACMCL, cAttribute.path(), I18n.t("The occurrences of all C_OBJECTS under this attributes is at least {0}, which does not fit in the upper limit of the cardinality of the attribute, {1}",
                        cAttribute.getAggregateOccurrencesLowerSum(), cardinality.getInterval().getUpper()));
            } else if (cAttribute.getMinimumChildCount() > cardinality.getInterval().getUpper()) {
                addMessageWithPath(ErrorType.VACMCO, cAttribute.path(), I18n.t("The attribute contains {0} objects that are required, but only has an upper cardinality of {1}",
                        cAttribute.getMinimumChildCount(), cardinality.getInterval().getUpper()));
            }
        }
    }

    @Override
    protected void beginValidation() {
        //validateTerminologyBindings();
    }

    private void validateTerminologyBindings() {
        ArchetypeTerminology terminology = archetype.getTerminology();
        Map<String, Map<String, URI>> termBindings = terminology.getTermBindings();
        for(String terminologyId: termBindings.keySet()) {
            for(String constraintCodeOrPath: termBindings.get(terminologyId).keySet()) {
                boolean archetypeHasPath = false;
                try {
                    archetypeHasPath = archetype.hasPath(constraintCodeOrPath);
                } catch (Exception e) {
                    //if not a valid path, fine
                }
                if(!AOMUtils.isValidCode(constraintCodeOrPath) && !(
                        archetypeHasPath || combinedModels.hasReferenceModelPath(archetype.getDefinition().getRmTypeName(), constraintCodeOrPath)
                )
                        ) {
                    addMessage(ErrorType.VTTBK, I18n.t("Term binding key {0} points to a path that cannot be found in the archetype", constraintCodeOrPath));
                }
                else if(AOMUtils.isValidCode(constraintCodeOrPath) &&
                        !terminology.hasCode(constraintCodeOrPath) &&
                        !(archetype.isSpecialized() && flatParent != null && !flatParent.getTerminology().hasCode(constraintCodeOrPath))
                        )
                {
                    addMessage(ErrorType.VTTBK, I18n.t("Term binding key {0} is not present in the terminology", constraintCodeOrPath));
                } else {
                    //TODO: two warnings
                }
            }
        }
    }
}
