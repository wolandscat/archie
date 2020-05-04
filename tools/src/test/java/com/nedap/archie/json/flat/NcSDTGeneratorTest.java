package com.nedap.archie.json.flat;

import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.OperationalTemplate;
import com.nedap.archie.creation.ExampleJsonInstanceGenerator;
import com.nedap.archie.flattener.Flattener;
import com.nedap.archie.flattener.FlattenerConfiguration;
import com.nedap.archie.flattener.SimpleArchetypeRepository;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.rm.RMObject;
import com.nedap.archie.rminfo.ArchieRMInfoLookup;
import org.junit.Test;
import org.openehr.referencemodels.BuiltinReferenceModels;

import java.io.InputStream;
import java.util.Map;

public class NcSDTGeneratorTest {

    private static final String BLOOD_PRESSURE_PATH = "/ckm-mirror/local/archetypes/entry/observation/openEHR-EHR-OBSERVATION.blood_pressure.v1.1.0.adls";

    @Test
    public void testBloodPressureExample() throws Exception {

        try(InputStream stream = getClass().getResourceAsStream(BLOOD_PRESSURE_PATH)) {
            Archetype bloodPressure = new ADLParser(BuiltinReferenceModels.getMetaModels()).parse(stream);
            Flattener flattener = new Flattener(new SimpleArchetypeRepository(), BuiltinReferenceModels.getMetaModels(), FlattenerConfiguration.forOperationalTemplate());
            OperationalTemplate bloodPressureOpt = (OperationalTemplate) flattener.flatten(bloodPressure);

            ExampleJsonInstanceGenerator exampleGenerator = new ExampleJsonInstanceGenerator(BuiltinReferenceModels.getMetaModels(), "en");
            Map<String, Object> generate = exampleGenerator.generate(bloodPressureOpt);
            String rmObjectJson = JacksonUtil.getObjectMapper().writeValueAsString(generate);
            RMObject rmObject = JacksonUtil.getObjectMapper().readValue(rmObjectJson, RMObject.class);
            Map<String, Object> stringObjectMap = new NcSDTGenerator(ArchieRMInfoLookup.getInstance(), false, false).buildPathsAndValues(rmObject);
            System.out.println(JacksonUtil.getObjectMapper().writeValueAsString(stringObjectMap));
        }


    }


    @Test
    public void testBloodPressureExampleWithPipesForFinalFields() throws Exception {

        try(InputStream stream = getClass().getResourceAsStream(BLOOD_PRESSURE_PATH)) {
            Archetype bloodPressure = new ADLParser(BuiltinReferenceModels.getMetaModels()).parse(stream);
            Flattener flattener = new Flattener(new SimpleArchetypeRepository(), BuiltinReferenceModels.getMetaModels(), FlattenerConfiguration.forOperationalTemplate());
            OperationalTemplate bloodPressureOpt = (OperationalTemplate) flattener.flatten(bloodPressure);

            ExampleJsonInstanceGenerator exampleGenerator = new ExampleJsonInstanceGenerator(BuiltinReferenceModels.getMetaModels(), "en");
            Map<String, Object> generate = exampleGenerator.generate(bloodPressureOpt);
            String rmObjectJson = JacksonUtil.getObjectMapper().writeValueAsString(generate);
            RMObject rmObject = JacksonUtil.getObjectMapper().readValue(rmObjectJson, RMObject.class);
            Map<String, Object> stringObjectMap = new NcSDTGenerator(ArchieRMInfoLookup.getInstance(), true, false).buildPathsAndValues(rmObject);
            System.out.println(JacksonUtil.getObjectMapper().writeValueAsString(stringObjectMap));
        }

    }

    @Test
    public void testBloodPressureHumanReadable() throws Exception {

        try(InputStream stream = getClass().getResourceAsStream(BLOOD_PRESSURE_PATH)) {
            Archetype bloodPressure = new ADLParser(BuiltinReferenceModels.getMetaModels()).parse(stream);
            Flattener flattener = new Flattener(new SimpleArchetypeRepository(), BuiltinReferenceModels.getMetaModels(), FlattenerConfiguration.forOperationalTemplate());
            OperationalTemplate bloodPressureOpt = (OperationalTemplate) flattener.flatten(bloodPressure);

            ExampleJsonInstanceGenerator exampleGenerator = new ExampleJsonInstanceGenerator(BuiltinReferenceModels.getMetaModels(), "en");
            exampleGenerator.setUseTypeNameWhenTermMissing(true);
            Map<String, Object> generate = exampleGenerator.generate(bloodPressureOpt);
            String rmObjectJson = JacksonUtil.getObjectMapper().writeValueAsString(generate);
            RMObject rmObject = JacksonUtil.getObjectMapper().readValue(rmObjectJson, RMObject.class);
            Map<String, Object> stringObjectMap = new NcSDTGenerator(ArchieRMInfoLookup.getInstance(), false, true).buildPathsAndValues(rmObject);
            System.out.println(JacksonUtil.getObjectMapper().writeValueAsString(stringObjectMap));
        }


    }
}
