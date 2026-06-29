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
 * Represents a related content object for a Personalization Schema.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalizationRelatedContentObjectInputRepresentation {

    @McpToolParam(description = "The API name of the related Content Object")
    private String contentObjectName;

    @McpToolParam(description = "List of DMO field API names for the related content object")
    private List<String> fieldNames;

    @Valid
    @McpToolParam(description = "List of nested related content objects")
    private List<PersonalizationRelatedContentObjectInputRepresentation> relatedContentObjects;

    public String getContentObjectName() {
        return contentObjectName;
    }

    public void setContentObjectName(String contentObjectName) {
        this.contentObjectName = contentObjectName;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public List<PersonalizationRelatedContentObjectInputRepresentation> getRelatedContentObjects() {
        return relatedContentObjects;
    }

    public void setRelatedContentObjects(List<PersonalizationRelatedContentObjectInputRepresentation> relatedContentObjects) {
        this.relatedContentObjects = relatedContentObjects;
    }
}
