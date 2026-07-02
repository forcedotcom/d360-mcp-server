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
package com.salesforce.data360.mcp.model.request.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;
import java.util.Map;

/**
 * Request body for {@code POST /ssot/connections} (and {@code PUT /ssot/connections/{id}}).
 *
 * <p>Flat union mirroring the abstract {@code ConnectionInputRepresentation} parent and every
 * concrete subclass declared in the UDF: connector-type subclasses (DataConnection,
 * AccountEngagement, Crm, IngestApi, MarketingCloud, StreamingApp, UnstructuredIndex) plus the
 * collection/filter subclasses (ConnectionDbSchemaCollection, ResourceFilters,
 * ResourceFilterByProperty). Only the fields that apply to the chosen {@code connectorType}
 * should be populated; the rest are omitted from the wire via {@code @JsonInclude(NON_NULL)}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionCreateRequest {

    @NotBlank
    @McpToolParam(description = "ype of the connector. For example, AwsRdsPostgres, AzureBlob, Databricks, Gcs, IngestApi, SalesforceDotCom, SalesforceMarketingCloud, Sftp, StreamingApp, and so forth.")
    private String connectorType;

    @McpToolParam(description = "Display label for the connection", required = false)
    private String label;

    @McpToolParam(description = "Developer name for the connection", required = false)
    private String name;

    // ----- DataConnection (parent-id="ConnectionInputRepresentation") -----

    @McpToolParam(description = "DataConnection only: connector capabilities", required = false)
    private Map<String, Boolean> capabilities;

    @McpToolParam(description = "DataConnection only: connection credentials", required = false)
    private List<DataConnectionParameterInput> credentials;

    @McpToolParam(description = "DataConnection only: connection method. DataConnectionMethodEnum.", required = false)
    private String method;

    @McpToolParam(description = "DataConnection only: connection parameters", required = false)
    private List<DataConnectionParameterInput> parameters;

    // ----- AccountEngagementConnection -----

    @McpToolParam(description = "AccountEngagement only: data stream type. AccountEngagementDataStreamTypeEnum.", required = false)
    private String dataStreamType;

    @McpToolParam(description = "AccountEngagement only: Pardot Tenant Id", required = false)
    private String pardotTenantId;

    // ----- CrmConnection -----

    @McpToolParam(description = "CRM only: Organization ID", required = false)
    private String organizationId;

    // ----- StreamingAppConnection -----

    @McpToolParam(description = "StreamingApp only: streaming app sub-type. StreamingAppSubTypeEnum.", required = false)
    private String streamingAppSubType;

    @McpToolParam(description = "StreamingApp only: streaming app connector type. StreamingAppDataConnectorTypeEnum.", required = false)
    private String streamingAppType;

    // ----- UnstructuredIndex -----

    @McpToolParam(description = "UnstructuredIndex only: additional metadata attributes", required = false)
    private Map<String, Object> additionalAttributes;

    @McpToolParam(description = "UnstructuredIndex only: prefilter container id for stored indexes/chunks", required = false)
    private String containerId;

    @McpToolParam(description = "UnstructuredIndex only: file names to chunk and index", required = false)
    private List<String> filePaths;

    @McpToolParam(description = "UnstructuredIndex only: Search Index API name where chunking/embedding metadata is stored", required = false)
    private String searchIndexApiName;

    // ----- ConnectionDbSchemaCollection -----

    @McpToolParam(description = "ConnectionDbSchemaCollection only: connector specific properties required to fetch a list of database schemas (e.g., {\"database\":\"MY_DB\"})", required = false)
    private Map<String, String> advancedAttributes;

    // ----- ResourceFilters -----

    @McpToolParam(description = "ResourceFilters only: list of property filters to apply", required = false)
    private List<ResourceFilterByPropertyInput> filtersByProperty;

    // ----- ResourceFilterByProperty -----

    @McpToolParam(description = "ResourceFilterByProperty only: filter operator. One of EqualsOp, LikeOp, SubstrOp.", required = false)
    private String filterOperator;

    @McpToolParam(description = "ResourceFilterByProperty only: values to filter on based on the filterOperator", required = false)
    private List<String> values;

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
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

    public Map<String, Boolean> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Boolean> capabilities) {
        this.capabilities = capabilities;
    }

    public List<DataConnectionParameterInput> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<DataConnectionParameterInput> credentials) {
        this.credentials = credentials;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<DataConnectionParameterInput> getParameters() {
        return parameters;
    }

    public void setParameters(List<DataConnectionParameterInput> parameters) {
        this.parameters = parameters;
    }

    public String getDataStreamType() {
        return dataStreamType;
    }

    public void setDataStreamType(String dataStreamType) {
        this.dataStreamType = dataStreamType;
    }

    public String getPardotTenantId() {
        return pardotTenantId;
    }

    public void setPardotTenantId(String pardotTenantId) {
        this.pardotTenantId = pardotTenantId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getStreamingAppSubType() {
        return streamingAppSubType;
    }

    public void setStreamingAppSubType(String streamingAppSubType) {
        this.streamingAppSubType = streamingAppSubType;
    }

    public String getStreamingAppType() {
        return streamingAppType;
    }

    public void setStreamingAppType(String streamingAppType) {
        this.streamingAppType = streamingAppType;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getSearchIndexApiName() {
        return searchIndexApiName;
    }

    public void setSearchIndexApiName(String searchIndexApiName) {
        this.searchIndexApiName = searchIndexApiName;
    }

    public Map<String, String> getAdvancedAttributes() {
        return advancedAttributes;
    }

    public void setAdvancedAttributes(Map<String, String> advancedAttributes) {
        this.advancedAttributes = advancedAttributes;
    }

    public List<ResourceFilterByPropertyInput> getFiltersByProperty() {
        return filtersByProperty;
    }

    public void setFiltersByProperty(List<ResourceFilterByPropertyInput> filtersByProperty) {
        this.filtersByProperty = filtersByProperty;
    }

    public String getFilterOperator() {
        return filterOperator;
    }

    public void setFilterOperator(String filterOperator) {
        this.filterOperator = filterOperator;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
