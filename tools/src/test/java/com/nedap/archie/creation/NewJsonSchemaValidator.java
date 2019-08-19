package com.nedap.archie.creation;

import com.nedap.archie.json.JSONSchemaCreator;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openehr.bmm.core.BmmModel;
import org.openehr.referencemodels.BuiltinReferenceModels;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class NewJsonSchemaValidator {

    Schema schema;

    public NewJsonSchemaValidator(boolean allowAdditionalProperties) {
        BmmModel model = BuiltinReferenceModels.getBmmRepository().getModel("openehr_rm_1.0.4").getModel();
        JSONObject schemaJson = new JSONSchemaCreator().create(model);
        schema = SchemaLoader.load(schemaJson);


        if(!allowAdditionalProperties) {
            //addAdditionalProperties(schemaJson);

            if(schemaJson.has("definitions")) {
                JSONObject definitions = schemaJson.getJSONObject("definitions");
                for(String key:definitions.keySet()) {
                    JSONObject jsonObject = definitions.getJSONObject(key);
                    addAdditionalProperties(jsonObject);
                }
            }

        }

    }

    private void addAdditionalProperties(JSONObject schemaJson) {
        if(schemaJson.has("type")) {
            String jsonType = schemaJson.getString("type");
            if (jsonType.equalsIgnoreCase("object")) {
                schemaJson.put("additionalProperties", false);
            }
        }
    }

    public void validate(String type, String json) throws IOException {
        schema.validate(new JSONObject(json));
        System.out.println("validation ok!");
    }

}
