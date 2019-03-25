package com.nedap.archie.archetypevalidator.validations;

import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.archetypevalidator.ErrorType;
import com.nedap.archie.archetypevalidator.ValidatingVisitor;
import com.nedap.archie.base.Cardinality;
import com.nedap.archie.base.MultiplicityInterval;
import org.openehr.utils.message.I18n;

/**
 * Phase 0 validator on existence
 */
public class MultiplicitiesValidation extends ValidatingVisitor {
    public MultiplicitiesValidation() {
        super();
    }

    @Override
    public void validate(CAttribute attribute) {
        validateExistence(attribute);
    }

    public void validate(CObject cObject) {
        if(cObject.getParent() != null) {
            CAttribute attribute = cObject.getParent();
            if(attribute.getDifferentialPath() == null) {
                //we cannot validate differential paths here because we do not know the type
                //TODO: lookup differential path types to validate as well?
                if(attribute.isSingle()) {
                    if(cObject.getOccurrences() != null &&
                        (cObject.getOccurrences().isUpperUnbounded() || cObject.getOccurrences().getUpper() > 1)) {
                        addMessageWithPath(ErrorType.VACSO, cObject.path(),
                                I18n.t("The attribute {0} of type {1} can only have a single value, but the occurrences of the C_OBJECTS below has an upper limit of more than 1",
                                        attribute.getRmAttributeName(), attribute.getParent().getRmTypeName()));
                    }
                } else {
                    //multiple
                    Cardinality cardinality = attribute.getCardinality();
                    if(cardinality != null && !cardinality.getInterval().isUpperUnbounded() &&
                            cObject.getOccurrences() != null  && !cObject.getOccurrences().isUpperUnbounded() &&
                            cObject.getOccurrences().getUpper() > cardinality.getInterval().getUpper()) {
                        addMessageWithPath(ErrorType.VACMCU, cObject.path(),
                                I18n.t("The occurrences upper limit of the C_OBJECT {0} was {1}, which is greater than the parent attribute cardinality {2}",
                                        cObject.getNodeId(), cObject.getOccurrences().getUpper(),
                                        cardinality.getInterval().getUpper()));
                    }

                }
            }
        }

    }

    private void validateExistence(CAttribute attribute) {
        MultiplicityInterval existence = attribute.getExistence();
        if(existence != null) {
            if(existence.getLower() == null || existence.getUpper() == null || existence.isUpperUnbounded() ||
                    existence.isLowerUnbounded() || !existence.isLowerIncluded() || !existence.isUpperIncluded()) {
                addMessageWithPath(ErrorType.SEXLMG, attribute.path(),
                        I18n.t("Syntax error: existence must be one of 0..0, 0..1, or 1..1, but was {0}", existence));
            } else {
                if(existence.getLower() == 0) {
                  if(existence.getUpper() != 0 && existence.getUpper() != 1) {
                      addMessageWithPath(ErrorType.SEXLU, attribute.path(),
                              I18n.t("Syntax error: existence must be one of 0..0, 0..1, or 1..1, but was {0}", existence));
                  }
                } else if(existence.getLower() == 1) {
                    if(existence.getUpper() != 1) {
                        addMessageWithPath(ErrorType.SEXLU, attribute.path(),
                                I18n.t("Syntax error: existence must be one of 0..0, 0..1, or 1..1, but was {0}", existence));
                    }
                } else if (existence.getLower() > 1 || existence.getLower() < 0) {
                    addMessageWithPath(ErrorType.SEXLU, attribute.path(),
                            I18n.t("Syntax error: existence must be one of 0..0, 0..1, or 1..1, but was {0}", existence));
                }
            }
        }
    }

}
