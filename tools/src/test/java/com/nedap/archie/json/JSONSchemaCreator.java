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
import java.util.function.Supplier;

public class JSONSchemaCreator {

    private Map<String, Supplier<JSONObject>> primitiveTypeMapping;
    private List<String> rootTypes;
    private BmmModel bmmModel;

    public JSONSchemaCreator() {
        primitiveTypeMapping = new HashMap<>();
        primitiveTypeMapping.put("integer", () -> createType("integer"));
        primitiveTypeMapping.put("integer64", () -> createType("integer"));
        primitiveTypeMapping.put("boolean", () -> createType("boolean"));
        primitiveTypeMapping.put("real", () -> createType("number"));
        primitiveTypeMapping.put("double", () -> createType("number"));
        primitiveTypeMapping.put("octet", () -> createType("string"));//well, base64...
        primitiveTypeMapping.put("byte", () -> createType("string"));
        primitiveTypeMapping.put("character", () -> createType("string"));
        primitiveTypeMapping.put("hash", () -> createType("object"));
        primitiveTypeMapping.put("string", () -> createType("integer"));
        primitiveTypeMapping.put("iso8601_date", () -> createType("integer").put("format", "date"));
        primitiveTypeMapping.put("iso8601_date_time", () -> createType("integer").put("format", "date-time"));
        primitiveTypeMapping.put("iso8601_time", () -> createType("integer").put("format", "time"));
        primitiveTypeMapping.put("iso8601_duration", () -> createType("string"));
        primitiveTypeMapping.put("proportion_kind", () -> createType("integer"));//TODO: proper enum support

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
                .put("allOf", allOfArray)
                .put("$schema", "http://json-schema.org/draft-07/schema");


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
            if((bmmClass.getTypeName().startsWith("POINT_EVENT") || bmmClass.getTypeName().startsWith("INTERVAL_EVENT")) &&
                    propertyName.equalsIgnoreCase("data")) {
                //we don't handle generics yet, and it's very tricky with the current BMM indeed. So, just manually hack this
                JSONObject propertyDef = createPolymorphicReference(bmmModel.getClassDefinition("ITEM_STRUCTURE"));
                extendPropertyDef(propertyDef, bmmProperty);
                properties.put(propertyName, propertyDef);

                if (bmmProperty.getMandatory()) {
                    required.put(propertyName);
                }
            } else {

                JSONObject propertyDef = createPropertyDef(bmmProperty.getType());
                extendPropertyDef(propertyDef, bmmProperty);
                properties.put(propertyName, propertyDef);

                if (bmmProperty.getMandatory()) {
                    required.put(propertyName);
                }
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
                return getJSPrimitive(type);
            } else {
                return createPolymorphicReference(type.getBaseClass());
            }
        } else if (type instanceof BmmContainerType) {

            BmmContainerType containerType = (BmmContainerType) type;
            return new JSONObject()
                .put("type", "array")
                .put("items", createPropertyDef(containerType.getBaseType()));
        } else if (type instanceof BmmGenericType) {
            if(isJSPrimitive(type)) {
                return getJSPrimitive(type);
            } else {
                return createPolymorphicReference(type.getBaseClass());
            }

        }
        throw new IllegalArgumentException("type must be a BmmType, but was " + type.getClass().getSimpleName());

    }

    private JSONObject createPolymorphicReference(BmmClass type) {

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
                JSONObject typePropertyCheck = createConstType(descendant);
                JSONObject typeCheck = new JSONObject().put("properties", typePropertyCheck);

                JSONObject typeReference = createReference(descendant);
                //IF the type matches
                //THEN check the correct type from the definitions
                JSONObject ifObject = new JSONObject()
                        .put("if", typeCheck)
                        .put("then", typeReference);
                array.put(ifObject);

            }
            array.put(createRequiredArray("_type"));
            def.put("allOf", array);
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

    private JSONObject getJSPrimitive(BmmType bmmType) {
        return primitiveTypeMapping.get(bmmType.getTypeName().toLowerCase()).get();
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
        return new JSONObject().put("$ref", "#/definitions/" + rootType);
    }
}
