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

import com.salesforce.data360.mcp.model.request.datastream.DataLakeFieldInput;

import java.util.List;

/**
 * Request body for creating a Data Lake Object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DloCreateRequest {

    @McpToolParam(description = "DLO API name")
    private String name;

    @McpToolParam(description = "Display label for the DLO")
    private String label;

    @McpToolParam(description = "Category: Directory_Table, Engagement, Insights, Other, Profile")
    private String category;

    @McpToolParam(description = "Array of dataspace info objects")
    private List<DataSpaceInfoInput> dataspaceInfo;

    @McpToolParam(description = "Array of field definitions", required = false)
    private List<DataLakeFieldInput> dataLakeFieldInputRepresentations;

    @McpToolParam(description = "Array of data object field definitions", required = false)
    private List<DataObjectFieldInput> fields;

    @McpToolParam(description = "Data space name", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Description", required = false)
    private String description;

    @McpToolParam(description = "Org unit identifier field name", required = false)
    private String orgUnitIdentifierFieldName;

    @McpToolParam(description = "Record modified timestamp field name", required = false)
    private String recordModifiedFieldName;

    @McpToolParam(description = "Event date time field name", required = false)
    private String eventDateTimeFieldName;

    @McpToolParam(description = "Type: DataLakeObject or DataModelObject", required = false)
    private String type;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<DataSpaceInfoInput> getDataspaceInfo() {
        return dataspaceInfo;
    }

    public void setDataspaceInfo(List<DataSpaceInfoInput> dataspaceInfo) {
        this.dataspaceInfo = dataspaceInfo;
    }

    public List<DataLakeFieldInput> getDataLakeFieldInputRepresentations() {
        return dataLakeFieldInputRepresentations;
    }

    public void setDataLakeFieldInputRepresentations(List<DataLakeFieldInput> dataLakeFieldInputRepresentations) {
        this.dataLakeFieldInputRepresentations = dataLakeFieldInputRepresentations;
    }

    public List<DataObjectFieldInput> getFields() {
        return fields;
    }

    public void setFields(List<DataObjectFieldInput> fields) {
        this.fields = fields;
    }

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

    public String getOrgUnitIdentifierFieldName() {
        return orgUnitIdentifierFieldName;
    }

    public void setOrgUnitIdentifierFieldName(String orgUnitIdentifierFieldName) {
        this.orgUnitIdentifierFieldName = orgUnitIdentifierFieldName;
    }

    public String getRecordModifiedFieldName() {
        return recordModifiedFieldName;
    }

    public void setRecordModifiedFieldName(String recordModifiedFieldName) {
        this.recordModifiedFieldName = recordModifiedFieldName;
    }

    public String getEventDateTimeFieldName() {
        return eventDateTimeFieldName;
    }

    public void setEventDateTimeFieldName(String eventDateTimeFieldName) {
        this.eventDateTimeFieldName = eventDateTimeFieldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
