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
 * Request body for creating a Data Model Object mapping.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MappingCreateRequest {

    @McpToolParam(description = "Source entity developer name (DLO name)")
    private String sourceEntityDeveloperName;

    @McpToolParam(description = "Target entity developer name (DMO name)")
    private String targetEntityDeveloperName;

    @McpToolParam(description = "Array of field mappings between source DLO and target DMO fields")
    private List<FieldMappingInput> fieldMapping;

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
}
