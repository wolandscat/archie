package com.nedap.archie.serializer.adl;

import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.json.ArchieRMObjectMapperProvider;
import com.nedap.archie.rm.datavalues.DvText;
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
    public void serializeDefaultOdinValue() throws Exception {
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
}
