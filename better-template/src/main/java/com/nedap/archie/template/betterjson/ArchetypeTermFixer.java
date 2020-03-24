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
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.aom.utils.NodeIdUtil;
import com.nedap.archie.base.Interval;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;

import java.util.ArrayList;
import java.util.List;

public class ArchetypeTermFixer {

    private String originalLanguage;

    public void fixTerms(Archetype archetype, InMemoryFullArchetypeRepository repo) {
        originalLanguage = archetype.getOriginalLanguage().getCodeString();
        fixTerms(archetype,repo, archetype.getDefinition());

        if(archetype instanceof Template) {
            Template template = (Template) archetype;
            for(TemplateOverlay overlay:template.getTemplateOverlays()) {
                fixTerms(overlay, repo, overlay.getDefinition());
            }
        }
    }

    private void fixTerms(Archetype archetype, InMemoryFullArchetypeRepository repo, CComplexObject cObject) {
        for(CAttribute attribute:cObject.getAttributes()) {
            fixTerms(archetype, repo, attribute);
        }
    }

    private void fixTerms(Archetype archetype, InMemoryFullArchetypeRepository repo, CAttribute cAttribute) {
        List<CObject> cObjectsToRemove = new ArrayList<>();
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
                } else if(!archetype.getTerminology().getTermDefinitions().get(language).containsKey(cObject.getNodeId()) &&
                        archetype.specializationDepth() == AOMUtils.getSpecializationDepthFromCode(cObject.getNodeId())
                ) {
                    Archetype referencedArchetype = repo.getArchetype(((CArchetypeRoot) cObject).getArchetypeRef());
                    createTermForNewCode(archetype, cObject, referencedArchetype);
                    //TODO: fix lots of problems where node ids are set wrong, for example id2.1 to set the occurrences of id2 to {0} is a problem!
                }
                fixTerms(archetype, repo, (CComplexObject) cObject);
            } else if(cObject instanceof CComplexObject) {
                if(noTerminology) {
                } else if(!archetype.getTerminology().getTermDefinitions().get(language).containsKey(cObject.getNodeId()) &&
                        archetype.specializationDepth() == AOMUtils.getSpecializationDepthFromCode(cObject.getNodeId())
                ) {
                    createTermForNewCode(archetype, cObject, null);
                    //TODO: fix lots of problems where node ids are set wrong, for example id2.1 to set the occurrences of id2 to {0} is a problem!
                }
                fixTerms(archetype, repo, (CComplexObject) cObject);
            }
        }
        for(CObject cObject:cObjectsToRemove) {
            cAttribute.removeChild(cObject);
        }
    }

    private void createTermForNewCode(Archetype archetype, CObject cObject, Archetype referencedArchetype) {
        //if(cObject.getParent().isMultiple()) {
            for(String language: archetype.getTerminology().getTermDefinitions().keySet()) {
                //TODO: add new archetype term to conversion log?
                ArchetypeTerm term = null;
                if(term == null) {
                    ArchetypeTerm newTerm = new ArchetypeTerm();
                    newTerm.setCode(cObject.getNodeId());
                    ArchetypeTerm rootTerm = null;
                    if(referencedArchetype != null) {
                        rootTerm = referencedArchetype.getTerm(archetype.getDefinition(), language);
                    }

                    newTerm.setText(rootTerm == null ? "* missing code" : rootTerm.getText());
                    newTerm.setDescription(rootTerm == null ? "* missing code" : rootTerm.getDescription());
                    archetype.getTerminology().getTermDefinitions().get(language).put(newTerm.getCode(), newTerm);
                }
            }
       // }
    }
}
