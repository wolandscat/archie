package com.nedap.archie.adl14;

import com.google.common.collect.Lists;
import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.adl14.log.CreatedCode;
import com.nedap.archie.adl14.log.ReasonForCodeCreation;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.ArchetypeModelObject;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ValueSet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Takes the ConversionLog from a previous run and applies the changes that where applied there.
 *
 * Preconditions: an archetype that has it's node-ids already converted, but no synthesized node ids present
 * and no CTerminologyCodes processed, with the terminology still intact.
 */
public class PreviousConversionApplier {

    private ADL14NodeIDConverter converter;
    private final Archetype archetype;
    private final ADL2ConversionLog conversionLog;

    public PreviousConversionApplier(ADL14NodeIDConverter converter, Archetype archetype, ADL2ConversionLog conversionLog) {
        this.converter = converter;
        this.archetype = archetype;
        this.conversionLog = conversionLog;
    }

    public void addCreatedCodes() {
        if(this.conversionLog == null) {
            return;
        }

        if(conversionLog.getCreatedCodes() != null) {
            for (CreatedCode createdCode : conversionLog.getCreatedCodes().values()) {
                switch (createdCode.getReasonForCreation()) {
                    case C_OBJECT_WITHOUT_NODE_ID:
                        applyPreviousSynthesizedCode(createdCode);
                        break;
                    case CREATED_VALUE_SET:
                        //will be done elsewhere, since here the members are unknown
                        break;
                    case CREATED_VALUE_FOR_EXTERNAL_TERM:
                        applyPreviousTermCodeCreation(createdCode);
                        break;
                }
            }
        }
    }

    public void addValueSets() {
        if(this.conversionLog == null) {
            return;
        }
        if(conversionLog.getCreatedValueSets() != null && !conversionLog.getCreatedValueSets().isEmpty()) {
            if(archetype.getTerminology().getValueSets() == null) {
                archetype.getTerminology().setValueSets(new LinkedHashMap<>());
            }
            Map<String, ValueSet> valueSets = archetype.getTerminology().getValueSets();
            valueSets.putAll(conversionLog.getCreatedValueSets());
        }
    }

    private void applyPreviousTermCodeCreation(CreatedCode createdCode) {
        try {
            Map<String, URI> termBindingsMap = archetype.getTerminology().getTermBindings().get(createdCode.getOriginalTerm().getTerminologyId());
            URI uri = new ADL14ConversionUtil().convertToUri(createdCode.getOriginalTerm());
            if (termBindingsMap == null) {
                termBindingsMap = new LinkedHashMap<>();
                archetype.getTerminology().getTermBindings().put(createdCode.getOriginalTerm().getTerminologyId(), termBindingsMap);
            }
            //TODO: check if this is a converted or old term binding - old is unusual, but could be possible!
            String existingTermBinding = ADL14ConversionUtil.findExistingTermBinding(archetype, uri, termBindingsMap);
            if (existingTermBinding == null) {
                String valueCode = createdCode.getGeneratedCode();
                termBindingsMap.put(valueCode, uri);
                //make sure the next conversion log will get the same value again
                CreatedCode newCreatedCode = new CreatedCode(valueCode, ReasonForCodeCreation.CREATED_VALUE_FOR_EXTERNAL_TERM);
                newCreatedCode.setOriginalTerm(createdCode.getOriginalTerm());
                converter.addCreatedCode(createdCode.getOriginalTerm().toString(), createdCode);

                for (String language : archetype.getTerminology().getTermDefinitions().keySet()) {
                    ArchetypeTerm term = new ArchetypeTerm();
                    term.setCode(valueCode);
                    term.setText("Term binding for " + createdCode.getGeneratedCode() + ", translation not known in ADL 1.4 -> ADL 2 converter");
                    term.setDescription(term.getText());
                    archetype.getTerminology().getTermDefinitions().get(language).put(valueCode, term);
                }
            }
        } catch (URISyntaxException e) {
            //TODO: warn? or add generated URL to CreatedCode?
        }
    }

    private void applyPreviousSynthesizedCode(CreatedCode createdCode) {
        ArchetypeModelObject object = archetype.itemAtPath(createdCode.getPathCreated());
        if(object == null) {
            //throw new IllegalArgumentException("path in previously converted node id points null");
            //TODO: warn.
        } else if(!(object instanceof CAttribute)) {
            //this MUST be an attribute, so something is wrong
            throw new IllegalArgumentException("path in previously converted node id points to a CObject, must be a CAttribute");
        } else if (createdCode.getRmTypeName() == null) {
            throw new IllegalArgumentException("When applying a previously synthesized node id, the rm type name field must be present");
        } else {
            CAttribute attribute = (CAttribute) object;
            int index = attribute.getIndexOfChildWithMatchingRmTypeName(createdCode.getRmTypeName());
            if(index >= 0) {
                CObject cObject = attribute.getChildren().get(index);
                cObject.setNodeId(createdCode.getGeneratedCode());
            } else {
                //TODO: warn. has been deleted, can be fine, can be less than fine :)
            }
            return;
        }
    }
}
