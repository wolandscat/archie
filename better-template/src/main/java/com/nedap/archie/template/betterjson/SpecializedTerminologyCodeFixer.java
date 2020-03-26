package com.nedap.archie.template.betterjson;

import com.nedap.archie.adl14.ADL14NodeIDConverter;
import com.nedap.archie.adl14.OutsideRangeIdCodeGenerator;
import com.nedap.archie.adl14.log.ConvertedCodeResult;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.ArchetypeModelObject;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.Template;
import com.nedap.archie.aom.TemplateOverlay;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lots of errors in the better template where new value sets get created that should have been specialised codes. Fix that here.
 */
public class SpecializedTerminologyCodeFixer {

    Map<String, ConvertedCodeResult> convertedCodes;

    public void fixTerminologyCodes(Archetype archetype, InMemoryFullArchetypeRepository repo) {
        fixInner(archetype, getFlatParent(archetype, repo));
        if(archetype instanceof Template) {
            Template template = (Template) archetype;
            for(TemplateOverlay overlay:template.getTemplateOverlays()) {
                fixInner(overlay, getFlatParent(overlay, repo));
            }
        }

    }

    private Archetype getFlatParent(Archetype archetype, InMemoryFullArchetypeRepository repo) {
        Archetype flatParent =  null;
        if(archetype.getParentArchetypeId() != null) {
            flatParent = repo.getFlattenedArchetype(archetype.getParentArchetypeId());
        }
        return flatParent;
    }

    private void fixInner(Archetype archetype, Archetype flatParent) {
        if(flatParent == null) {
            return;
        }
        convertedCodes = new LinkedHashMap<>();
        fixInner(archetype, archetype.getDefinition(), flatParent);
        ADL14NodeIDConverter.convertTermDefinitions(archetype, convertedCodes);
    }

    private void fixInner(Archetype archetype, CObject cObject, Archetype flatParent) {
        if(cObject instanceof CTerminologyCode) {
            CTerminologyCode cTerminologyCode = (CTerminologyCode) cObject;
            if(cTerminologyCode.getConstraint() != null && cTerminologyCode.getConstraint().size() > 0) {
                String constraint = cTerminologyCode.getConstraint().get(0);
                if(AOMUtils.getSpecializationDepthFromCode(constraint) == archetype.specializationDepth()) {
                    String pathInParent = AOMUtils.pathAtSpecializationLevel(cObject.getPathSegments(), flatParent.specializationDepth());
                    ArchetypeModelObject parentCObject = flatParent.itemAtPath(pathInParent);
                    if(parentCObject instanceof CTerminologyCode) {
                        CTerminologyCode codeInParent = (CTerminologyCode) parentCObject;
                        if(codeInParent.getConstraint() != null && codeInParent.getConstraint().size() > 0) {
                            String constraintInParent = cTerminologyCode.getConstraint().get(0);
                            if(AOMUtils.isValueSetCode(constraint) && AOMUtils.isValueSetCode(constraintInParent) && !AOMUtils.codesConformant(constraint, constraintInParent)) {
                                //this is invalid. Let's fix it!
                                String newCode = archetype.generateNextSpecializedIdCode(constraintInParent);
                                cTerminologyCode.getConstraint().set(0, newCode);

                                ValueSet valueSet = archetype.getTerminology().getValueSets().remove(constraint);
                                archetype.getTerminology().getValueSets().put(newCode, valueSet);
                                convertedCodes.put(constraint, new ConvertedCodeResult(constraint, newCode));

                            }

                        }
                    }
                }
            }

        }
        for(CAttribute attribute:cObject.getAttributes()) {
            fixInner(archetype, attribute, flatParent);
        }
    }

    private void fixInner(Archetype archetype, CAttribute cAttribute, Archetype flatParent) {

        for(CObject child:cAttribute.getChildren()) {
            fixInner(archetype, child, flatParent);
        }
    }
}
