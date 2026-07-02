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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for {@code POST /ssot/data-lake-objects}.
 *
 * <p>Mirrors {@code DataLakeObjectInputRepresentation} which extends
 * {@code DataObjectInputRepresentation} (abstract) which extends
 * {@code CdpObjectBaseInputRepresentation} (abstract). All inherited
 * properties are flattened onto this single class.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DloCreateRequest {

    // ---- CdpObjectBaseInputRepresentation (shared) ----
    // NOTE: dataspaceName is intentionally omitted. The DLO endpoint
    // (/ssot/data-lake-objects) rejects this field with
    // "Unrecognized field 'dataspaceName'" / "Unrecognized field 'dataSpaceName'"
    // even though the UDF inherits it from CdpObjectBaseInputRepresentation.
    // The DLO endpoint defaults to the "default" dataspace when unspecified.
    // (DMO endpoint, by contrast, does accept dataSpaceName.)

    @McpToolParam(description = "Description of the object to create.", required = false)
    private String description;

    @NotBlank
    @McpToolParam(description = "Display label for the DLO.")
    private String label;

    @NotBlank
    @McpToolParam(description = "DLO API name.")
    private String name;

    // ---- DataObjectInputRepresentation ----

    @NotBlank
    @McpToolParam(
        description = "Category of the data object. One of Engagement, Other, Profile")
    private String category;

    @McpToolParam(description = "Name of the event date and time field. This property is required when the value of the category property is Engagement.Name of the event date and time field. This property is required when the value of the category property is Engagement.", required = false)
    private String eventDateTimeFieldName;

    @McpToolParam(description = "Fields in the data object.", required = false)
    private List<DataObjectFieldInput> fields;

    @McpToolParam(description = "Record modified field name.", required = false)
    private String recordModifiedFieldName;

    @McpToolParam(
        description = "Type of data object (DataLakeObject)",
        required = false)
    private String type;

    // ---- DataLakeObjectInputRepresentation (own) ----

    @McpToolParam(description = "Status of the DLO.", required = false)
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
