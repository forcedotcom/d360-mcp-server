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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for updating a Data Model Object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DmoUpdateRequest {

    @McpToolParam(description = "New display name", required = false)
    private String label;

    @McpToolParam(description = "Updated field definitions", required = false)
    private List<DataModelFieldInputRepresentation> fields;

    @McpToolParam(description = "Updated object category (e.g., 'Profile', 'Engagement', 'Other')", required = false)
    private String category;

    @McpToolParam(description = "Updated description", required = false)
    private String description;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<DataModelFieldInputRepresentation> getFields() {
        return fields;
    }

    public void setFields(List<DataModelFieldInputRepresentation> fields) {
        this.fields = fields;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
