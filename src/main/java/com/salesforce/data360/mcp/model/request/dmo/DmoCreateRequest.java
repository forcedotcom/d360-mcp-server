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
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request body for creating a Data Model Object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DmoCreateRequest {

    @NotBlank
    @McpToolParam(description = "DMO API name — do NOT include __dlm suffix, the API appends it automatically. " +
        "Must contain only alphanumeric characters and underscores, must begin with a letter, " +
        "must not end with an underscore or contain two consecutive underscores and not start with 'ssot'.")
    private String name;

    @NotBlank
    @McpToolParam(description = "DMO display name")
    private String label;

    @McpToolParam(description = "Object type (e.g., 'Standard', 'Custom'). " +
        "Omit for most custom DMO creation — the API infers it.", required = false)
    private String objectType;

    @JsonProperty("category")
    @JsonAlias("objectCategory")
    @McpToolParam(description = "Object category: 'Profile', 'Engagement', or 'Other'. " +
        "Pass as 'category' in JSON (not 'objectCategory').", required = false)
    private String category;

    @McpToolParam(description = "Field definitions. Each field must include: name, label, dataType, and isPrimaryKey (true/false).",
        required = false)
    private List<DataModelFieldInputRepresentation> fields;

    @McpToolParam(description = "DMO status: Active, Error, Inactive, or Processing", required = false)
    private String status;

    @McpToolParam(description = "Description of the DMO", required = false)
    private String description;

    @McpToolParam(description = "Data space name", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Event date/time field name (required when category is Engagement)", required = false)
    private String eventDateTimeFieldName;

    @McpToolParam(description = "Record modified field name", required = false)
    private String recordModifiedFieldName;

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

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<DataModelFieldInputRepresentation> getFields() {
        return fields;
    }

    public void setFields(List<DataModelFieldInputRepresentation> fields) {
        this.fields = fields;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getEventDateTimeFieldName() {
        return eventDateTimeFieldName;
    }

    public void setEventDateTimeFieldName(String eventDateTimeFieldName) {
        this.eventDateTimeFieldName = eventDateTimeFieldName;
    }

    public String getRecordModifiedFieldName() {
        return recordModifiedFieldName;
    }

    public void setRecordModifiedFieldName(String recordModifiedFieldName) {
        this.recordModifiedFieldName = recordModifiedFieldName;
    }
}
