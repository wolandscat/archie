package com.nedap.archie.json;

import com.nedap.archie.aom.utils.AOMUtils;
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
//        rootTypes.add("COMPOSITION");
        rootTypes.add("OBSERVATION");
//        rootTypes.add("EVALUATION");
//        rootTypes.add("ACTIVITY");
//        rootTypes.add("ACTION");
//        rootTypes.add("SECTION");
//        rootTypes.add("INSTRUCTION");
//        rootTypes.add("INSTRUCTION_DETAILS");
//        rootTypes.add("ADMIN_ENTRY");
//        rootTypes.add("CLUSTER");
//        rootTypes.add("CAPABILITY");
//        rootTypes.add("PERSON");
//        rootTypes.add("ADDRESS");
//        rootTypes.add("ROLE");
//        rootTypes.add("ORGANISATION");
//        rootTypes.add("PARTY_IDENTITY");
//        rootTypes.add("ITEM_TREE");
    }

    public JSONObject create(BmmModel bmm) {
        this.bmmModel = bmm;

        JSONObject schemaRoot = new JSONObject();
        JSONArray anyOfArray = new JSONArray();


        JSONObject definitions = new JSONObject();
        schemaRoot.put("definitions", definitions);
        schemaRoot.put("anyOf", anyOfArray);
        for(String rootType:rootTypes) {

            JSONObject anyOfObject = new JSONObject();
            addReference(rootType, anyOfObject);
            anyOfArray.put(anyOfObject);
        }
        for(BmmClass bmmClass: bmm.getClassDefinitions().values()) {
            if (!bmmClass.isAbstract() && !primitiveTypeMapping.containsKey(bmmClass.getTypeName().toLowerCase())) {
                addClass(anyOfArray, definitions, bmmClass);
            }
        }
        return schemaRoot;
    }

    private void addClass(JSONArray anyOfArray, JSONObject definitions, BmmClass bmmClass) {
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
        JSONObject typeProperty = new JSONObject();
        typeProperty.put("const", typeName);
        properties.put("_type", typeProperty);
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
        JSONObject def = new JSONObject();

        if(type instanceof BmmOpenType) {
            System.out.println("open");
            def.put("type", "object");
            //nothing more to be done
        } else if (type instanceof BmmSimpleType) {
            if(isJSPrimitive(type)) {
                def.put("type", getJSPrimitive(type));
            } else {
                createPolymorphicReference(def, type.getBaseClass());
            }
        } else if (type instanceof BmmContainerType) {
            BmmContainerType containerType = (BmmContainerType) type;
            def.put("type", "array");
            def.put("items", createPropertyDef(containerType.getBaseType()));
            //System.out.println("container");
        } else if (type instanceof BmmGenericType) {

            createPolymorphicReference(def, type.getBaseClass());
            //base class is what we need
            //it can be hash!
        }

        return def;
    }

    private void createPolymorphicReference(JSONObject def, BmmClass type) {
        if(BmmDefinitions.typeNameToClassKey(type.getTypeName()).equalsIgnoreCase("hash")) {
            def.put("type", "object");
            return;
        }

        List<String> descendants = getAllNonAbstractDescendants( type);
        if(!type.isAbstract()) {
            descendants.add(BmmDefinitions.typeNameToClassKey(type.getTypeName()));
        }

        if(descendants.isEmpty()) {
            //this is an object of which only an abstract class exists.
            //it cannot be represented as standard json, one would think. this is mainly access control and authored
            //resource in the RM
            def.put("type", "object");
        } else if (descendants.size() > 1) {
            JSONArray array = new JSONArray();
            for(String descendant:descendants) {

                JSONArray allOf = new JSONArray();

                JSONObject ref = new JSONObject();
                addReference(descendant, ref);
                allOf.put(ref);
                JSONObject requiredType = new JSONObject();
                JSONArray requiredArray = new JSONArray();
                requiredArray.put("_type");
                requiredType.put("required", requiredArray);
                allOf.put(requiredType);
                JSONObject allOfContainer = new JSONObject();
                allOfContainer.put("allOf", allOf);
                array.put(allOfContainer);
            }
            def.put("anyOf", array);
        } else {
            addReference(BmmDefinitions.typeNameToClassKey(type.getTypeName()), def);
        }

    }

    private void addReference(String typeName, JSONObject ref) {
        ref.put("$ref", "#/definitions/" + typeName);
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
}
