package com.nedap.archie.adl14;

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
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.aom.utils.NodeIdUtil;
import com.nedap.archie.paths.PathSegment;
import com.nedap.archie.paths.PathUtil;
import com.nedap.archie.query.APathQuery;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ADL14NodeIDConverter {

    private final Archetype archetype;
    private final Archetype flatParentArchetype;

    private final ADL14ConversionConfiguration conversionConfiguration;

    private final ADL14TermConstraintConverter termConstraintConverter;
    private final PreviousConversionApplier previousConversionApplier;


    /**
     * Contains a mapping between the adl 1.4 codes and the generated adl 2 code
     */
    private Map<String, ConvertedCodeResult> convertedCodes = new LinkedHashMap<>();
    private Map<String, CreatedCode> createdCodes = new LinkedHashMap<>();
    private Map<String, ValueSet> createdValueSets = new LinkedHashMap<>();

    private Map<String, String> newCodeToOldCodeMap = new LinkedHashMap<>();



    public ADL14NodeIDConverter(Archetype archetype, Archetype flatParentArchetype) {
        this(archetype, flatParentArchetype, new ADL14ConversionConfiguration(), null);
    }

    public ADL14NodeIDConverter(Archetype archetype, Archetype flatParentArchetype, ADL14ConversionConfiguration configuration, ADL2ConversionLog oldLog) {
        this.conversionConfiguration = configuration;
        this.archetype = archetype;
        this.flatParentArchetype = flatParentArchetype;
        this.termConstraintConverter = new ADL14TermConstraintConverter(this, archetype, flatParentArchetype);
        this.previousConversionApplier = new PreviousConversionApplier(this, archetype, oldLog);
    }


    public ADL14ConversionConfiguration getConversionConfiguration() {
        return conversionConfiguration;
    }

    public ADL2ConversionLog convert() {

        convert(archetype.getDefinition());
        if(previousConversionApplier != null) {
            //tricky stuff here:
            //apply the previous conversion. This does 3 things:
            //1. add synthesized id codes in the same was as before
            //2. create previously synthesized term codes plus binding again
            //3. create previously created value sets again
            //These processes are not tricky in itself, but in what effect it has on the rest of the conversion process
            //for example, if you add an at-code to the term bindings here, it must not be converted again
            //this is done by keeping a log of converted codes in this class, and not converting any term bindings
            //unless they are in the converted codes.
            previousConversionApplier.addCreatedCodes();
            previousConversionApplier.addValueSets();
            //TODO: remove all unused value sets and term bindings created here
        }
        termConstraintConverter.convert();
        convertTermBindings(archetype);
        generateMissingNodeIds(archetype.getDefinition());

        convertTermDefinitions();
        previousConversionApplier.removeCreatedUnusedTermCodesAndValueSets();
        return new ADL2ConversionLog(/*convertedCodes*/ null, createdCodes, createdValueSets);
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
            if(archetype.getParentArchetypeId() != null && flatParentArchetype != null) {
                //TODO: get the matching path in the parent archetype id. Find this node
                //if found, this is a specialization of said node and needs to be checked for differences and/or
                //given the same node id
                //if not found, generate/synthesize a new node id.
                String parentPath = AOMUtils.pathAtSpecializationLevel(cObject.getPathSegments(), archetype.specializationDepth()-1);
                System.out.println("path: " + path + " parent path " + parentPath);
                CAttribute cAttribute = flatParentArchetype.itemAtPath(parentPath);
                if(cAttribute != null) {
                    List<CObject> childrenWithMatchingChildName = cAttribute.getChildrenWithMatchingChildName(cObject.getRmTypeName());
                    if(childrenWithMatchingChildName.size() > 0) {
                        //TODO: this is way too simple of course :)
                        cObject.setNodeId(childrenWithMatchingChildName.get(0).getNodeId());
                    } else {
                        synthesizeNodeId(cObject, path);
                    }
                } else {
                    synthesizeNodeId(cObject, path);
                }

            } else {
                synthesizeNodeId(cObject, path);
            }
        }
        for(CAttribute attribute:cObject.getAttributes()) {
            generateMissingNodeIds(attribute);
        }
    }

    private void synthesizeNodeId(CObject cObject, String path) {
        cObject.setNodeId(archetype.generateNextIdCode());
        CreatedCode createdCode = new CreatedCode(cObject.getNodeId(), ReasonForCodeCreation.C_OBJECT_WITHOUT_NODE_ID);
        createdCode.setRmTypeName(cObject.getRmTypeName());
        createdCode.setPathCreated(path);
        addCreatedCode(cObject.getNodeId(), createdCode);
    }

    protected void addCreatedCode(String code, CreatedCode createdCode) {
        this.createdCodes.put(code, createdCode);
    }

    protected void addCreatedValueSet(String nodeId, ValueSet valueSet) {
        this.createdValueSets.put(nodeId, valueSet);
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
        }
        for(CAttribute attribute:cObject.getAttributes()) {
            convert(attribute);
        }
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

    protected void addConvertedCode(String oldNodeId, String newNodeId) {
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
            if(term != null) {
                for (String newCode : newCodes) {
                    ArchetypeTerm newTerm = new ArchetypeTerm();
                    newTerm.setCode(newCode);
                    newTerm.setText(term.getText());
                    newTerm.setDescription(term.getDescription());
                    newTerm.putAll(term.getOtherItems());
                    terms.put(newCode, term);
                }
            } else {
                //TODO
                System.out.println("hmm, this is weird, cannot find term for code " + oldCode);
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

    protected String convertValueCode(String oldCode) {
        ConvertedCodeResult convertedCodeResult = convertedCodes.get(oldCode);
        if(convertedCodeResult != null && convertedCodeResult.hasValueCode()) {
            return convertedCodeResult.getValueCode();
        }
        return convertCode(oldCode, "at");
    }

    public String convertValueSetCode(String oldCode) {
        ConvertedCodeResult convertedCodeResult = convertedCodes.get(oldCode);
        if(convertedCodeResult != null && convertedCodeResult.hasValueCode()) {
            return convertedCodeResult.getValueCode();
        }
        return convertCode(oldCode, "ac");
    }

    private String convertCode(String oldCode, String newCodePrefix) {
        NodeIdUtil nodeIdUtil = new NodeIdUtil(oldCode);
        if (nodeIdUtil.getCodes().isEmpty()) {
            return oldCode;
        }
        nodeIdUtil.setPrefix(newCodePrefix); //will automatically strip the leading zeroes due to integer-parsing
        if(!oldCode.startsWith("at0.")) { //TODO: check if correct! this is a specialized code, can it be at0000.X? Or is that the root code?
            nodeIdUtil.getCodes().set(0, nodeIdUtil.getCodes().get(0) + 1); //increment with 1, old is 0-based
        }
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

    public String getOldCodeForNewCode(String nodeId) {
        return this.newCodeToOldCodeMap.get(nodeId);
    }

    protected Archetype getFlatParentArchetype() {
        return flatParentArchetype;
    }

}
