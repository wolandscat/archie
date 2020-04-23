package com.nedap.archie.serializer.adl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.json.ArchieRMObjectMapperProvider;
import com.nedap.archie.rm.datastructures.Cluster;
import com.nedap.archie.rm.datastructures.Element;
import com.nedap.archie.rm.datatypes.CodePhrase;
import com.nedap.archie.rm.datavalues.DvCodedText;
import com.nedap.archie.rm.datavalues.DvText;
import com.nedap.archie.rm.support.identification.TerminologyId;
import com.nedap.archie.rminfo.MetaModels;
import com.nedap.archie.testutil.TestUtil;
import org.junit.Test;
import org.openehr.referencemodels.BuiltinReferenceModels;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultValueSerializerTest {

    @Test
    public void serializeDvTextOdin() throws Exception {
        ADLParser adlParser = new ADLParser(BuiltinReferenceModels.getMetaModels());
        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-CLUSTER.simple.v1.adls")) {
            Archetype archetype = adlParser.parse(stream);
            CComplexObject cComplexObject = archetype.itemAtPath("/items[id2]/value[id21]");
            DvText dvText = new DvText();
            dvText.setValue("some default value");
            cComplexObject.setDefaultValue(dvText);
            String serialized = ADLArchetypeSerializer.serialize(archetype, null, new ArchieRMObjectMapperProvider());
            System.out.println(serialized);
            assertTrue(serialized.contains("_default = "));
            assertTrue(serialized.contains("\"some default value\""));


            Archetype parsed = adlParser.parse(serialized);
            assertTrue(adlParser.getErrors().hasNoErrors());
            assertNotNull(((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue());
            DvText defaultValue = (DvText) ((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue();
            assertEquals("some default value", defaultValue.getValue());
        }

    }

    @Test
    public void serializeDvTextJson() throws Exception {
        ADLParser adlParser = new ADLParser(BuiltinReferenceModels.getMetaModels());
        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-CLUSTER.simple.v1.adls")) {
            Archetype archetype = adlParser.parse(stream);
            CComplexObject cComplexObject = archetype.itemAtPath("/items[id2]/value[id21]");
            DvText dvText = new DvText();
            dvText.setValue("some default value");
            cComplexObject.setDefaultValue(dvText);
            String serialized = ADLArchetypeSerializer.serialize(archetype, null, new ArchieRMObjectMapperProvider() {
                @Override
                public ObjectMapper getOutputOdinObjectMapper() {
                    return null;
                }
            });
            System.out.println(serialized);
            assertTrue(serialized.contains("_default = < (json) <#"));
            assertTrue(serialized.contains("\"some default value\""));
            assertTrue(serialized.contains("\"_type\" : \"DV_TEXT\""));


            Archetype parsed = adlParser.parse(serialized);
            assertTrue(adlParser.getErrors().hasNoErrors());
            assertNotNull(((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue());
            DvText defaultValue = (DvText) ((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue();
            assertEquals("some default value", defaultValue.getValue());
        }

    }


    @Test
    public void serializeDvCodedTextJson() throws Exception {
        ADLParser adlParser = new ADLParser(BuiltinReferenceModels.getMetaModels());
        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-CLUSTER.simple.v1.adls")) {
            Archetype archetype = adlParser.parse(stream);
            CComplexObject cComplexObject = archetype.itemAtPath("/items[id2]/value[id21]");
            DvCodedText dvCodedText = new DvCodedText("some default value", new CodePhrase(new TerminologyId("local"), "at5"));

            cComplexObject.setDefaultValue(dvCodedText);
            String serialized = ADLArchetypeSerializer.serialize(archetype, null, new ArchieRMObjectMapperProvider() {
                @Override
                public ObjectMapper getOutputOdinObjectMapper() {
                    return null;
                }
            });
            System.out.println(serialized);
            assertTrue(serialized.contains("_default = < (json) <#"));
            assertTrue(serialized.contains("\"some default value\""));
            assertTrue(serialized.contains("\"_type\" : \"DV_CODED_TEXT\""));


            Archetype parsed = adlParser.parse(serialized);
            assertTrue(adlParser.getErrors().hasNoErrors());
            assertNotNull(((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue());
            DvText defaultValue = (DvText) ((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue();
            assertEquals("some default value", defaultValue.getValue());
        }

    }

    @Test
    public void serializeDvCodedTextOdin() throws Exception {
        ADLParser adlParser = new ADLParser(BuiltinReferenceModels.getMetaModels());
        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-CLUSTER.simple.v1.adls")) {
            Archetype archetype = adlParser.parse(stream);
            CComplexObject cComplexObject = archetype.itemAtPath("/items[id2]/value[id21]");
            DvCodedText dvCodedText = new DvCodedText("some default value", new CodePhrase(new TerminologyId("local"), "at5"));

            cComplexObject.setDefaultValue(dvCodedText);
            String serialized = ADLArchetypeSerializer.serialize(archetype, null, new ArchieRMObjectMapperProvider());
            System.out.println(serialized);

            assertTrue(serialized.contains("\"some default value\""));
            assertTrue(serialized.contains("(DV_CODED_TEXT)"));


            Archetype parsed = adlParser.parse(serialized);
            assertTrue(adlParser.getErrors().hasNoErrors());
            assertNotNull(((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue());
            DvText defaultValue = (DvText) ((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue();
            assertEquals("some default value", defaultValue.getValue());
        }

    }

    @Test
    public void serializeClusterJson() throws Exception {
        ADLParser adlParser = new ADLParser(BuiltinReferenceModels.getMetaModels());
        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-CLUSTER.simple.v1.adls")) {
            Archetype archetype = adlParser.parse(stream);
            CComplexObject cComplexObject = archetype.getDefinition();
            Cluster cluster = new Cluster();
            cluster.setArchetypeNodeId("id1");
            cluster.addItem(new Element("id2", new DvText("dv text name"), new DvText("some default value")));

            cComplexObject.setDefaultValue(cluster);
            String serialized = ADLArchetypeSerializer.serialize(archetype, null, new ArchieRMObjectMapperProvider() {
                @Override
                public ObjectMapper getOutputOdinObjectMapper() {
                    return null;
                }
            });
            System.out.println(serialized);
            assertTrue(serialized.contains("_default = < (json) <#"));
            assertTrue(serialized.contains("\"some default value\""));
            assertTrue(serialized.contains("\"_type\" : \"CLUSTER\""));


            Archetype parsed = adlParser.parse(serialized);
            assertTrue(adlParser.getErrors().hasNoErrors());
            assertNotNull(((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue());
            DvText defaultValue = (DvText) ((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue();
            assertEquals("some default value", defaultValue.getValue());
        }
    }

    @Test
    public void serializeClusterOdin() throws Exception {
        ADLParser adlParser = new ADLParser(BuiltinReferenceModels.getMetaModels());
        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-CLUSTER.simple.v1.adls")) {
            Archetype archetype = adlParser.parse(stream);
            CComplexObject cComplexObject = archetype.getDefinition();
            Cluster cluster = new Cluster();
            cluster.setArchetypeNodeId("id1");
            cluster.addItem(new Element("id2", new DvText("dv text name"), new DvText("some default value")));

            cComplexObject.setDefaultValue(cluster);
            String serialized = ADLArchetypeSerializer.serialize(archetype, null, new ArchieRMObjectMapperProvider());
            System.out.println(serialized);
            assertTrue(serialized.contains("_default = <"));
            assertTrue(serialized.contains("\"some default value\""));
            assertTrue(serialized.contains("(CLUSTER)"));


            Archetype parsed = adlParser.parse(serialized);
            assertTrue(adlParser.getErrors().hasNoErrors());
            assertNotNull(((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue());
            DvText defaultValue = (DvText) ((CComplexObject) archetype.itemAtPath("/items[id2]/value[id21]")).getDefaultValue();
            assertEquals("some default value", defaultValue.getValue());
        }

    }
}
