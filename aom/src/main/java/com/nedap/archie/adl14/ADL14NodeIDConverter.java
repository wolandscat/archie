package com.nedap.archie.adl14;

import com.google.common.collect.Lists;
import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.adl14.log.ConvertedCodeResult;
import com.nedap.archie.adl14.log.CreatedCode;
import com.nedap.archie.adl14.log.ReasonForCodeCreation;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.ArchetypeSlot;
import com.nedap.archie.aom.CArchetypeRoot;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.aom.CComplexObjectProxy;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.CPrimitiveObject;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.aom.utils.NodeIdUtil;
import com.nedap.archie.base.terminology.TerminologyCode;
import com.nedap.archie.paths.PathSegment;
import com.nedap.archie.query.APathQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ADL14NodeIDConverter {

    private final Archetype archetype;

    public ADL14NodeIDConverter(Archetype archetype) {
        this.archetype = archetype;
    }

    /**
     * Contains a mapping between the adl 1.4 codes and the generated adl 2 code
     */
    private Map<String, ConvertedCodeResult> convertedCodes = new LinkedHashMap<>();
    private Map<String, CreatedCode> createdCodes = new LinkedHashMap<>();
    private Map<String, ValueSet> createdValueSets = new LinkedHashMap<>();

    private Map<String, String> newCodeToOldCodeMap = new LinkedHashMap<>();

    public ADL2ConversionLog convert() {

        convert(archetype.getDefinition());
        convertTermBindings(archetype);
        generateMissingNodeIds(archetype.getDefinition());
        convertTermDefinitions();
        return new ADL2ConversionLog(convertedCodes, createdCodes, createdValueSets);
    }

    private void convertTermDefinitions() {
        for(ConvertedCodeResult convertedCode: convertedCodes.values()) {
            updateTermDefinition(convertedCode.getOriginalCode(), convertedCode.getConvertedCodes());
        }
        //TODO: scan terminology for any unused codes. Then warn or convert about them?
    }

    private void convertTermBindings(Archetype archetype) {
        if(archetype.getTerminology().getTermBindings() != null) {
            Map<String, Map<String, URI>> newTermBindings = new LinkedHashMap<>();
            for(String terminologyid: archetype.getTerminology().getTermBindings().keySet()) {
                Map<String, URI> termbindingMap = archetype.getTerminology().getTermBindings().get(terminologyid);
                Map<String, URI> newTermbindingsMap = new LinkedHashMap<>();
                newTermBindings.put(terminologyid, newTermbindingsMap);
                for(String valueOrPath:termbindingMap.keySet()) {
                    if(valueOrPath.startsWith("/")) {
                        String newPath = convertPath(valueOrPath);
                        newTermbindingsMap.put(newPath, termbindingMap.get(valueOrPath));
                    } else if (AOMUtils.isValueCode(valueOrPath)) {
                        if(convertedCodes.containsKey(valueOrPath)) {
                            for(String newCode:convertedCodes.get(valueOrPath).getConvertedCodes()) {
                                newTermbindingsMap.put(newCode, termbindingMap.get(valueOrPath));
                            }
                        } else {
                            //TODO: bound to at-code without automated conversion. Not sure if this should be an at, ac or id code
                            newTermbindingsMap.put(valueOrPath, termbindingMap.get(valueOrPath));
                        }
                    } else if (AOMUtils.isValueSetCode(valueOrPath)) {
                        if(convertedCodes.containsKey(valueOrPath)) {
                            for(String newCode:convertedCodes.get(valueOrPath).getConvertedCodes()) {
                                newTermbindingsMap.put(newCode, termbindingMap.get(valueOrPath));
                            }
                        } else {
                            //TODO: bound to at-code without automated conversion. Not sure if this should be an at, ac or id code
                            newTermbindingsMap.put(valueOrPath, termbindingMap.get(valueOrPath));
                        }
                    } else if (AOMUtils.isIdCode(valueOrPath)) {
                        if(convertedCodes.containsKey(valueOrPath)) {
                            for(String newCode:convertedCodes.get(valueOrPath).getConvertedCodes()) {
                                newTermbindingsMap.put(newCode, termbindingMap.get(valueOrPath));
                            }
                        } else {
                            //TODO: bound to at-code without automated conversion. Not sure if this should be an at, ac or id code
                            newTermbindingsMap.put(valueOrPath, termbindingMap.get(valueOrPath));
                        }
                    } else {
                        newTermbindingsMap.put(valueOrPath, termbindingMap.get(valueOrPath));
                    }
                }
            }
            archetype.getTerminology().setTermBindings(newTermBindings);
        }
    }

    private void generateMissingNodeIds(CObject cObject) {
        if(!(cObject instanceof CPrimitiveObject) && cObject.getNodeId() == null) {
            String path = cObject.getPath();

            cObject.setNodeId(archetype.generateNextIdCode());
            CreatedCode createdCode = new CreatedCode(cObject.getNodeId(), ReasonForCodeCreation.C_OBJECT_WITHOUT_NODE_ID);
            createdCode.setRmTypeName(cObject.getRmTypeName());
            createdCode.setPathCreated(path);
            this.createdCodes.put(cObject.getNodeId(), createdCode);
        }
        for(CAttribute attribute:cObject.getAttributes()) {
            generateMissingNodeIds(attribute);
        }
    }

    private void generateMissingNodeIds(CAttribute attribute) {
        for(CObject cObject:attribute.getChildren()) {
            generateMissingNodeIds(cObject);
        }
    }

    private void convert(CObject cObject) {

        if(cObject instanceof CComplexObject) {
            if (cObject instanceof CArchetypeRoot) {

            }
            calculateNewNodeId(cObject);
        } else if (cObject instanceof ArchetypeSlot) {
            calculateNewNodeId(cObject);
        } else if (cObject instanceof CComplexObjectProxy) {
            CComplexObjectProxy proxy = (CComplexObjectProxy) cObject;
            calculateNewNodeId(cObject);
            proxy.setTargetPath(convertPath(proxy.getTargetPath()));
        } else if (cObject instanceof CTerminologyCode) {
            convertCTerminologyCode((CTerminologyCode) cObject);
        }
        for(CAttribute attribute:cObject.getAttributes()) {
            convert(attribute);
        }
    }

    private void convertCTerminologyCode(CTerminologyCode cTerminologyCode) {
        if(cTerminologyCode.getConstraint() != null && !cTerminologyCode.getConstraint().isEmpty()) {
            String firstConstraint = cTerminologyCode.getConstraint().get(0);
            if(AOMUtils.isValueCode(firstConstraint)) {
                //local codes
                if(cTerminologyCode.getConstraint().size() == 1) {
                    //do not create a value set, just convert the code
                    String newCode = convertValueCode(firstConstraint);
                    this.addConvertedCode(firstConstraint, newCode);
                    cTerminologyCode.setConstraint(Lists.newArrayList(newCode));
                } else {
                    Set<String> localCodes = new LinkedHashSet<>();
                    for(String code:cTerminologyCode.getConstraint()) {
                        String newCode = convertValueCode(code);
                        this.addConvertedCode(code, newCode);
                        localCodes.add(newCode);
                    }

                    ValueSet valueSet = findOrCreateValueSet(cTerminologyCode.getArchetype(), localCodes, cTerminologyCode);
                    cTerminologyCode.setConstraint(Lists.newArrayList(valueSet.getId()));
                }
                if(cTerminologyCode.getAssumedValue() != null) {
                    TerminologyCode assumedValue = cTerminologyCode.getAssumedValue();
                    String oldCode = assumedValue.getCodeString();
                    String newCode = convertValueCode(oldCode);
                    assumedValue.setCodeString(newCode);
                }
            } else {
                if(cTerminologyCode.getConstraint().size() == 1) {
                    try {
                        //do not create a value set, create a code plus binding to the old non-local code
                        TerminologyCode termCode = TerminologyCode.createFromString(firstConstraint);
                        URI uri = new ADL14ConversionUtil().convertToUri(termCode);
                        Map<String, URI> termBindingsMap = archetype.getTerminology().getTermBindings().get(termCode.getTerminologyId());
                        if(termBindingsMap == null) {
                            termBindingsMap = new LinkedHashMap<>();
                            archetype.getTerminology().getTermBindings().put(termCode.getTerminologyId(), termBindingsMap);
                        }
                        //TODO: check if this is a converted or old term binding - old is unusual, but could be possible!
                        String existingTermBinding = findExistingTermBinding(uri, termBindingsMap);
                        if(existingTermBinding != null) {
                            cTerminologyCode.setConstraint(Lists.newArrayList(existingTermBinding));
                        } else {
                            String valueCode = archetype.generateNextValueCode();
                            termBindingsMap.put(valueCode, uri);
                            CreatedCode createdCode = new CreatedCode(valueCode, ReasonForCodeCreation.CREATED_VALUE_FOR_EXTERNAL_TERM);
                            createdCode.setOriginalTerm(termCode);
                            this.createdCodes.put(termCode.toString(), createdCode);
                            cTerminologyCode.setConstraint(Lists.newArrayList(valueCode));
                            for(String language:archetype.getTerminology().getTermDefinitions().keySet()) {
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

    private String findExistingTermBinding(URI uri, Map<String, URI> termBindingsMap) {
        for(Map.Entry<String, URI> existingBinding:termBindingsMap.entrySet()) {
            if(existingBinding.getValue().equals(uri) && !existingBinding.getKey().startsWith("/")) {
                return existingBinding.getKey();
            }
        }
        return null;
    }

    private ValueSet findOrCreateValueSet(Archetype archetype, Set<String> localCodes, CObject owningConstraint) {
        for(ValueSet valueSet:archetype.getTerminology().getValueSets().values()) {
            if(valueSet.getMembers().equals(localCodes)) {
                return valueSet;
            }
        }
        ValueSet valueSet = new ValueSet();
        valueSet.setMembers(localCodes);
        valueSet.setId(archetype.generateNextValueSetCode());
        this.createdCodes.put(valueSet.getId(), new CreatedCode(valueSet.getId(), ReasonForCodeCreation.CREATED_VALUE_SET));
        this.createdValueSets.put(valueSet.getId(), valueSet);
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

    /**
     * Get a term from the archetype terminology for a CObject that has the new node id, but with a yet unconverted
     * terminology, so using the newCodetoOldCodeMap to retrieve the old code first
     * @param owningConstraint
     */
    private ArchetypeTerm getTerm(String language, CObject owningConstraint) {
        CObject cObject = owningConstraint;
        while(cObject != null) {
            if (owningConstraint.getNodeId() != null) {
                String oldCode = newCodeToOldCodeMap.get(cObject.getNodeId());
                if(oldCode != null && archetype.getTerminology().getTermDefinition(language, oldCode) != null) {
                    return archetype.getTerminology().getTermDefinition(language, oldCode);
                }
            }
            cObject = cObject.getParent() == null ? null : cObject.getParent().getParent();
        }
        return null;
    }

    private void calculateNewNodeId(CObject cObject) {
        if(cObject.getNodeId() == null) {
            if(cObject instanceof CComplexObject || cObject instanceof CArchetypeRoot || cObject instanceof ArchetypeSlot) {
               //will be calculated later
            } else if(cObject instanceof CPrimitiveObject){
                //done automatically
            }
        } else {
            String oldNodeId = cObject.getNodeId();
            String newNodeId = convertNodeId(oldNodeId);
            addConvertedCode(oldNodeId, newNodeId);

            cObject.setNodeId(newNodeId);
        }
    }

    private void addConvertedCode(String oldNodeId, String newNodeId) {
        if(convertedCodes.containsKey(oldNodeId)) {
            convertedCodes.get(oldNodeId).addNewCode(newNodeId);
        } else {
            convertedCodes.put(oldNodeId, new ConvertedCodeResult(oldNodeId, newNodeId));
        }
        this.newCodeToOldCodeMap.put(newNodeId, oldNodeId);
    }

    private void updateTermDefinition(String oldCode, List<String> newCodes) {
        for(String language:archetype.getTerminology().getTermDefinitions().keySet()) {
            Map<String, ArchetypeTerm> terms = archetype.getTerminology().getTermDefinitions().get(language);
            ArchetypeTerm term = terms.remove(oldCode);
            for(String newCode:newCodes) {
                ArchetypeTerm newTerm = new ArchetypeTerm();
                newTerm.setCode(newCode);
                newTerm.setText(term.getText());
                newTerm.setDescription(term.getDescription());
                newTerm.putAll(term.getOtherItems());
                terms.put(newCode, term);
            }
        }
    }

    public String convertNodeId(String oldNodeId) {
        ConvertedCodeResult convertedCodeResult = convertedCodes.get(oldNodeId);
        if(convertedCodeResult != null && convertedCodeResult.hasIdCode()) {
          return convertedCodeResult.getIdCode();
        }
        return convertCode(oldNodeId, "id");
    }

    private String convertValueCode(String oldCode) {
        ConvertedCodeResult convertedCodeResult = convertedCodes.get(oldCode);
        if(convertedCodeResult != null && convertedCodeResult.hasValueCode()) {
            return convertedCodeResult.getValueCode();
        }
        return convertCode(oldCode, "at");
    }

    private String convertCode(String oldCode, String newCodePrefix) {
        NodeIdUtil nodeIdUtil = new NodeIdUtil(oldCode);
        if (nodeIdUtil.getCodes().isEmpty()) {
            return oldCode;
        }
        nodeIdUtil.setPrefix(newCodePrefix); //will automatically strip the leading zeroes due to integer-parsing
        nodeIdUtil.getCodes().set(0, nodeIdUtil.getCodes().get(0) + 1); //increment with 1, old is 0-based
        return nodeIdUtil.toString();
    }

    private void convert(CAttribute attribute) {
        for(CObject object:attribute.getChildren()) {
            convert(object);
        }
    }

    public String convertPath(String key) {
        APathQuery aPathQuery = new APathQuery(key);
        for(PathSegment segment:aPathQuery.getPathSegments()) {
            if(segment.getNodeId() != null) {
                segment.setNodeId(convertNodeId(segment.getNodeId()));
            }
        }
        return aPathQuery.toString();
    }
}
