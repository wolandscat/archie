package com.nedap.archie.json.flat;

import com.google.common.collect.Sets;
import com.nedap.archie.base.OpenEHRBase;
import com.nedap.archie.datetime.DateTimeSerializerFormatters;
import com.nedap.archie.rminfo.ModelInfoLookup;
import com.nedap.archie.rminfo.RMAttributeInfo;
import com.nedap.archie.rminfo.RMTypeInfo;

import java.lang.reflect.InvocationTargetException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FlatJsonGenerator {

    private final ModelInfoLookup modelInfoLookup;

    //TODO: this is model-specific, remove from here
    private final Set<String> ignoredFieldNames = Sets.newHashSet("archetype_details", "archetype_node_id");

    private final boolean writePipesForPrimitiveTypes;
    private final boolean humanReadableFormat;
    private final IndexNotation indexNotation;
    private final String typeIdPropertyName;


    public FlatJsonGenerator(ModelInfoLookup modelInfoLookup, FlatJsonFormatConfiguration config) {
        this.modelInfoLookup = modelInfoLookup;
        this.writePipesForPrimitiveTypes = config.isWritePipesForPrimitiveTypes();
        this.humanReadableFormat = config.isHumanReadableFormat();
        this.indexNotation = config.getIndexNotation();
        this.typeIdPropertyName = config.getTypeIdPropertyName();
    }

    public Map<String, Object> buildPathsAndValues(OpenEHRBase rmObject) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildPathsAndValuesInner(result,null, "/", rmObject);

        if(humanReadableFormat) {
            String rootName = modelInfoLookup.getNameFromRMObject(rmObject);
            if(rootName != null) {
                Map<String, Object> humanReadableResult = new LinkedHashMap<>();
                result.forEach((key, value) -> humanReadableResult.put(addUnderScores(rootName) + key, value));
                return humanReadableResult;
            }
        }
        return result;

    }

    private void buildPathsAndValuesInner(Map<String, Object> result, RMTypeInfo rmAttributeTypeInfo, String pathSoFar, OpenEHRBase rmObject) {

        if(rmObject == null) {
            return;
        }
        RMTypeInfo typeInfo = modelInfoLookup.getTypeInfo(rmObject.getClass());
        if(pathSoFar.equalsIgnoreCase ("/") || (typeHasDescendants(rmAttributeTypeInfo) && !sameType(rmAttributeTypeInfo, rmObject))) {
            result.put(joinPath(pathSoFar, typeIdPropertyName, null, null, "/"), getTypeIdFromValue(rmObject));
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
            return true;// we have no idea, so include @type/_type
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
            String newPath = joinPath(pathSoFar, attributeName, null, index, writePipesForPrimitiveTypes ? "|" : "/");

            if(child instanceof Number) {
                result.put(newPath, child);
            } else if (child instanceof TemporalAccessor) {
                Temporal t = (Temporal) child;
                boolean hoursSupported = t.isSupported(ChronoUnit.HOURS);
                boolean monthsSupported = t.isSupported(ChronoUnit.MONTHS);

                if(hoursSupported && monthsSupported) {
                    //datetime
                    result.put(newPath, DateTimeSerializerFormatters.ISO_8601_DATE_TIME.format(t));
                } else if (monthsSupported) {
                    //date
                    result.put(newPath, DateTimeSerializerFormatters.ISO_8601_DATE.format(t));
                } else if (hoursSupported) {
                    //time
                    result.put(newPath, DateTimeSerializerFormatters.ISO_8601_TIME.format(t));
                }
            } else if (child instanceof TemporalAmount) {
                //duration or period. now just a toString, should this be a specific formatter?
                result.put(newPath, child);
            } else {
                result.put(newPath, child.toString());
            }
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
        String name = modelInfoLookup.getNameFromRMObject(rmObject);
        boolean wroteHumanReadableName = name != null && humanReadableFormat;
        String newPathSegment = wroteHumanReadableName ? addUnderScores(name) : attributeName;
        String nodeId = modelInfoLookup.getArchetypeNodeIdFromRMObject(rmObject);

        if(nodeId != null && !wroteHumanReadableName) {

            if(indexNotation == IndexNotation.AFTER_A_COLON) {
                newPathSegment = newPathSegment + "[" + nodeId + "]";
                if (index != null) {
                    newPathSegment = newPathSegment + ":" + index;
                }
            } else {
                if(index == null) {
                    newPathSegment = newPathSegment + "[" + nodeId + "]";
                } else {
                    newPathSegment = newPathSegment + "[" + nodeId + "," + index + "]";
                }
            }
        } else if (index != null) {
            if(indexNotation == IndexNotation.AFTER_A_COLON) {
                newPathSegment = newPathSegment + ":" + index;
            } else {
                newPathSegment = newPathSegment + "[" + index + "]";
            }
        }

        if(pathSoFar.endsWith("/")) {
            return pathSoFar + newPathSegment;
        }
        return pathSoFar + pathSeparator + newPathSegment;
    }

    private String addUnderScores(String name) {
        if(name == null) {
            return null;
        }
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }
}

