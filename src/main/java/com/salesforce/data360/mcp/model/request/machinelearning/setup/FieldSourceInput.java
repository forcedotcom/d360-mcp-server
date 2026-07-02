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
package com.salesforce.data360.mcp.model.request.machinelearning.setup;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Typed reference to a field on a specific source object.
 * profile/related field lists, multi-source filter criteria, etc.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldSourceInput {

    @NotBlank
    @McpToolParam(description = "Field API name (e.g. Amount__c).")
    private String name;

    @McpToolParam(description = "API name of the source object that owns this field (e.g. Sales__dlm).", required = false)
    private String source;

    @McpToolParam(description = "Source kind for the owning object. One of: DataModelObject, CalculatedInsightObject, SdmObject.", required = false)
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
