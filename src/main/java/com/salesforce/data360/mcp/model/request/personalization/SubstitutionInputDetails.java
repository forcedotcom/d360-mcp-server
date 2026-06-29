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
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Details for a substitution variable in a transformer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubstitutionInputDetails {

    @McpToolParam(description = "Label to be displayed in a UI when configuring this Substitution Variable")
    private String label;

    @McpToolParam(description = "Description to be displayed in a UI when configuring this Substitution Variable")
    private String description;

    @McpToolParam(description = "How to interpret the substitution variable. Values: SchemaPath, Static")
    private String configType;

    @McpToolParam(description = "A default value for the substitution definition to be used if Personalization Experience Config does not override the value")
    private String defaultValue;

    @McpToolParam(description = "Can the Experience override the default value?")
    private Boolean overridable;

    @McpToolParam(description = "Must a value (either default or in Experience) be provided?")
    private Boolean required;

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

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getOverridable() {
        return overridable;
    }

    public void setOverridable(Boolean overridable) {
        this.overridable = overridable;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
