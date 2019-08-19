package com.nedap.archie.json;

import org.json.JSONObject;
import org.junit.Test;
import org.openehr.bmm.core.BmmModel;
import org.openehr.referencemodels.BuiltinReferenceModels;

public class JSONSchemaCreatorTest {

    @Test
    public void createSchema() {
        BmmModel model = BuiltinReferenceModels.getBmmRepository().getModel("openehr_rm_1.0.4").getModel();
        JSONObject jsonObject = new JSONSchemaCreator().create(model);
        System.out.println(jsonObject.toString(2));
    }
}
