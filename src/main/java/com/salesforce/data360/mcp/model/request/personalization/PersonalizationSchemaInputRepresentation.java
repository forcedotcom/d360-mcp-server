/*
 * Copyright (c) 2026, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.salesforce.data360.mcp.model.request.personalization;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Represents the input for creating or updating a Personalization Schema.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalizationSchemaInputRepresentation extends BasePersonalizationInputRepresentation {

    @McpToolParam(description = "The API name of the Data Space associated with the Personalization Schema")
    private String dataSpaceName;

    @McpToolParam(description = "The type of the Personalization Schema. Values: ExperienceVariation, FlowPath, ManualContent, Recommendations")
    private String personalizationType;

    @Valid
    @McpToolParam(description = "List of Personalization Attributes associated with the Personalization Schema")
    private List<PersonalizationAttributeInputRepresentation> attributes;

    @Valid
    @McpToolParam(description = "Information about the ContentObject (DMO) to be used for this Personalization")
    private PersonalizationContentObjectInputRepresentation contentObject;

    @Valid
    @McpToolParam(description = "List of related content objects for this Personalization Schema")
    private List<PersonalizationRelatedContentObjectInputRepresentation> relatedContentObjects;

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }

    public String getPersonalizationType() {
        return personalizationType;
    }

    public void setPersonalizationType(String personalizationType) {
        this.personalizationType = personalizationType;
    }

    public List<PersonalizationAttributeInputRepresentation> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PersonalizationAttributeInputRepresentation> attributes) {
        this.attributes = attributes;
    }

    public PersonalizationContentObjectInputRepresentation getContentObject() {
        return contentObject;
    }

    public void setContentObject(PersonalizationContentObjectInputRepresentation contentObject) {
        this.contentObject = contentObject;
    }

    public List<PersonalizationRelatedContentObjectInputRepresentation> getRelatedContentObjects() {
        return relatedContentObjects;
    }

    public void setRelatedContentObjects(List<PersonalizationRelatedContentObjectInputRepresentation> relatedContentObjects) {
        this.relatedContentObjects = relatedContentObjects;
    }
}
