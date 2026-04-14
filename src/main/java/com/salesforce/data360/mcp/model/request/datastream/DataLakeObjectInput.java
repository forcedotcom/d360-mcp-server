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
package com.salesforce.data360.mcp.model.request.datastream;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Represents the input for a data lake object (DLO).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataLakeObjectInput {

    @McpToolParam(description = "Name of the DLO")
    private String name;

    @McpToolParam(description = "Label of the DLO")
    private String label;

    @McpToolParam(description = "Category of the DLO: Directory_Table, Engagement, Insights, Other, Profile")
    private String category;

    @McpToolParam(description = "Information about DLO fields")
    private List<DataLakeFieldInput> dataLakeFieldInputRepresentations;

    @McpToolParam(description = "Information about the data space")
    private List<DataSpaceInput> dataspaceInfo;

    @McpToolParam(description = "Name of the event date and time field (required when category is Engagement)", required = false)
    private String eventDateTimeFieldName;

    @McpToolParam(description = "Name of the org unit identifier field", required = false)
    private String orgUnitIdentifierFieldName;

    @McpToolParam(description = "Name of the record modified field", required = false)
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<DataLakeFieldInput> getDataLakeFieldInputRepresentations() {
        return dataLakeFieldInputRepresentations;
    }

    public void setDataLakeFieldInputRepresentations(List<DataLakeFieldInput> dataLakeFieldInputRepresentations) {
        this.dataLakeFieldInputRepresentations = dataLakeFieldInputRepresentations;
    }

    public List<DataSpaceInput> getDataspaceInfo() {
        return dataspaceInfo;
    }

    public void setDataspaceInfo(List<DataSpaceInput> dataspaceInfo) {
        this.dataspaceInfo = dataspaceInfo;
    }

    public String getEventDateTimeFieldName() {
        return eventDateTimeFieldName;
    }

    public void setEventDateTimeFieldName(String eventDateTimeFieldName) {
        this.eventDateTimeFieldName = eventDateTimeFieldName;
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
}
