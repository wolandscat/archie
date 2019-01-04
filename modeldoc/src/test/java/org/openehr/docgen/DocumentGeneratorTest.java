package org.openehr.docgen;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openehr.bmm.v2.persistence.PBmmSchema;
import org.openehr.bmm.v2.persistence.odin.BmmOdinParser;
import org.openehr.bmm.v2.validation.BmmRepository;
import org.openehr.bmm.v2.validation.BmmSchemaConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * Copyright 2017 Cognitive Medical Systems, Inc (http://www.cognitivemedicine.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Created by cnanjo on 3/7/17.
 */

@Ignore
public class DocumentGeneratorTest {

    private BmmRepository repo;

    @Before
    public void setup() throws Exception {
        repo = new BmmRepository();
        repo.addPersistentSchema(parse("/cimi/CIMI-RM-3.0.5.bmm"));
        repo.addPersistentSchema(parse("/cimi/CIMI_RM_CLINICAL.v.0.0.3.bmm"));
        repo.addPersistentSchema(parse("/cimi/CIMI_RM_CORE.v.0.0.3.bmm"));
        repo.addPersistentSchema(parse("/cimi/CIMI_RM_FOUNDATION.v.0.0.3.bmm"));


        BmmSchemaConverter converter = new BmmSchemaConverter(repo);
        converter.validateAndConvertRepository();
    }

    private PBmmSchema parse(String name) throws IOException {
        try(InputStream stream = getClass().getResourceAsStream(name)) {//"/testbmm/TestBmm1.bmm")) {
            return BmmOdinParser.convert(stream);
        }
    }

    @Test
    public void generateDocument() throws Exception {

        DocumentGenerator generator = new DocumentGenerator();
        File file = new File(DocumentGeneratorTest.class.getResource("/templates/").getFile());
        assertTrue(file.isDirectory());
        generator.configure(file);
        generator.setOutputDirectory("/Users/cnanjo/work/cimi_doc");
        generator.generateDocument(repo.getModel("cimi_rm_clinical_0.0.3".toUpperCase()).getModel());
    }

}