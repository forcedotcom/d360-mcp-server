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
 * Vector embedding model and index configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VectorEmbeddingConfigInput {

    @McpToolParam(description = "Embedding model configuration (e.g., id: e5_large_v2 with dimension and max_token_limit userValues)")
    private ConfigInput embeddingModel;

    @McpToolParam(description = "Index configuration (e.g., id: HNSW with hnswEfConstruction and M userValues)")
    private ConfigInput index;

    @McpToolParam(description = "Similarity metric: COSINE, DOT_PRODUCT, or EUCLIDEAN")
    private String similarityMetric;

    public ConfigInput getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(ConfigInput embeddingModel) { this.embeddingModel = embeddingModel; }

    public ConfigInput getIndex() { return index; }
    public void setIndex(ConfigInput index) { this.index = index; }

    public String getSimilarityMetric() { return similarityMetric; }
    public void setSimilarityMetric(String similarityMetric) { this.similarityMetric = similarityMetric; }
}
