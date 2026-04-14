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
package com.salesforce.data360.mcp.model.request.dmo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Field definition input for creating/updating Data Model Object fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataModelFieldInputRepresentation {

    @McpToolParam(description = "API name of the custom data model field")
    private String name;

    @McpToolParam(description = "Label of the custom data model field")
    private String label;

    @McpToolParam(description = "Description of the custom data model field", required = false)
    private String description;

    @McpToolParam(description = "Creation type: System or Custom", required = false)
    private String creationType;

    @McpToolParam(description = "True if this field is a primary key", required = false)
    private Boolean isPrimaryKey;

    @McpToolParam(description = "True if this field has dynamic lookup", required = false)
    private Boolean hasDynamicLookup;

    @JsonProperty("dataType")
    @JsonAlias("type")
    @McpToolParam(description = "Data type of the field (e.g., Text, Date, DateTime, Number, Boolean)")
    private String dataType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreationType() {
        return creationType;
    }

    public void setCreationType(String creationType) {
        this.creationType = creationType;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public Boolean getHasDynamicLookup() {
        return hasDynamicLookup;
    }

    public void setHasDynamicLookup(Boolean hasDynamicLookup) {
        this.hasDynamicLookup = hasDynamicLookup;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
