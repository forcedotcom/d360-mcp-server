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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for {@code POST /ssot/data-model-objects}.
 *
 * <p>Mirrors {@code DataModelObjectInputRepresentation} (no own properties)
 * which extends {@code DataObjectInputRepresentation} (abstract) which extends
 * {@code CdpObjectBaseInputRepresentation} (abstract). All inherited
 * properties are flattened onto this single class.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DmoCreateRequest {

    // ---- CdpObjectBaseInputRepresentation (shared) ----

    @McpToolParam(description = "Name of the data space. If unspecified, the default data space is used.", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Description.", required = false)
    private String description;

    @NotBlank
    @McpToolParam(description = "Display label for the DMO.")
    private String label;

    @NotBlank
    @McpToolParam(
        description = "DMO API name. Do NOT include the __dlm suffix; the API appends it. "
            + "Must be alphanumeric/underscore, begin with a letter, and not start with 'ssot'.")
    private String name;

    // ---- DataObjectInputRepresentation ----

    @NotBlank
    @McpToolParam(
        description = "Category of the data object. One of Engagement, Other, Profile")
    private String category;

    @McpToolParam(
        description = "Event date time field name (required when category is Engagement).",
        required = false)
    private String eventDateTimeFieldName;

    @McpToolParam(description = "Fields in the data object.", required = false)
    private List<DataObjectFieldInput> fields;

    @McpToolParam(description = "Record modified field name.", required = false)
    private String recordModifiedFieldName;

    @McpToolParam(description = "Type of data object (DataModelObject)", required = false)
    private String type;

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventDateTimeFieldName() {
        return eventDateTimeFieldName;
    }

    public void setEventDateTimeFieldName(String eventDateTimeFieldName) {
        this.eventDateTimeFieldName = eventDateTimeFieldName;
    }

    public List<DataObjectFieldInput> getFields() {
        return fields;
    }

    public void setFields(List<DataObjectFieldInput> fields) {
        this.fields = fields;
    }

    public String getRecordModifiedFieldName() {
        return recordModifiedFieldName;
    }

    public void setRecordModifiedFieldName(String recordModifiedFieldName) {
        this.recordModifiedFieldName = recordModifiedFieldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
