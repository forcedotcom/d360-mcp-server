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
package com.salesforce.data360.mcp.model.request.datatransform;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Base class for data transform request models with common fields.
 *
 * <p>Consolidates shared fields between DataTransformPrepareRequest,
 * DataTransformCreateRequest, and DataTransformUpdateRequest to eliminate duplication.</p>
 *
 * <p><strong>Note on field requirements:</strong> All fields are marked as optional
 * (required = false) to support partial updates via DataTransformUpdateRequest.
 * For create and prepare operations, validation should be handled at the API layer
 * to ensure required fields are present.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class DataTransformBaseRequest {

    @McpToolParam(description = "Label of the data transform", required = false)
    private String label;

    @McpToolParam(description = "API name of the data transform", required = false)
    private String name;

    @McpToolParam(description = "Type: batch or streaming", required = false)
    private String type;

    @McpToolParam(description = "Transform definition", required = false)
    private DataTransformDefinitionInput definition;

    @McpToolParam(description = "Description", required = false)
    private String description;

    @McpToolParam(description = "Data space name", required = false)
    private String dataSpaceName;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DataTransformDefinitionInput getDefinition() {
        return definition;
    }

    public void setDefinition(DataTransformDefinitionInput definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }
}
