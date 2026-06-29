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

/**
 * Request body for {@code POST /ssot/machine-learning/retrievers/{id}/configurations}.
 *
 * <p>The endpoint accepts a {@code MlRetrieverConfigurationBaseInputRepresentation}
 * (abstract). This class is a flat union over the concrete subtypes
 * {@code MlRetrieverConfigurationNoCodeInputRepresentation} and
 * {@code MlRetrieverConfigurationEnsembleInputRepresentation}; only the fields
 * relevant to the chosen {@code queryType} are populated, the rest are dropped
 * by {@link JsonInclude.Include#NON_NULL}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrieverConfigurationCreateRequest {

    // ---- MlRetrieverConfigurationBaseInputRepresentation (shared) ----

    @McpToolParam(description = "Optional Citation Configuration", required = false)
    private MlRetrieverCitationConfigurationInput citationConfiguration;

    @McpToolParam(description = "Semantic Search Definition id or name that the retriever configuration is for", required = false)
    private MlRetrieverSourceInput input;

    @McpToolParam(description = "Set this configuration to be active or not. At most one configuration can be active within the parent retriever", required = false)
    private Boolean isActive;

    @McpToolParam(description = "Enable retriever citation or not, if not present, treat it as false", required = false)
    private Boolean isCitationEnabled;

    @McpToolParam(description = "Number of retriever query results to return, positive", required = false)
    private Integer numberOfResults;

    @McpToolParam(description = "QueryType", required = false)
    private String queryType;

    @McpToolParam(description = "Retrieval mode", required = false)
    private String retrievalMode;

    // ---- MlRetrieverConfigurationNoCodeInputRepresentation ----

    @McpToolParam(description = "The list of output fields", required = false)
    private List<MlRetrieverOutputFieldInput> outputFields;

    @McpToolParam(description = "The query filter applied on the retriever query results", required = false)
    private CdpMlFilterInput queryFilter;

    @McpToolParam(description = "Web search provider type", required = false)
    private String webSearchProviderType;

    @McpToolParam(description = "A list of optional website filters to be applied", required = false)
    private List<String> websiteFilters;

    // ---- MlRetrieverConfigurationEnsembleInputRepresentation ----

    @McpToolParam(description = "A list of reference retrievers", required = false)
    private List<CdpAssetReferenceInput> referencedRetrievers;

    public MlRetrieverCitationConfigurationInput getCitationConfiguration() {
        return citationConfiguration;
    }

    public void setCitationConfiguration(MlRetrieverCitationConfigurationInput citationConfiguration) {
        this.citationConfiguration = citationConfiguration;
    }

    public MlRetrieverSourceInput getInput() {
        return input;
    }

    public void setInput(MlRetrieverSourceInput input) {
        this.input = input;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsCitationEnabled() {
        return isCitationEnabled;
    }

    public void setIsCitationEnabled(Boolean isCitationEnabled) {
        this.isCitationEnabled = isCitationEnabled;
    }

    public Integer getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(Integer numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getRetrievalMode() {
        return retrievalMode;
    }

    public void setRetrievalMode(String retrievalMode) {
        this.retrievalMode = retrievalMode;
    }

    public List<MlRetrieverOutputFieldInput> getOutputFields() {
        return outputFields;
    }

    public void setOutputFields(List<MlRetrieverOutputFieldInput> outputFields) {
        this.outputFields = outputFields;
    }

    public CdpMlFilterInput getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(CdpMlFilterInput queryFilter) {
        this.queryFilter = queryFilter;
    }

    public String getWebSearchProviderType() {
        return webSearchProviderType;
    }

    public void setWebSearchProviderType(String webSearchProviderType) {
        this.webSearchProviderType = webSearchProviderType;
    }

    public List<String> getWebsiteFilters() {
        return websiteFilters;
    }

    public void setWebsiteFilters(List<String> websiteFilters) {
        this.websiteFilters = websiteFilters;
    }

    public List<CdpAssetReferenceInput> getReferencedRetrievers() {
        return referencedRetrievers;
    }

    public void setReferencedRetrievers(List<CdpAssetReferenceInput> referencedRetrievers) {
        this.referencedRetrievers = referencedRetrievers;
    }
}
