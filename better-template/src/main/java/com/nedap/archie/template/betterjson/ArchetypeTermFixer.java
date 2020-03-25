package com.nedap.archie.template.betterjson;

import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.CArchetypeRoot;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.CPrimitiveObject;
import com.nedap.archie.aom.Template;
import com.nedap.archie.aom.TemplateOverlay;
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ArchetypeTerminology;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.aom.utils.NodeIdUtil;
import com.nedap.archie.base.Interval;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ArchetypeTermFixer {

    private String originalLanguage;

    public void fixTerms(Archetype archetype, InMemoryFullArchetypeRepository repo) {
        originalLanguage = archetype.getOriginalLanguage().getCodeString();
        fixTerms(archetype,repo, archetype.getDefinition());

        removeUntranslatedLanguages(archetype);

        if(archetype instanceof Template) {
            Template template = (Template) archetype;
            for(TemplateOverlay overlay:template.getTemplateOverlays()) {
                fixTerms(overlay, repo, overlay.getDefinition());
            }
        }
    }

    /** If any languages do not exist in the terminology, remove from translations*/
    private void removeUntranslatedLanguages(Archetype archetype) {
        if(archetype.getTranslations() == null) {
            return;
        }
        List<String> toRemove = new ArrayList<>();
        for(String translation:archetype.getTranslations().keySet()) {
            if(archetype.getTerminology() != null && !archetype.getTerminology().getTermDefinitions().containsKey(translation)) {
                toRemove.add(translation);
            }
        }
        if(!toRemove.isEmpty()) {
            for(String translation:toRemove) {
                archetype.getTranslations().remove(translation);
            }
        }
    }

    private void fixTerms(Archetype archetype, InMemoryFullArchetypeRepository repo, CComplexObject cObject) {
        for(CAttribute attribute:cObject.getAttributes()) {
            fixTerms(archetype, repo, attribute);
        }
    }

    private void fixTerms(Archetype archetype, InMemoryFullArchetypeRepository repo, CAttribute cAttribute) {
        //TODO: create terminology if it does not exist!
        String language = originalLanguage;
        boolean noTerminology = false;
        if(archetype.getTerminology().getTermDefinitions().isEmpty()) {
            noTerminology = true;
        } else if(!archetype.getTerminology().getTermDefinitions().containsKey(originalLanguage)) {
            language = archetype.getTerminology().getTermDefinitions().keySet().iterator().next();
        }
        for(CObject cObject:cAttribute.getChildren()) {
            if(cObject instanceof CArchetypeRoot) {
                if (noTerminology) {
                    //
                } else if(!archetype.getTerminology().getTermDefinitions().get(language).containsKey(cObject.getNodeId()) &&
                        archetype.specializationDepth() == AOMUtils.getSpecializationDepthFromCode(cObject.getNodeId())
                ) {
                    Archetype referencedArchetype = repo.getArchetype(((CArchetypeRoot) cObject).getArchetypeRef());
                    createTermForNewCodeWithRoot(archetype, cObject, referencedArchetype);
                    //TODO: fix lots of problems where node ids are set wrong, for example id2.1 to set the occurrences of id2 to {0} is a problem!
                }
                fixTerms(archetype, repo, (CComplexObject) cObject);
            } else if(cObject instanceof CComplexObject) {
                if(noTerminology) {
                } else if(!archetype.getTerminology().getTermDefinitions().get(language).containsKey(cObject.getNodeId()) &&
                        archetype.specializationDepth() == AOMUtils.getSpecializationDepthFromCode(cObject.getNodeId())
                ) {
                    Archetype flatParent = repo.getArchetype(archetype.getParentArchetypeId());
                    createTermForNewCodeWithFlatParent(archetype, cObject, flatParent);
                    //TODO: fix lots of problems where node ids are set wrong, for example id2.1 to set the occurrences of id2 to {0} is a problem!
                }
                fixTerms(archetype, repo, (CComplexObject) cObject);
            }
        }
    }

    private static Pattern synthesizedCodesPattern = Pattern.compile("(at|ac|id)(0\\.)*9[0-9]3");

    private void createTermForNewCodeWithRoot(Archetype archetype, CObject cObject, Archetype referencedArchetype) {
        if(!synthesizedCodesPattern.matcher(cObject.getNodeId()).matches()) {
            //if(cObject.getParent().isMultiple()) {
            for (String language : archetype.getTerminology().getTermDefinitions().keySet()) {
                //TODO: add new archetype term to conversion log?

                ArchetypeTerm newTerm = new ArchetypeTerm();
                newTerm.setCode(cObject.getNodeId());
                ArchetypeTerm rootTerm = null;
                if (referencedArchetype != null) {
                    rootTerm = referencedArchetype.getTerm(referencedArchetype.getDefinition(), language);
                }

                newTerm.setText(rootTerm == null ? "* missing code" : rootTerm.getText());
                newTerm.setDescription(rootTerm == null ? "* missing code" : rootTerm.getDescription());
                archetype.getTerminology().getTermDefinitions().get(language).put(newTerm.getCode(), newTerm);

            }
        }
       // }
    }

    private void createTermForNewCodeWithFlatParent(Archetype archetype, CObject cObject, Archetype flatParent) {
        if(!synthesizedCodesPattern.matcher(cObject.getNodeId()).matches()) {
            //if(cObject.getParent().isMultiple()) {
            for (String language : archetype.getTerminology().getTermDefinitions().keySet()) {
                //TODO: add new archetype term to conversion log?

                ArchetypeTerm newTerm = new ArchetypeTerm();
                newTerm.setCode(cObject.getNodeId());
                ArchetypeTerm parentTerm = null;
                if (flatParent != null) {
                    ArchetypeTerminology terminology = flatParent.getTerminology();
                    parentTerm = terminology == null ?
                            null :
                            terminology.getTermDefinition(language, AOMUtils.codeAtLevel(cObject.getNodeId(), flatParent.specializationDepth()));
                }

                newTerm.setText(parentTerm == null ? "* missing code" : parentTerm.getText());
                newTerm.setDescription(parentTerm == null ? "* missing code" : parentTerm.getDescription());
                archetype.getTerminology().getTermDefinitions().get(language).put(newTerm.getCode(), newTerm);

            }
        }
        // }
    }



}
