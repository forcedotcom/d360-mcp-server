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
 * Request body for {@code POST /ssot/machine-learning/retrievers}.
 *
 * <p>Mirrors {@code MlRetrieverInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrieverCreateRequest {

    @McpToolParam(description = "Retriever configuration", required = false)
    private RetrieverConfigurationCreateRequest configuration;

    @McpToolParam(description = "Retriever data source type. One of DataCloudAsset, RealTimeSearchIndex, SearchIndex, Web", required = false)
    private String dataSourceType;

    @McpToolParam(description = "DataSpaces that this retriever belongs to", required = false)
    private List<String> dataSpaces;

    @McpToolParam(description = "Description", required = false)
    private String description;

    @McpToolParam(description = "Label", required = false)
    private String label;

    @McpToolParam(description = "Retriever owner type", required = false)
    private String ownerType;

    public RetrieverConfigurationCreateRequest getConfiguration() {
        return configuration;
    }

    public void setConfiguration(RetrieverConfigurationCreateRequest configuration) {
        this.configuration = configuration;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public List<String> getDataSpaces() {
        return dataSpaces;
    }

    public void setDataSpaces(List<String> dataSpaces) {
        this.dataSpaces = dataSpaces;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }
}
