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
package com.salesforce.data360.mcp.model.request.dlo;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Data object field definition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataObjectFieldInput {

    @McpToolParam(description = "Field name")
    private String name;

    @McpToolParam(description = "Field label")
    private String label;

    @McpToolParam(description = "Field type: Boolean, Date, DateOnly, DateTime, Email, Number, Percent, Phone, Text, Url")
    private String type;

    @McpToolParam(description = "Whether this field is a primary key")
    private Boolean isPrimaryKey;

    @McpToolParam(description = "Key qualifier field name")
    private String keyQualifierFieldName;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getKeyQualifierFieldName() {
        return keyQualifierFieldName;
    }

    public void setKeyQualifierFieldName(String keyQualifierFieldName) {
        this.keyQualifierFieldName = keyQualifierFieldName;
    }
}
