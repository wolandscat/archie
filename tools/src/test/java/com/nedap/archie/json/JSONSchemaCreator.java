package com.nedap.archie.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openehr.bmm.core.BmmClass;
import org.openehr.bmm.core.BmmContainerProperty;
import org.openehr.bmm.core.BmmContainerType;
import org.openehr.bmm.core.BmmGenericType;
import org.openehr.bmm.core.BmmModel;
import org.openehr.bmm.core.BmmOpenType;
import org.openehr.bmm.core.BmmProperty;
import org.openehr.bmm.core.BmmSimpleType;
import org.openehr.bmm.core.BmmType;
import org.openehr.bmm.persistence.validation.BmmDefinitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONSchemaCreator {

    private Map<String, String> primitiveTypeMapping;
    private List<String> rootTypes;
    private BmmModel bmmModel;

    public JSONSchemaCreator() {
        primitiveTypeMapping = new HashMap<>();
        primitiveTypeMapping.put("integer", "integer");
        primitiveTypeMapping.put("integer64", "integer");
        primitiveTypeMapping.put("boolean", "boolean");
        primitiveTypeMapping.put("real", "number");
        primitiveTypeMapping.put("double", "number");
        primitiveTypeMapping.put("octet", "string");
        primitiveTypeMapping.put("byte", "string");
        primitiveTypeMapping.put("character", "string");
        primitiveTypeMapping.put("hash", "object");
        primitiveTypeMapping.put("string", "string");
        primitiveTypeMapping.put("iso8601_date", "string");
        primitiveTypeMapping.put("iso8601_date_time", "string");
        primitiveTypeMapping.put("iso8601_time", "string");
        primitiveTypeMapping.put("ISO8601_duration", "string");

        rootTypes = new ArrayList<>();
        rootTypes.add("COMPOSITION");
        rootTypes.add("OBSERVATION");
        rootTypes.add("EVALUATION");
        rootTypes.add("ACTIVITY");
        rootTypes.add("ACTION");
        rootTypes.add("SECTION");
        rootTypes.add("INSTRUCTION");
        rootTypes.add("INSTRUCTION_DETAILS");
        rootTypes.add("ADMIN_ENTRY");
        rootTypes.add("CLUSTER");
        rootTypes.add("CAPABILITY");
        rootTypes.add("PERSON");
        rootTypes.add("ADDRESS");
        rootTypes.add("ROLE");
        rootTypes.add("ORGANISATION");
        rootTypes.add("PARTY_IDENTITY");
        rootTypes.add("ITEM_TREE");
    }

    public JSONObject create(BmmModel bmm) {
        this.bmmModel = bmm;

        //create the definitions and the root if/else base

        JSONArray allOfArray = new JSONArray();
        JSONObject definitions = new JSONObject();
        JSONObject schemaRoot = new JSONObject()
                .put("definitions", definitions)
                .put("allOf", allOfArray);


        //at the root level, require the type
        JSONObject typeRequired = createRequiredArray("_type");
        allOfArray.put(typeRequired);

        //for every root type, if the type is right, check that type
        //anyof does more or less the same, but this is faster plus it gives MUCH less errors!
        for(String rootType:rootTypes) {

            JSONObject typePropertyCheck = createConstType(rootType);
            JSONObject typeCheck = new JSONObject().put("properties", typePropertyCheck);

            JSONObject typeReference = createReference(rootType);
            //IF the type matches
            //THEN check the correct type from the definitions
            JSONObject ifObject = new JSONObject()
                    .put("if", typeCheck)
                    .put("then", typeReference);
            allOfArray.put(ifObject);
        }
        for(BmmClass bmmClass: bmm.getClassDefinitions().values()) {
            if (!bmmClass.isAbstract() && !primitiveTypeMapping.containsKey(bmmClass.getTypeName().toLowerCase())) {
                addClass(definitions, bmmClass);
            }
        }
        return schemaRoot;
    }

    private void addClass(JSONObject definitions, BmmClass bmmClass) {
        String typeName = BmmDefinitions.typeNameToClassKey(bmmClass.getTypeName());

        JSONObject definition = new JSONObject();
        definition.put("type", "object");
        BmmClass flatBmmClass = bmmClass.flattenBmmClass();
        JSONArray required = new JSONArray();
        JSONObject properties = new JSONObject();
        for (String propertyName : flatBmmClass.getProperties().keySet()) {
            BmmProperty bmmProperty = flatBmmClass.getProperties().get(propertyName);
            JSONObject propertyDef = createPropertyDef(bmmProperty.getType());
            extendPropertyDef(propertyDef, bmmProperty);
            properties.put(propertyName, propertyDef);

            if (bmmProperty.getMandatory()) {
                required.put(propertyName);
            }
        }
        properties.put("_type", new JSONObject().put("const", typeName));
        definition.put("required", required);
        definition.put("properties", properties);
        definitions.put(typeName, definition);
    }

    private void extendPropertyDef(JSONObject propertyDef, BmmProperty bmmProperty) {
        if(bmmProperty instanceof BmmContainerProperty) {
            BmmContainerProperty containerProperty = (BmmContainerProperty) bmmProperty;
            if(containerProperty.getCardinality() != null && containerProperty.getCardinality().getLower() > 0) {
                propertyDef.put("minItems", containerProperty.getCardinality().getLower());
            }
        }
    }

    private JSONObject createPropertyDef(BmmType type) {


        if(type instanceof BmmOpenType) {
            return createType("object");
            //nothing more to be done
        } else if (type instanceof BmmSimpleType) {
            if(isJSPrimitive(type)) {
                return createType(getJSPrimitive(type));
            } else {
                return createPolymorphicReference(type.getBaseClass());
            }
        } else if (type instanceof BmmContainerType) {

            BmmContainerType containerType = (BmmContainerType) type;
            return new JSONObject()
                .put("type", "array")
                .put("items", createPropertyDef(containerType.getBaseType()));
        } else if (type instanceof BmmGenericType) {

            return createPolymorphicReference(type.getBaseClass());

        }
        throw new IllegalArgumentException("type must be a BmmType, but was " + type.getClass().getSimpleName());

    }

    private JSONObject createPolymorphicReference(BmmClass type) {

        if(BmmDefinitions.typeNameToClassKey(type.getTypeName()).equalsIgnoreCase("hash")) {
            return createType("object");
        }

        List<String> descendants = getAllNonAbstractDescendants( type);
        if(!type.isAbstract()) {
            descendants.add(BmmDefinitions.typeNameToClassKey(type.getTypeName()));
        }

        if(descendants.isEmpty()) {
            //this is an object of which only an abstract class exists.
            //it cannot be represented as standard json, one would think. this is mainly access control and authored
            //resource in the RM
            return createType("object");
        } else if (descendants.size() > 1) {
            JSONObject def = new JSONObject();
            JSONArray array = new JSONArray();
            for(String descendant:descendants) {

                JSONArray allOf = new JSONArray();

                JSONObject ref = createReference(descendant);
                allOf.put(ref);
                JSONObject requiredType = createRequiredArray("_type");
                allOf.put(requiredType);
                JSONObject allOfContainer = new JSONObject();
                allOfContainer.put("allOf", allOf);
                array.put(allOfContainer);
            }
            def.put("anyOf", array);
            return def;
        } else {
            return createReference(BmmDefinitions.typeNameToClassKey(type.getTypeName()));
        }

    }


    private List<String> getAllNonAbstractDescendants(BmmClass bmmClass) {
        List<String> result = new ArrayList<>();
        List<String> descs = bmmClass.getImmediateDescendants();
        for(String desc:descs) {
            if(!bmmClass.getTypeName().equalsIgnoreCase(desc)) {//TODO: fix getImmediateDescendants in BMM so this check is not required
                BmmClass classDefinition = bmmModel.getClassDefinition(desc);
                if (!classDefinition.isAbstract()) {
                    result.add(BmmDefinitions.typeNameToClassKey(classDefinition.getTypeName()));
                }
                result.addAll(getAllNonAbstractDescendants(classDefinition));
            }
        }
        return result;
    }

    private boolean isJSPrimitive(BmmType bmmType) {
        return primitiveTypeMapping.containsKey(bmmType.getTypeName().toLowerCase());
    }

    private String getJSPrimitive(BmmType bmmType) {
        return primitiveTypeMapping.get(bmmType.getTypeName().toLowerCase());
    }

    private JSONObject createConstType(String rootType) {
        JSONObject constTypeObject = new JSONObject().put("const", rootType);
        return new JSONObject().put("_type", constTypeObject);
    }

    private JSONObject createRequiredArray(String... requiredFields) {
        JSONObject requiredType = new JSONObject();
        JSONArray requiredArray = new JSONArray();
        for(String requiredProperty: requiredFields) {
            requiredArray.put(requiredProperty);
        }
        requiredType.put("required", requiredArray);
        return requiredType;
    }


    private JSONObject createType(String jsPrimitive) {
        return new JSONObject().put("type", jsPrimitive);
    }

    private JSONObject createReference(String rootType) {
        return new JSONObject().put("$ref", rootType);
    }
}
