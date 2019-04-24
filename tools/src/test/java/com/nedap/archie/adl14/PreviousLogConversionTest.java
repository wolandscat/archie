package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CObject;
import org.junit.Test;

import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;

public class PreviousLogConversionTest {

    @Test
    public void applyConsistentConversion() throws Exception {
        ADL2ConversionLog log = null;
        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-COMPOSITION.review.v1.adl")) {
            ADL2ConversionResult result = new ADL14Converter().convert(new ADL14Parser().parse(stream, ConversionConfigForTest.getConfig()), ConversionConfigForTest.getConfig(), null);
            log = result.getConversionLog();
        }

        try(InputStream stream = getClass().getResourceAsStream("openEHR-EHR-COMPOSITION.review.v1.modified.adl")) {
            ADL2ConversionResult result = new ADL14Converter().convert(new ADL14Parser().parse(stream, ConversionConfigForTest.getConfig()), ConversionConfigForTest.getConfig(), log);
            CAttribute attribute = result.getArchetype().itemAtPath("/category");
            assertEquals(2, attribute.getChildren().size());
            CObject dvText = attribute.getChildren().get(0);
            CObject dvCodedText = attribute.getChildren().get(1);
            assertEquals("DV_CODED_TEXT", dvCodedText.getRmTypeName());
            assertEquals("DV_TEXT", dvText.getRmTypeName());
            assertEquals("id3", dvCodedText.getNodeId());
            assertEquals("id4", dvText.getNodeId());

        }
    }
}
