package com.nedap.archie.opt_marand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.AuthoredResource;
import com.nedap.archie.aom.CArchetypeRoot;
import com.nedap.archie.aom.Template;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.base.terminology.TerminologyCode;
import com.nedap.archie.json.JacksonUtil;
import com.nedap.archie.json.RMJacksonConfiguration;
import com.nedap.archie.serializer.adl.ADLArchetypeSerializer;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class ParseBetterSystemsOptTest {

    @Test
    public void parseOpt() throws IOException {
        try(InputStream stream = getClass().getResourceAsStream("/opt_json/COVID-19-Screening_t.json")) {
            RMJacksonConfiguration config = new RMJacksonConfiguration();
            config.setFailOnUnknownProperties(true);
            ObjectMapper objectMapper = new ObjectMapper();
            JacksonUtil.configureObjectMapper(objectMapper, config);



            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);


            objectMapper.addMixIn(TerminologyCode.class, TerminologyIdParsingTerminologyCodeMixin.class);
            objectMapper.addMixIn(AuthoredResource.class, AuthoredResourceMixin.class);
            objectMapper.addMixIn(Archetype.class, ArchetypeMixin.class);
            objectMapper.addMixIn(Template.class, ArchetypeMixin.class);
            objectMapper.addMixIn(CArchetypeRoot.class, CArchetypeRootMixin.class);

            objectMapper.addMixIn(CTerminologyCode.class, CTerminologyCodeMixin.class);



            Archetype archetype = objectMapper.readValue(stream, Archetype.class);
            System.out.println(ADLArchetypeSerializer.serialize(archetype));
        }
    }
}
