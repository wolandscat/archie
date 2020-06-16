package com.nedap.archie.rm.datatypes;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class CodePhraseTest {

    @Test
    public void testEquals() {
        CodePhrase codePhraseOne = new CodePhrase("hl7::gender");
        CodePhrase codePhraseTwo = new CodePhrase("hl7::gender");
        CodePhrase codePhraseThree = new CodePhrase("hl2::gender");
        CodePhrase codePhraseFour = new CodePhrase("hl7::color");

        Assert.assertEquals(codePhraseOne, codePhraseTwo);

        Assert.assertNotEquals(codePhraseOne, codePhraseThree);
        Assert.assertNotEquals(codePhraseOne, codePhraseFour);
    }
}