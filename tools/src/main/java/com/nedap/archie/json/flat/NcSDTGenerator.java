package com.nedap.archie.json.flat;

import com.google.common.collect.Sets;
import com.nedap.archie.base.OpenEHRBase;
import com.nedap.archie.rminfo.ModelInfoLookup;
import com.nedap.archie.rminfo.RMAttributeInfo;
import com.nedap.archie.rminfo.RMTypeInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NcSDTGenerator {

    private final ModelInfoLookup modelInfoLookup;
    //TODO: this is model-specific, remove from here
    private Set<String> ignoredFieldNames = Sets.newHashSet("archetype_details", "archetype_node_id");
    private final boolean writePipesForPrimitiveTypes;


    public NcSDTGenerator(ModelInfoLookup modelInfoLookup, boolean writePipesForPrimitiveTypes) {
        this.modelInfoLookup = modelInfoLookup;
        this.writePipesForPrimitiveTypes = writePipesForPrimitiveTypes;
    }

    public Map<String, Object> buildPathsAndValues(OpenEHRBase rmObject) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildPathsAndValuesInner(result,null, "/", rmObject);
        return result;

    }

    private void buildPathsAndValuesInner(Map<String, Object> result, RMTypeInfo rmAttributeTypeInfo, String pathSoFar, OpenEHRBase rmObject) {

        if(rmObject == null) {
            return;
        }
        RMTypeInfo typeInfo = modelInfoLookup.getTypeInfo(rmObject.getClass());
        if(pathSoFar.equalsIgnoreCase ("/") || (typeHasDescendants(rmAttributeTypeInfo) && !sameType(rmAttributeTypeInfo, rmObject))) {
            result.put(joinPath(pathSoFar, "@type", null, null, "/"), getTypeIdFromValue(rmObject));
        }

        for(String attributeName:typeInfo.getAttributes().keySet()) {
            RMAttributeInfo attributeInfo = typeInfo.getAttributes().get(attributeName);
            if(!attributeInfo.isComputed() && !ignoredFieldNames.contains(attributeName) && attributeInfo.getGetMethod() != null) {
                try {
                    Object child = attributeInfo.getGetMethod().invoke(rmObject);
                    addAttribute(result, pathSoFar, rmObject, child, attributeName,null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);//TODO: fine for now...
                }
            }

        }
    }

    private Object getTypeIdFromValue(OpenEHRBase value) {
        RMTypeInfo typeInfo = modelInfoLookup.getTypeInfo(value.getClass());

        if(typeInfo != null) {
            //this case is faster and should always work. If for some reason it does not, the case below works fine instead.
            return typeInfo.getRmName();
        } else {
            return modelInfoLookup.getNamingStrategy().getTypeName(value.getClass());
        }
    }

    private boolean sameType(RMTypeInfo typeInfo, OpenEHRBase rmObject) {
        if(rmObject == null) {
            return false;
        }
        return modelInfoLookup.getTypeInfo(rmObject.getClass()).equals(typeInfo);
    }

    private boolean typeHasDescendants(RMTypeInfo typeInfo) {
        if(typeInfo == null) {
            return true;// we have no idea, so include @type
        }
        return !typeInfo.getDirectDescendantClasses().isEmpty();
    }

    private void addAttribute(Map<String, Object> result, String pathSoFar, OpenEHRBase parent, Object child, String attributeName, Integer index)  {

        if(child instanceof OpenEHRBase) {
            String newPath = joinPath(pathSoFar, attributeName, (OpenEHRBase) child, index, "/");
            //TODO: get correct type info here
            RMAttributeInfo attributeInfo = modelInfoLookup.getAttributeInfo(parent.getClass(), attributeName);
            RMTypeInfo typeInfo = getAttributeTypeInfo(attributeInfo);
            buildPathsAndValuesInner(result, typeInfo, newPath, (OpenEHRBase) child);

            String archetypeId = modelInfoLookup.getArchetypeIdFromArchetypedRmObject(child);
            if(archetypeId != null) {
                result.put(pathSoFar, archetypeId);
            }
        } else if (child instanceof Collection) {

            Map<String, Integer> amountsPerNodeId = new HashMap<>();
            for(Object c: (Collection) child) {

                int numberOfNonLocatables = 1; //1-based, sory
                String archetypeNodeId = modelInfoLookup.getArchetypeNodeIdFromRMObject(c);
                if(archetypeNodeId != null) {
                    Integer numberOfPreviousOccurrences = amountsPerNodeId.get(archetypeNodeId);
                    addAttribute(result, pathSoFar, parent, c, attributeName, numberOfPreviousOccurrences);
                    numberOfPreviousOccurrences = numberOfPreviousOccurrences == null ? 1: numberOfPreviousOccurrences + 1;
                    amountsPerNodeId.put(archetypeNodeId, numberOfPreviousOccurrences);
                } else {
                    addAttribute(result, pathSoFar, parent, c, attributeName, numberOfNonLocatables == 1 ? null: numberOfNonLocatables);
                    numberOfNonLocatables++;
                }
            }
            //TODO: do we need Map-support as well?
        } else if(child != null) {
            String newPath = joinPath(pathSoFar, attributeName, null, index, this.writePipesForPrimitiveTypes ? "|" : "/");
            //not an RMObject, likely a primitive type or something with a good toString option, so do that
            result.put(newPath, child.toString());
        }
    }

    private RMTypeInfo getAttributeTypeInfo(RMAttributeInfo attributeInfo) {
        RMTypeInfo typeInfo = null;
        if(attributeInfo != null) {
            typeInfo = modelInfoLookup.getTypeInfo(attributeInfo.getTypeInCollection());
        }
        return typeInfo;
    }

    private String joinPath(String pathSoFar, String attributeName, OpenEHRBase rmObject, Integer index, String pathSeparator) {
        String newPathSegment = attributeName;
        String nodeId = modelInfoLookup.getArchetypeNodeIdFromRMObject(rmObject);

        if(nodeId != null) {
            newPathSegment = newPathSegment + "[" + nodeId + "]";
            if(index != null) {
                newPathSegment = newPathSegment + ":" + index;
            }
        } else if (index != null) {
            newPathSegment = newPathSegment + ":" + index;
        }

        if(pathSoFar.endsWith("/")) {
            return pathSoFar + newPathSegment;
        }
        return pathSoFar + pathSeparator + newPathSegment;
    }
}

