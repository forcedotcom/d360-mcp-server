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
package com.salesforce.data360.mcp.model.request.dataaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/** Mirrors ConnectApi.DataActionSearchConfigInputRepresentation. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionSearchConfigInput {

    @McpToolParam(description = "Score threshold.", required = false)
    private Double scoreThreshold;

    @McpToolParam(description = "Search index API name.", required = false)
    private String searchIndexApiName;

    @McpToolParam(description = "Search query.", required = false)
    private String searchQuery;

    @McpToolParam(description = "Top results limit.", required = false)
    private Integer topResultsLimit;

    public Double getScoreThreshold() { return scoreThreshold; }
    public void setScoreThreshold(Double scoreThreshold) { this.scoreThreshold = scoreThreshold; }
    public String getSearchIndexApiName() { return searchIndexApiName; }
    public void setSearchIndexApiName(String searchIndexApiName) { this.searchIndexApiName = searchIndexApiName; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    public Integer getTopResultsLimit() { return topResultsLimit; }
    public void setTopResultsLimit(Integer topResultsLimit) { this.topResultsLimit = topResultsLimit; }
}
