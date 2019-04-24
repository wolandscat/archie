package com.nedap.archie.adl14;

import com.google.common.collect.Lists;
import com.nedap.archie.adl14.log.CreatedCode;
import com.nedap.archie.adl14.log.ReasonForCodeCreation;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.ArchetypeSlot;
import com.nedap.archie.aom.CArchetypeRoot;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.aom.CComplexObjectProxy;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.base.terminology.TerminologyCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ADL14TermConstraintConverter {

    private final Archetype flatParentArchetype;
    private Archetype archetype;
    private ADL14NodeIDConverter converter;

    public ADL14TermConstraintConverter(ADL14NodeIDConverter converter, Archetype archetype, Archetype flatParentArchetype) {
        this.converter = converter;
        this.archetype = archetype;
        this.flatParentArchetype = flatParentArchetype;
    }

    public void convert() {
        convert(archetype.getDefinition());
    }

    private void convert(CObject cObject) {

        if (cObject instanceof CTerminologyCode) {
            convertCTerminologyCode((CTerminologyCode) cObject);
        }
        for(CAttribute attribute:cObject.getAttributes()) {
            convert(attribute);
        }
    }

    private void convert(CAttribute attribute) {
        for(CObject object:attribute.getChildren()) {
            convert(object);
        }
    }

    private void convertCTerminologyCode(CTerminologyCode cTerminologyCode) {
        if(cTerminologyCode.getConstraint() != null && !cTerminologyCode.getConstraint().isEmpty()) {
            String firstConstraint = cTerminologyCode.getConstraint().get(0);
            TerminologyCode termCode = TerminologyCode.createFromString(firstConstraint);
            boolean isLocalCode = termCode.getTerminologyId() == null || termCode.getTerminologyId().equalsIgnoreCase("local");
            if(isLocalCode && AOMUtils.isValueCode(firstConstraint)) {
                //local codes
                if(cTerminologyCode.getConstraint().size() == 1) {
                    //do not create a value set, just convert the code
                    String newCode = converter.convertValueCode(firstConstraint);
                    converter.addConvertedCode(firstConstraint, newCode);
                    cTerminologyCode.setConstraint(Lists.newArrayList(newCode));
                } else {
                    Set<String> localCodes = new LinkedHashSet<>();
                    for(String code:cTerminologyCode.getConstraint()) {
                        String newCode = converter.convertValueCode(code);
                        converter.addConvertedCode(code, newCode);
                        localCodes.add(newCode);
                    }

                    ValueSet valueSet = findOrCreateValueSet(cTerminologyCode.getArchetype(), localCodes, cTerminologyCode);
                    cTerminologyCode.setConstraint(Lists.newArrayList(valueSet.getId()));
                }
                if(cTerminologyCode.getAssumedValue() != null) {
                    TerminologyCode assumedValue = cTerminologyCode.getAssumedValue();
                    String oldCode = assumedValue.getCodeString();
                    String newCode = converter.convertValueCode(oldCode);
                    assumedValue.setCodeString(newCode);
                }
            } else if (isLocalCode && AOMUtils.isValueSetCode(termCode.getCodeString())) {
                List<String> newConstraint = new ArrayList<>();
                for(String constraint:cTerminologyCode.getConstraint()) {
                    TerminologyCode code = TerminologyCode.createFromString(constraint);
                    String newCode = converter.convertValueSetCode(termCode.getCodeString());
                    converter.addConvertedCode(termCode.getCodeString(), newCode);
                    newConstraint.add(newCode);
                }
                cTerminologyCode.setConstraint(newConstraint);

            } else {
                if (cTerminologyCode.getConstraint().size() == 1) {
                    try {
                        //do not create a value set, create a code plus binding to the old non-local code
                        URI uri = new ADL14ConversionUtil(converter.getConversionConfiguration()).convertToUri(termCode);
                        Map<String, URI> termBindingsMap = archetype.getTerminology().getTermBindings().get(termCode.getTerminologyId());
                        if (termBindingsMap == null) {
                            termBindingsMap = new LinkedHashMap<>();
                            archetype.getTerminology().getTermBindings().put(termCode.getTerminologyId(), termBindingsMap);
                        }
                        //TODO: check if this is a converted or old term binding - old is unusual, but could be possible!
                        String existingTermBinding = ADL14ConversionUtil.findExistingTermBinding(termCode.getTerminologyId(), archetype, flatParentArchetype, uri, termBindingsMap);
                        if (existingTermBinding != null) {
                            cTerminologyCode.setConstraint(Lists.newArrayList(existingTermBinding));
                        } else {
                            String valueCode = archetype.generateNextValueCode();
                            termBindingsMap.put(valueCode, uri);
                            CreatedCode createdCode = new CreatedCode(valueCode, ReasonForCodeCreation.CREATED_VALUE_FOR_EXTERNAL_TERM);
                            createdCode.setOriginalTerm(termCode);
                            converter.addCreatedCode(termCode.toString(), createdCode);
                            cTerminologyCode.setConstraint(Lists.newArrayList(valueCode));
                            for (String language : archetype.getTerminology().getTermDefinitions().keySet()) {
                                ArchetypeTerm term = new ArchetypeTerm();
                                term.setCode(valueCode);
                                term.setText("Term binding for " + termCode.toString() + ", translation not known in ADL 1.4 -> ADL 2 converter");
                                term.setDescription(term.getText());
                                archetype.getTerminology().getTermDefinitions().get(language).put(valueCode, term);
                            }
                        }

                    } catch (URISyntaxException e) {
                        //TODO
                    }
                } else {
//                    //TODO: create a value set with codes, plus a term binding per code
//                    Set<String> localCodes = new LinkedHashSet<>();
//                    for(String code:cTerminologyCode.getConstraint()) {
//                        String newCode = convertValueCode(code);
//                        this.addConvertedCode(code, newCode);
//                        localCodes.add(newCode);
//                    }
//
//                    ValueSet valueSet = findOrCreateValueSet(cTerminologyCode.getArchetype(), localCodes, cTerminologyCode);
//                    cTerminologyCode.setConstraint(Lists.newArrayList(valueSet.getId()));
                }
                //TODO: assumed value!
//                if(cTerminologyCode.getAssumedValue() != null) {
//                    TerminologyCode assumedValue = cTerminologyCode.getAssumedValue();
//                    String oldCode = assumedValue.getCodeString();
//                    String newCode = convertValueCode(oldCode);
//                    assumedValue.setCodeString(newCode);
//                }
            }
        }
    }

    private ValueSet findOrCreateValueSet(Archetype archetype, Set<String> localCodes, CObject owningConstraint) {
        //TODO: already checks for equal value sets. But if specialized check if parent contains a value set that  can be redefined to
        //be the same
        if(flatParentArchetype != null) {
            ValueSet valueSet = findValueSet(flatParentArchetype, localCodes);
            if (valueSet != null) {
                return valueSet;
            }
        }
        ValueSet valueSet = findValueSet(archetype, localCodes);
        if (valueSet != null) {
            return valueSet;
        }
        valueSet = new ValueSet();
        valueSet.setMembers(localCodes);
        valueSet.setId(archetype.generateNextValueSetCode());
        converter.addCreatedCode(valueSet.getId(), new CreatedCode(valueSet.getId(), ReasonForCodeCreation.CREATED_VALUE_SET));
        converter.addCreatedValueSet(valueSet.getId(), valueSet);
        archetype.getTerminology().getValueSets().put(valueSet.getId(), valueSet);

        for(String language: archetype.getTerminology().getTermDefinitions().keySet()) {
            //TODO: add new archetype term to conversion log!
            ArchetypeTerm term = getTerm(language, owningConstraint);
            if(term != null) {
                ArchetypeTerm newTerm = new ArchetypeTerm();
                newTerm.setCode(valueSet.getId());
                newTerm.setText(term.getText() + " (synthesised)");
                newTerm.setDescription(term.getDescription() + " (synthesised)");
                archetype.getTerminology().getTermDefinitions().get(language).put(newTerm.getCode(), newTerm);
            }
        }
        return valueSet;
    }

    private ValueSet findValueSet(Archetype archetype, Set<String> localCodes) {
        for(ValueSet valueSet:archetype.getTerminology().getValueSets().values()) {
            if(valueSet.getMembers().equals(localCodes)) {
                return valueSet;
            }
        }
        return null;
    }

    /**
     * Get a term from the archetype terminology for a CObject that has the new node id, but with a yet unconverted
     * terminology, so using the newCodetoOldCodeMap to retrieve the old code first
     * @param owningConstraint
     */
    private ArchetypeTerm getTerm(String language, CObject owningConstraint) {
        CObject cObject = owningConstraint;
        while(cObject != null) {
            if (owningConstraint.getNodeId() != null) {
                String oldCode = converter.getOldCodeForNewCode(cObject.getNodeId());
                if(oldCode != null && archetype.getTerminology().getTermDefinition(language, oldCode) != null) {
                    return archetype.getTerminology().getTermDefinition(language, oldCode);
                }
            }
            cObject = cObject.getParent() == null ? null : cObject.getParent().getParent();
        }
        return null;
    }

}
