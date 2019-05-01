package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.antlr.errors.ANTLRParserErrors;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.archetypevalidator.ValidationResult;
import com.nedap.archie.diff.Differentiator;
import com.nedap.archie.flattener.Flattener;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.serializer.adl.ADLArchetypeSerializer;
import org.junit.Test;
import org.openehr.referencemodels.BuiltinReferenceModels;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Created by pieter.bos on 16/10/15.
 */
public class LargeSetOfADL14sTest {

    private static Logger logger = LoggerFactory.getLogger(LargeSetOfADL14sTest.class);

    @Test
    public void parseLots() throws Exception {
        Reflections reflections = new Reflections("adl14", new ResourcesScanner());
        List<String> adlFiles = new ArrayList(reflections.getResources(Pattern.compile(".*\\.adl")));

        Map<String, Exception> exceptions = new LinkedHashMap<>();
        Map<String, ANTLRParserErrors> parseErrors = new LinkedHashMap<>();

        InMemoryFullArchetypeRepository repository = new InMemoryFullArchetypeRepository();

        List<Archetype> archetypes = new ArrayList<>();
        for(String file:adlFiles) {
//            if(!file.contains("pulse")) {
//                continue;
//            }
            Archetype archetype = parse(exceptions, parseErrors, file);
            if(archetype != null) {
                archetypes.add(archetype);
            }

        }
        ADL2ConversionResultList converted = new ADL14Converter().convert(BuiltinReferenceModels.getMetaModels(), archetypes, ConversionConfigForTest.getConfig(), null);
        for(ADL2ConversionResult result:converted.getConversionResults()) {
//            System.out.println(ADLArchetypeSerializer.serialize(result.getArchetype()));
        }

        for(String file:adlFiles) {
            if(parseErrors.containsKey(file)) {
                logger.error("parse error found in " + file);
                logger.error(parseErrors.get(file).toString());
            }
            if(exceptions.containsKey(file)) {
                logger.error("exception found in " + file, exceptions.get(file));
            }

        }
        for(String file:exceptions.keySet()) {
            if(!adlFiles.contains(file)) {
                logger.error("exception found in " + file, exceptions.get(file));
            }
        }

        int convertedArchetypes = 0;
        for(ADL2ConversionResult conversionResult:converted.getConversionResults()) {
            if(conversionResult.getException() != null) {
                logger.error("exception in converter for archetype id " + conversionResult.getArchetypeId(), conversionResult.getException());
            } else if (conversionResult.getArchetype() != null) {
                convertedArchetypes++;
            }
        }

        logger.info("parsed adls: " + adlFiles.size());
        logger.info("number of archetypes: " + archetypes.size());
        logger.info("number of adl 2 archetypes: " + convertedArchetypes);
        logger.info("parsed adls with ANTLR parse errors: " + parseErrors.size());
        logger.info("parsed adls with Exceptions: " + exceptions.size());

        InMemoryFullArchetypeRepository adl2Repository = new InMemoryFullArchetypeRepository();
        for(ADL2ConversionResult conversionResult:converted.getConversionResults()) {
            if(conversionResult.getException() == null && conversionResult.getArchetype() != null) {
                adl2Repository.addArchetype(conversionResult.getArchetype());
            }
        }
        adl2Repository.compile(BuiltinReferenceModels.getMetaModels());
        int passingValidations = 0;
        for(ValidationResult validationResult:adl2Repository.getAllValidationResults()) {
            if(validationResult.passes()) {
                passingValidations++;
            } else {
                logger.error("error validating {}: {}", validationResult.getArchetypeId(), validationResult.getErrors());
            }
        }

        logger.info("passing validation: " + passingValidations);

        //TODO: this is rather ugly, but I just want not more failing tests, that's all :)
        //this now contains regexp matching errors, version 1.5 (arguably, should not fail on that at all!)
        //and some other problems

        //some errors in the terminology sections caused by some property called 'items'
        //6 errors in annotations section caused by some property called 'items'
        //some errors due to test cases for wrong syntax
        //some errors due to incompatible ADL 1.5-syntax
        assertTrue(exceptions.size() <= 21);


    }

    private Archetype parse(Map<String, Exception> exceptions, Map<String, ANTLRParserErrors> parseErrors, String file) {
        try (InputStream stream = getClass().getResourceAsStream("/" + file)) {
            logger.info("trying to parse " + file);
            ADL14Parser parser = new ADL14Parser(BuiltinReferenceModels.getMetaModels());
            Archetype archetype = parser.parse(stream, ConversionConfigForTest.getConfig());
            //logger.info(JacksonUtil.getObjectMapper().writeValueAsString(conversionResult.getConversionLog()));
           // System.out.println(ADLArchetypeSerializer.serialize(archetype));
            if(parser.errorListener.getErrors().getErrors().size() > 0) {
                parseErrors.put(file, parser.errorListener.getErrors());
            }
            if(parser.getTree().exception != null) {
                exceptions.put(file, parser.getTree().exception);
            }
            return archetype;

        } catch (Exception e) {
            exceptions.put(file, e);
        }
        return null;
    }


}
