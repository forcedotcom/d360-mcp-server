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
 * Request body for updating a data transform.
 * All fields are optional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTransformUpdateRequest {

    @McpToolParam(description = "Label of the data transform", required = false)
    private String label;

    @McpToolParam(description = "API name of the data transform", required = false)
    private String name;

    @McpToolParam(description = "Type: BATCH or STREAMING", required = false)
    private String type;

    @McpToolParam(description = "Creation type: Custom or System", required = false)
    private String creationType;

    @McpToolParam(description = "Currency ISO code", required = false)
    private String currencyIsoCode;

    @McpToolParam(description = "Transform definition", required = false)
    private DataTransformDefinitionInput definition;

    @McpToolParam(description = "Description", required = false)
    private String description;

    @McpToolParam(description = "Primary source", required = false)
    private String primarySource;

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

    public String getCreationType() {
        return creationType;
    }

    public void setCreationType(String creationType) {
        this.creationType = creationType;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
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

    public String getPrimarySource() {
        return primarySource;
    }

    public void setPrimarySource(String primarySource) {
        this.primarySource = primarySource;
    }

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }
}
