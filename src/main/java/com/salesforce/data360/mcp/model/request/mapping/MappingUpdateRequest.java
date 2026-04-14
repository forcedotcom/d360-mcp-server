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
package com.salesforce.data360.mcp.model.request.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for updating a Data Model Object mapping.
 * All fields are optional for partial updates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MappingUpdateRequest {

    @McpToolParam(description = "Source entity developer name (DLO name)", required = false)
    private String sourceEntityDeveloperName;

    @McpToolParam(description = "Target entity developer name (DMO name)", required = false)
    private String targetEntityDeveloperName;

    @McpToolParam(description = "Array of field mappings between source DLO and target DMO fields", required = false)
    private List<FieldMappingInput> fieldMapping;

    @McpToolParam(description = "Data space name", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Description of the mapping", required = false)
    private String description;

    @McpToolParam(description = "Label for the mapping", required = false)
    private String label;

    @McpToolParam(description = "Name for the mapping", required = false)
    private String name;

    public String getSourceEntityDeveloperName() {
        return sourceEntityDeveloperName;
    }

    public void setSourceEntityDeveloperName(String sourceEntityDeveloperName) {
        this.sourceEntityDeveloperName = sourceEntityDeveloperName;
    }

    public String getTargetEntityDeveloperName() {
        return targetEntityDeveloperName;
    }

    public void setTargetEntityDeveloperName(String targetEntityDeveloperName) {
        this.targetEntityDeveloperName = targetEntityDeveloperName;
    }

    public List<FieldMappingInput> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(List<FieldMappingInput> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
