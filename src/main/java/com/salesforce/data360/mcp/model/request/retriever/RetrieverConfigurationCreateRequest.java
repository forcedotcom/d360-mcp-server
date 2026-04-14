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
package com.salesforce.data360.mcp.model.request.retriever;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating a retriever configuration (version).
 * Maps to MlRetrieverConfigurationBaseInputRepresentation in core.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrieverConfigurationCreateRequest {

    @McpToolParam(description = "Query type: NoCode, ProCode, or Ensemble")
    private String queryType;

    @McpToolParam(description = "Input source reference (object with name/id of the semantic search definition)", required = false)
    private Map<String, Object> input;

    @McpToolParam(description = "Whether this configuration should be the active version", required = false)
    private Boolean isActive;

    @McpToolParam(description = "Maximum number of results to return", required = false)
    private Integer numberOfResults;

    @McpToolParam(description = "Whether citation is enabled for this configuration", required = false)
    private Boolean isCitationEnabled;

    @McpToolParam(description = "Citation configuration with type, mappedLabelField, baseUrl, mappedUrlField", required = false)
    private Map<String, Object> citationConfiguration;

    @McpToolParam(description = "Output fields for NoCode/ProCode types. List of objects with relatedDmoName, relatedDmoFieldName, label, relationships", required = false)
    private List<Map<String, Object>> outputFields;

    @McpToolParam(description = "Query filter for NoCode type", required = false)
    private Map<String, Object> queryFilter;

    @McpToolParam(description = "Web search provider type (e.g., BrightData) for NoCode web retrievers", required = false)
    private String webSearchProviderType;

    @McpToolParam(description = "Website URL filters for web retrievers", required = false)
    private List<String> websiteFilters;

    @McpToolParam(description = "Referenced retrievers for Ensemble type. List of objects with name/id of child retrievers", required = false)
    private List<Map<String, Object>> referencedRetrievers;

    @McpToolParam(description = "Query filter fields for ProCode type", required = false)
    private List<Map<String, Object>> queryFilterFields;

    @McpToolParam(description = "Result filter fields for ProCode type", required = false)
    private List<Map<String, Object>> resultFilterFields;

    @McpToolParam(description = "Retriever query template for ProCode type", required = false)
    private String retrieverQueryTemplate;

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public Map<String, Object> getInput() { return input; }
    public void setInput(Map<String, Object> input) { this.input = input; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getNumberOfResults() { return numberOfResults; }
    public void setNumberOfResults(Integer numberOfResults) { this.numberOfResults = numberOfResults; }

    public Boolean getIsCitationEnabled() { return isCitationEnabled; }
    public void setIsCitationEnabled(Boolean isCitationEnabled) { this.isCitationEnabled = isCitationEnabled; }

    public Map<String, Object> getCitationConfiguration() { return citationConfiguration; }
    public void setCitationConfiguration(Map<String, Object> citationConfiguration) { this.citationConfiguration = citationConfiguration; }

    public List<Map<String, Object>> getOutputFields() { return outputFields; }
    public void setOutputFields(List<Map<String, Object>> outputFields) { this.outputFields = outputFields; }

    public Map<String, Object> getQueryFilter() { return queryFilter; }
    public void setQueryFilter(Map<String, Object> queryFilter) { this.queryFilter = queryFilter; }

    public String getWebSearchProviderType() { return webSearchProviderType; }
    public void setWebSearchProviderType(String webSearchProviderType) { this.webSearchProviderType = webSearchProviderType; }

    public List<String> getWebsiteFilters() { return websiteFilters; }
    public void setWebsiteFilters(List<String> websiteFilters) { this.websiteFilters = websiteFilters; }

    public List<Map<String, Object>> getReferencedRetrievers() { return referencedRetrievers; }
    public void setReferencedRetrievers(List<Map<String, Object>> referencedRetrievers) { this.referencedRetrievers = referencedRetrievers; }

    public List<Map<String, Object>> getQueryFilterFields() { return queryFilterFields; }
    public void setQueryFilterFields(List<Map<String, Object>> queryFilterFields) { this.queryFilterFields = queryFilterFields; }

    public List<Map<String, Object>> getResultFilterFields() { return resultFilterFields; }
    public void setResultFilterFields(List<Map<String, Object>> resultFilterFields) { this.resultFilterFields = resultFilterFields; }

    public String getRetrieverQueryTemplate() { return retrieverQueryTemplate; }
    public void setRetrieverQueryTemplate(String retrieverQueryTemplate) { this.retrieverQueryTemplate = retrieverQueryTemplate; }
}
