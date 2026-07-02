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
 * Mirrors {@code DataObjectFieldInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataObjectFieldInput {

    @McpToolParam(description = "Name of the field.", required = false)
    private String name;

    @McpToolParam(description = "Label of the field.", required = false)
    private String label;

    @JsonProperty("dataType")
    @JsonAlias("type")
    @McpToolParam(
        description = "Data type of the field. One of Boolean, Date, DateOnly, DateTime, Email, Number, Percent, Phone, Text, Url.",
        required = false)
    private String dataType;

    @McpToolParam(description = "Is the field a primary key.", required = false)
    private Boolean isPrimaryKey;

    @McpToolParam(description = "Key qualifier field name.", required = false)
    private String keyQualifierField;

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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getKeyQualifierField() {
        return keyQualifierField;
    }

    public void setKeyQualifierField(String keyQualifierField) {
        this.keyQualifierField = keyQualifierField;
    }
}
