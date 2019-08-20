package com.nedap.archie.creation;

import com.google.common.base.Charsets;
import com.nedap.archie.json.JSONSchemaCreator;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.openehr.bmm.core.BmmModel;
import org.openehr.referencemodels.BuiltinReferenceModels;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewJsonSchemaValidator {

    JsonSchema schema;

    public NewJsonSchemaValidator(boolean allowAdditionalProperties) {
        BmmModel model = BuiltinReferenceModels.getBmmRepository().getModel("openehr_rm_1.0.4").getModel();
        JsonObject schemaJson = new JSONSchemaCreator().allowAdditionalProperties(allowAdditionalProperties).create(model);

        JsonValidationService service = JsonValidationService.newInstance();
        schema = service.readSchema(createStringInputStream(schemaJson.toString()));

    }

    private ByteArrayInputStream createStringInputStream(String json) {
        return new ByteArrayInputStream(json.getBytes(Charsets.UTF_8));
    }

    public List<Problem> validate(String type, String json) throws IOException {

        JsonValidationService service = JsonValidationService.newInstance();
        service.createProblemPrinter(System.out::println);
        List<Problem> allProblems = new ArrayList<>();
        ProblemHandler problemHandler = new ProblemHandler() {
            @Override
            public void handleProblems(List<Problem> problems) {
                allProblems.addAll(problems);
            }
        };

        try (JsonReader reader = service.createReader(createStringInputStream(json), schema, problemHandler)) {
            System.out.println("validation errors: " + allProblems.size());
            JsonStructure structure = reader.read();
            return allProblems;
        }
    }

}
