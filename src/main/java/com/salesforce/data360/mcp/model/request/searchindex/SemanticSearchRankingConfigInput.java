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
package com.salesforce.data360.mcp.model.request.searchindex;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Mirrors {@code DataSmntcSearchRankConfigDetailsInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SemanticSearchRankingConfigInput {

    @McpToolParam(description = "Configuration type. ConfigurationType enum.", required = false)
    private String configurationType;

    @McpToolParam(description = "Developer name of the semantic search ranking configuration field", required = false)
    private String developerName;

    @McpToolParam(description = "Ranking function. RankingFunction enum.", required = false)
    private String rankingFunction;

    @McpToolParam(description = "Retrieval type. RetrievalType enum.", required = false)
    private String retrievalType;

    @McpToolParam(description = "Retrieval weight", required = false)
    private String retrievalWeight;

    @McpToolParam(description = "Transformation function", required = false)
    private String transformationFunction;

    public String getConfigurationType() { return configurationType; }
    public void setConfigurationType(String configurationType) { this.configurationType = configurationType; }

    public String getDeveloperName() { return developerName; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }

    public String getRankingFunction() { return rankingFunction; }
    public void setRankingFunction(String rankingFunction) { this.rankingFunction = rankingFunction; }

    public String getRetrievalType() { return retrievalType; }
    public void setRetrievalType(String retrievalType) { this.retrievalType = retrievalType; }

    public String getRetrievalWeight() { return retrievalWeight; }
    public void setRetrievalWeight(String retrievalWeight) { this.retrievalWeight = retrievalWeight; }

    public String getTransformationFunction() { return transformationFunction; }
    public void setTransformationFunction(String transformationFunction) { this.transformationFunction = transformationFunction; }
}
