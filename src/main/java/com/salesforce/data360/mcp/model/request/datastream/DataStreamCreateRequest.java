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
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating a Data Stream.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataStreamCreateRequest {

    @NotBlank
    @McpToolParam(description = "Name of the data stream")
    private String name;

    @NotBlank
    @McpToolParam(description = "Label of the data stream")
    private String label;

    @Valid
    @NotNull
    @McpToolParam(description = "Connector information")
    private ConnectorInput connectorInfo;

    @Valid
    @McpToolParam(description = "Data lake object info")
    private DataLakeObjectInput dataLakeObjectInfo;

    @Valid
    @McpToolParam(description = "Array of field mappings")
    private List<DataStreamFieldMappingInput> mappings;

    @Valid
    @McpToolParam(description = "Refresh configuration")
    private RefreshConfigInput refreshConfig;

    @Valid
    @McpToolParam(description = "Array of source fields")
    private List<DataStreamSourceFieldInput> sourceFields;

    @McpToolParam(description = "Datasource name", required = false)
    private String datasource;

    @McpToolParam(description = "Type of data stream", required = false)
    private String datastreamType;

    @Valid
    @McpToolParam(description = "Existing DLO info", required = false)
    private ExistingDataLakeObjectInput existingDataLakeObjectInfo;

    @McpToolParam(description = "Data access mode: Direct_Access, Ingest", required = false)
    private String dataAccessMode;

    @McpToolParam(description = "Advanced attributes such as directory, file name, or file parser options", required = false)
    private Map<String, Object> advancedAttributes;

    @Valid
    @McpToolParam(description = "Currency ISO code info", required = false)
    private CurrencyIsoCodeInput currencyIsoCodeInfo;

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

    public ConnectorInput getConnectorInfo() {
        return connectorInfo;
    }

    public void setConnectorInfo(ConnectorInput connectorInfo) {
        this.connectorInfo = connectorInfo;
    }

    public DataLakeObjectInput getDataLakeObjectInfo() {
        return dataLakeObjectInfo;
    }

    public void setDataLakeObjectInfo(DataLakeObjectInput dataLakeObjectInfo) {
        this.dataLakeObjectInfo = dataLakeObjectInfo;
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

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDatastreamType() {
        return datastreamType;
    }

    public void setDatastreamType(String datastreamType) {
        this.datastreamType = datastreamType;
    }

    public ExistingDataLakeObjectInput getExistingDataLakeObjectInfo() {
        return existingDataLakeObjectInfo;
    }

    public void setExistingDataLakeObjectInfo(ExistingDataLakeObjectInput existingDataLakeObjectInfo) {
        this.existingDataLakeObjectInfo = existingDataLakeObjectInfo;
    }

    public String getDataAccessMode() {
        return dataAccessMode;
    }

    public void setDataAccessMode(String dataAccessMode) {
        this.dataAccessMode = dataAccessMode;
    }

    public Map<String, Object> getAdvancedAttributes() {
        return advancedAttributes;
    }

    public void setAdvancedAttributes(Map<String, Object> advancedAttributes) {
        this.advancedAttributes = advancedAttributes;
    }

    public CurrencyIsoCodeInput getCurrencyIsoCodeInfo() {
        return currencyIsoCodeInfo;
    }

    public void setCurrencyIsoCodeInfo(CurrencyIsoCodeInput currencyIsoCodeInfo) {
        this.currencyIsoCodeInfo = currencyIsoCodeInfo;
    }
}
