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
 * Request body for updating a Data Stream.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataStreamPatchRequest {

    @McpToolParam(description = "Label of the data stream", required = false)
    private String label;

    @McpToolParam(description = "Type of data stream", required = false)
    private String datastreamType;

    @McpToolParam(description = "Connector patch information", required = false)
    private ConnectorPatchInput connectorPatchInfo;

    @McpToolParam(description = "Array of field mappings", required = false)
    private List<DataStreamFieldMappingInput> mappings;

    @McpToolParam(description = "Refresh configuration", required = false)
    private RefreshConfigInput refreshConfig;

    @McpToolParam(description = "Array of source fields", required = false)
    private List<DataStreamSourceFieldInput> sourceFields;

    @McpToolParam(description = "Data lake object info", required = false)
    private DataLakeObjectInput dataLakeObjectInfo;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDatastreamType() {
        return datastreamType;
    }

    public void setDatastreamType(String datastreamType) {
        this.datastreamType = datastreamType;
    }

    public ConnectorPatchInput getConnectorPatchInfo() {
        return connectorPatchInfo;
    }

    public void setConnectorPatchInfo(ConnectorPatchInput connectorPatchInfo) {
        this.connectorPatchInfo = connectorPatchInfo;
    }

    public List<DataStreamFieldMappingInput> getMappings() {
        return mappings;
    }

    public void setMappings(List<DataStreamFieldMappingInput> mappings) {
        this.mappings = mappings;
    }

    public RefreshConfigInput getRefreshConfig() {
        return refreshConfig;
    }

    public void setRefreshConfig(RefreshConfigInput refreshConfig) {
        this.refreshConfig = refreshConfig;
    }

    public List<DataStreamSourceFieldInput> getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(List<DataStreamSourceFieldInput> sourceFields) {
        this.sourceFields = sourceFields;
    }

    public DataLakeObjectInput getDataLakeObjectInfo() {
        return dataLakeObjectInfo;
    }

    public void setDataLakeObjectInfo(DataLakeObjectInput dataLakeObjectInfo) {
        this.dataLakeObjectInfo = dataLakeObjectInfo;
    }
}
