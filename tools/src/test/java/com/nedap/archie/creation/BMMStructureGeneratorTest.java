package com.nedap.archie.creation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.OperationalTemplate;
import com.nedap.archie.flattener.Flattener;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;
import com.nedap.archie.xml.JAXBUtil;
import org.junit.Test;
import org.openehr.referencemodels.BuiltinReferenceModels;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

public class BMMStructureGeneratorTest {

    @Test
    public void bloodPressure() throws Exception {
        ADLParser parser = new ADLParser();
        Archetype archetype;
        try (InputStream stream = getClass().getResourceAsStream("/ckm-mirror/local/archetypes/entry/observation/openEHR-EHR-OBSERVATION.blood_pressure.v1.1.0.adls")) {
            archetype = parser.parse(stream);
            if (parser.getErrors().hasErrors()) {
                throw new RuntimeException(parser.getErrors().toString());
            }
        }
        InMemoryFullArchetypeRepository repository = new InMemoryFullArchetypeRepository();
        repository.addArchetype(archetype);
        OperationalTemplate opt = (OperationalTemplate) new Flattener(repository, BuiltinReferenceModels.getMetaModels()).createOperationalTemplate(true).flatten(archetype);

        BmmStructureGenerator structureGenerator = new BmmStructureGenerator(BuiltinReferenceModels.getMetaModels(), "en");
        Map<String, Object> structure = structureGenerator.generate(opt);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String s = objectMapper.writeValueAsString(structure);
        //  System.out.println(s);

    }

    @Test
    public void xmlSchema() throws Exception {
        JAXBContext archieJAXBContext = JAXBUtil.getArchieJAXBContext();
        MySchemaOutputResolver resolver = new MySchemaOutputResolver();
        archieJAXBContext.generateSchema(resolver);
        System.out.println(resolver.getSchema());
    }

    public class MySchemaOutputResolver extends SchemaOutputResolver {
        private StringWriter stringWriter = new StringWriter();

        public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
            StreamResult result = new StreamResult(stringWriter);
            result.setSystemId(suggestedFileName);
            return result;
        }

        public String getSchema() {
            return stringWriter.toString();
        }

    }
}
