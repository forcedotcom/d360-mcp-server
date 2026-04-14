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

/**
 * Field mapping input for DLO-to-DMO field mapping.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldMappingInput {

    @McpToolParam(description = "Source field developer name (from DLO)")
    private String sourceFieldDeveloperName;

    @McpToolParam(description = "Target field developer name (from DMO)")
    private String targetFieldDeveloperName;

    public String getSourceFieldDeveloperName() {
        return sourceFieldDeveloperName;
    }

    public void setSourceFieldDeveloperName(String sourceFieldDeveloperName) {
        this.sourceFieldDeveloperName = sourceFieldDeveloperName;
    }

    public String getTargetFieldDeveloperName() {
        return targetFieldDeveloperName;
    }

    public void setTargetFieldDeveloperName(String targetFieldDeveloperName) {
        this.targetFieldDeveloperName = targetFieldDeveloperName;
    }
}
