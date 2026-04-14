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
 * Request body for creating a retriever.
 * Maps to MlRetrieverInputRepresentation in core.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrieverCreateRequest {

    @McpToolParam(description = "Label for the retriever")
    private String label;

    @McpToolParam(description = "Description of the retriever", required = false)
    private String description;

    @McpToolParam(description = "REQUIRED. Retriever configuration object with queryType-specific fields. "
        + "Must include 'queryType' (NoCode, ProCode, or Ensemble). "
        + "For NoCode: REQUIRED: input ({id: '<search_index_id>'}), outputFields (REQUIRED, non-empty array of "
        + "{relatedDmoName: '<dmo_name>', relatedDmoFieldName: '<field_name>', label: '<display_label>', relationships: []}), "
        + "numberOfResults (top-K integer), isActive (boolean). Optional: queryFilter, isCitationEnabled. "
        + "For Ensemble: include referencedRetrievers (list of retriever references), outputFields. "
        + "For ProCode: include input, outputFields, queryFilterFields, resultFilterFields, retrieverQueryTemplate.")
    private Map<String, Object> configuration;

    @McpToolParam(description = "Data source type for the retriever (e.g., SearchIndex, Web)", required = false)
    private String dataSourceType;

    @McpToolParam(description = "List of dataspace names that this retriever belongs to", required = false)
    private List<String> dataSpaces;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getConfiguration() { return configuration; }
    public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }

    public String getDataSourceType() { return dataSourceType; }
    public void setDataSourceType(String dataSourceType) { this.dataSourceType = dataSourceType; }

    public List<String> getDataSpaces() { return dataSpaces; }
    public void setDataSpaces(List<String> dataSpaces) { this.dataSpaces = dataSpaces; }
}
