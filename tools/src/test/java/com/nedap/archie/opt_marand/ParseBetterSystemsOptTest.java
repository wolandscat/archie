package com.nedap.archie.opt_marand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nedap.archie.adl14.ADL14ConversionConfiguration;
import com.nedap.archie.adl14.ADL14Converter;
import com.nedap.archie.adl14.ADL14Parser;
import com.nedap.archie.adl14.ADL2ConversionResult;
import com.nedap.archie.adl14.ADL2ConversionResultList;
import com.nedap.archie.adl14.ConversionConfigForTest;
import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.AuthoredResource;
import com.nedap.archie.aom.CArchetypeRoot;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.aom.CDefinedObject;
import com.nedap.archie.aom.Template;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.archetypevalidator.ValidationResult;
import com.nedap.archie.base.terminology.TerminologyCode;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.json.RMJacksonConfiguration;
import com.nedap.archie.rm.datavalues.DataValue;
import com.nedap.archie.rm.datavalues.DvCodedText;
import com.nedap.archie.rminfo.MetaModels;
import com.nedap.archie.serializer.adl.ADLArchetypeSerializer;
import com.nedap.archie.testutil.TestUtil;
import org.junit.Test;
import org.openehr.referencemodels.BuiltinReferenceModels;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ParseBetterSystemsOptTest {

    private String[] archetypeFiles = {
            "openEHR-EHR-CLUSTER.address_cc.v0.adl",
            "openEHR-EHR-CLUSTER.dwelling.v0.adl",
            "openEHR-EHR-CLUSTER.employment_covid.v0.adl",
            "openEHR-EHR-CLUSTER.occupation_record.v1.adl",
            "openEHR-EHR-CLUSTER.organisation_cc.v0.adl",
            "openEHR-EHR-CLUSTER.outbreak_exposure.v0.adl",
            "openEHR-EHR-CLUSTER.overcrowding_screening.v0.adl",
            "openEHR-EHR-CLUSTER.problem_qualifier.v1.adl",
            "openEHR-EHR-CLUSTER.symptom_sign.v1.adl",
            "openEHR-EHR-CLUSTER.symptom_sign-cvid.v0.adl",
            "openEHR-EHR-COMPOSITION.encounter.v1.adl",
            "openEHR-EHR-EVALUATION.health_risk.v1.adl",
            "openEHR-EHR-EVALUATION.health_risk-covid.v0.adl",
            "openEHR-EHR-EVALUATION.living_arrangement.v0.adl",
            "openEHR-EHR-EVALUATION.occupation_summary.v1.adl",
            "openEHR-EHR-EVALUATION.problem_diagnosis.v1.adl",
            "openEHR-EHR-INSTRUCTION.service_request.v1.adl",
            "openEHR-EHR-OBSERVATION.body_temperature.v2.adl",
            "openEHR-EHR-OBSERVATION.story.v1.adl",
            "openEHR-EHR-OBSERVATION.travel_history.v0.adl"
    };

    @Test
    public void parseOpt() throws IOException {
        try(InputStream stream = getClass().getResourceAsStream("/opt_json/COVID-19-Screening_t.json")) {
            RMJacksonConfiguration config = new RMJacksonConfiguration();
            config.setFailOnUnknownProperties(true);
            ObjectMapper objectMapper = getObjectMapper(config);


            Archetype archetype = objectMapper.readValue(stream, Archetype.class);
            System.out.println(ADLArchetypeSerializer.serialize(archetype));
        }
    }

    private ObjectMapper getObjectMapper(RMJacksonConfiguration config) {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonUtil.configureObjectMapper(objectMapper, config);

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);

        objectMapper.addMixIn(TerminologyCode.class, TerminologyIdParsingTerminologyCodeMixin.class);
        objectMapper.addMixIn(AuthoredResource.class, AuthoredResourceMixin.class);
        objectMapper.addMixIn(Archetype.class, ArchetypeMixin.class);
        objectMapper.addMixIn(Template.class, ArchetypeMixin.class);
        objectMapper.addMixIn(CArchetypeRoot.class, CArchetypeRootMixin.class);
        objectMapper.addMixIn(CComplexObject.class, CComplexObjectMixin.class);
        objectMapper.addMixIn(CDefinedObject.class, CDefinedObjectMixin.class);

        objectMapper.addMixIn(CTerminologyCode.class, CTerminologyCodeMixin.class);
        return objectMapper;
    }

    @Test
    public void parseCovidAssessment() throws Exception {
        MetaModels metaModels = BuiltinReferenceModels.getMetaModels();
        ADL14ConversionConfiguration adl14ConversionConfiguration = ConversionConfigForTest.getConfig();
        List<Archetype> archetypes = new ArrayList<>();
        for(String fileName:archetypeFiles) {
            try(InputStream stream = getClass().getResourceAsStream("/opt_json/Archetypes/" + fileName)) {

                ADL14Parser parser = new ADL14Parser(metaModels);
                Archetype archetype = parser.parse(stream, adl14ConversionConfiguration);
                archetypes.add(archetype);
                assertTrue(fileName + " should not contain errors", parser.getErrors().hasNoErrors());
            } catch (Exception e) {
                throw new RuntimeException(fileName + " did not parse", e);
            }
        }
        ADL2ConversionResultList converted = new ADL14Converter(BuiltinReferenceModels.getMetaModels(), adl14ConversionConfiguration)
                .convert(archetypes);
        int convertedArchetypes = 0;
        for(ADL2ConversionResult conversionResult:converted.getConversionResults()) {
            if(conversionResult.getException() != null) {
                throw new RuntimeException("problem converting archetype " + conversionResult.getArchetypeId(), conversionResult.getException());
            } else if (conversionResult.getArchetype() != null) {
                convertedArchetypes++;
            }
        }
        InMemoryFullArchetypeRepository adl2Repository = new InMemoryFullArchetypeRepository();
        for(ADL2ConversionResult conversionResult:converted.getConversionResults()) {
            if(conversionResult.getException() == null && conversionResult.getArchetype() != null) {
                adl2Repository.addArchetype(conversionResult.getArchetype());
            }
        }
        adl2Repository.compile(BuiltinReferenceModels.getMetaModels());
        int passingValidations = 0;
        for(ValidationResult validationResult:adl2Repository.getAllValidationResults()) {
            if(!validationResult.passes()) {
                throw new RuntimeException(MessageFormat.format("error validating {0}: {1}", validationResult.getArchetypeId(), validationResult.getErrors()));
            }
        }

        RMJacksonConfiguration config = new RMJacksonConfiguration();
        config.setFailOnUnknownProperties(true);
        ObjectMapper mapper = getObjectMapper(config);
        try(InputStream stream = getClass().getResourceAsStream("/opt_json/Templates/Suspected Covid-19 Assessment.v0.1.t.json")) {
            Archetype archetype = mapper.readValue(stream, Archetype.class);
            ADLArchetypeSerializer.serialize(archetype);
        }

    }
}
