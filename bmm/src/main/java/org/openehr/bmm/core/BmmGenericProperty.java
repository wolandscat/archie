package org.openehr.bmm.core;

/*
 * #%L
 * OpenEHR - Java Model Stack
 * %%
 * Copyright (C) 2016 - 2017 Cognitive Medical Systems
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 * Author: Claude Nanjo
 */

import org.openehr.bmm.BmmConstants;
import org.openehr.bmm.core.BmmGenericType;
import org.openehr.bmm.core.BmmProperty;
import org.openehr.bmm.persistence.PersistedBmmGenericProperty;
import org.openehr.odin.utils.OdinSerializationUtils;

import java.io.Serializable;

public class BmmGenericProperty extends BmmProperty implements Serializable {

    private BmmGenericType genericTypeDef;//TODO fix to call it typeDef once fix has been made to this attribute in parent class;

    public BmmGenericProperty() {}

    public BmmGenericProperty(String name, boolean isMandatory, BmmGenericType genericTypeDef) {
        setName(name);
        setMandatory(isMandatory);
        this.genericTypeDef = genericTypeDef;
    }

    public BmmGenericType getGenericTypeDef() {
        return genericTypeDef;
    }

    public void setGenericTypeDef(BmmGenericType genericTypeDef) {
        this.genericTypeDef = genericTypeDef;
    }

    /**
     * Method attempts to convert BmmGenericProperty into a PersistedBmmGenericProperty.
     *
     * @return
     */
    @Override
    public PersistedBmmGenericProperty convertToPersistentBmmProperty() {
        PersistedBmmGenericProperty property = new PersistedBmmGenericProperty(getName(), getMandatory());
        property.setDocumentation(getDocumentation());
        property.setImInfrastructure(getImInfrastructure());
        property.setImRuntime(getImRuntime());
        if(genericTypeDef != null) {
            property.setTypeDefinition(genericTypeDef.convertToPersistedBmmGenericType());
        }
        return property;
    }
}
