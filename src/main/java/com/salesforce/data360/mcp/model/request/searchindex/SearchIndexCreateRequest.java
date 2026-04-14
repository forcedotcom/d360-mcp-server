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

import java.util.List;

/**
 * Request body for creating or updating a search index (semantic search).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchIndexCreateRequest {

    @McpToolParam(description = "ID of the asset", required = false)
    private String id;

    @McpToolParam(description = "Name of the asset", required = false)
    private String name;

    @McpToolParam(description = "Namespace of the asset", required = false)
    private String namespace;

    @McpToolParam(description = "Activation status of the semantic search record", required = false)
    private String activationStatus;

    @McpToolParam(description = "Developer name of the attachment DMO (e.g., loanAgreement__dlm)", required = false)
    private String attachmentDmoDeveloperName;

    @McpToolParam(description = "Developer name of the DMO used for chunking, excluding __dlm suffix")
    private String chunkDmoDeveloperName;

    @McpToolParam(description = "Name of the DMO used for chunking")
    private String chunkDmoName;

    @McpToolParam(description = "Chunking configuration. IMPORTANT: include both fieldLevelConfigurations and fileLevelConfiguration "
        + "ONLY when attachmentDmoDeveloperName is set. Otherwise use fieldLevelConfigurations alone for structured DMOs, "
        + "or fileLevelConfiguration alone for unstructured DMOs/Directory_Table. "
        + "Get valid config ids from d360_search_index_config.")
    private ChunkingConfigInput chunkingConfiguration;

    @McpToolParam(description = "Description of semantic search record")
    private String description;

    @McpToolParam(description = "Developer name of the semantic search record")
    private String developerName;

    @McpToolParam(description = "Index configuration for keyword search. "
        + "recordFieldConfiguration for searchable+retrievable fields, "
        + "contextFieldConfiguration for retrievable-only fields, "
        + "shouldVectorizeSearchableFields to vectorize record chunks.", required = false)
    private IndexConfigInput indexConfiguration;

    @McpToolParam(description = "Label of semantic search record")
    private String label;

    @McpToolParam(description = "Processing type: NEAR_REALTIME or REALTIME", required = false)
    private String processingType;

    @McpToolParam(description = "List of ranking configurations for hybrid search", required = false)
    private List<RankingConfigInput> rankingConfigurations;

    @McpToolParam(description = "Type of the search index: HYBRID or VECTOR")
    private String searchType;

    @McpToolParam(description = "Developer name of the source DMO (e.g., loanAgreement__dlm)")
    private String sourceDmoDeveloperName;

    @McpToolParam(description = "Transcribe output DMO developer name", required = false)
    private String transcribeDmoDeveloperName;

    @McpToolParam(description = "Transcribe output DMO name", required = false)
    private String transcribeDmoName;

    @McpToolParam(description = "Transcribe output DMO ID", required = false)
    private String transcribeDmoId;

    @McpToolParam(description = "Transform configurations (e.g., transcription). "
        + "Get valid config ids from d360_search_index_config.")
    private List<TransformConfigInput> transformConfigurations;

    @McpToolParam(description = "Developer name of the vector DMO, excluding __dlm suffix")
    private String vectorDmoDeveloperName;

    @McpToolParam(description = "Name of the vector DMO")
    private String vectorDmoName;

    @McpToolParam(description = "Vector embedding with related DMO fields. "
        + "Use empty list for vectorEmbeddingRelatedFields if no related fields needed.")
    private VectorEmbeddingInput vectorEmbedding;

    @McpToolParam(description = "Vector embedding configuration with embeddingModel, index, and similarityMetric. "
        + "Get valid ids from d360_search_index_config.")
    private VectorEmbeddingConfigInput vectorEmbeddingConfiguration;

    @McpToolParam(description = "Parsing configurations for document processing. "
        + "Valid ids: system_default, parse_documents_using_llm, docling_parsing.", required = false)
    private List<ProcessingConfigInput> parsingConfigurations;

    @McpToolParam(description = "Pre-processing configurations. "
        + "Valid ids: system_default, pre_process_infographics_using_llm.", required = false)
    private List<ProcessingConfigInput> preProcessingConfigurations;

    @McpToolParam(description = "Post-processing configurations. "
        + "Get valid ids from d360_search_index_config.", required = false)
    private List<ProcessingConfigInput> postProcessingConfigurations;

    @McpToolParam(description = "Semantic search ranking configurations for hybrid search with retrieval types and weights", required = false)
    private List<SemanticSearchRankingConfigInput> semanticSearchRankingConfigurations;

    @McpToolParam(description = "Rule configuration with label, priority rules with conditions, and sorting rules", required = false)
    private RuleConfigInput ruleConfiguration;

    @McpToolParam(description = "Source application that created this search index", required = false)
    private String sourceApp;

    @McpToolParam(description = "Developer name of the source application", required = false)
    private String sourceAppDeveloperName;

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public String getActivationStatus() { return activationStatus; }
    public void setActivationStatus(String activationStatus) { this.activationStatus = activationStatus; }

    public String getAttachmentDmoDeveloperName() { return attachmentDmoDeveloperName; }
    public void setAttachmentDmoDeveloperName(String attachmentDmoDeveloperName) { this.attachmentDmoDeveloperName = attachmentDmoDeveloperName; }

    public String getChunkDmoDeveloperName() { return chunkDmoDeveloperName; }
    public void setChunkDmoDeveloperName(String chunkDmoDeveloperName) { this.chunkDmoDeveloperName = chunkDmoDeveloperName; }

    public String getChunkDmoName() { return chunkDmoName; }
    public void setChunkDmoName(String chunkDmoName) { this.chunkDmoName = chunkDmoName; }

    public ChunkingConfigInput getChunkingConfiguration() { return chunkingConfiguration; }
    public void setChunkingConfiguration(ChunkingConfigInput chunkingConfiguration) { this.chunkingConfiguration = chunkingConfiguration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDeveloperName() { return developerName; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }

    public IndexConfigInput getIndexConfiguration() { return indexConfiguration; }
    public void setIndexConfiguration(IndexConfigInput indexConfiguration) { this.indexConfiguration = indexConfiguration; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getProcessingType() { return processingType; }
    public void setProcessingType(String processingType) { this.processingType = processingType; }

    public List<RankingConfigInput> getRankingConfigurations() { return rankingConfigurations; }
    public void setRankingConfigurations(List<RankingConfigInput> rankingConfigurations) { this.rankingConfigurations = rankingConfigurations; }

    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }

    public String getSourceDmoDeveloperName() { return sourceDmoDeveloperName; }
    public void setSourceDmoDeveloperName(String sourceDmoDeveloperName) { this.sourceDmoDeveloperName = sourceDmoDeveloperName; }

    public String getTranscribeDmoDeveloperName() { return transcribeDmoDeveloperName; }
    public void setTranscribeDmoDeveloperName(String transcribeDmoDeveloperName) { this.transcribeDmoDeveloperName = transcribeDmoDeveloperName; }

    public String getTranscribeDmoName() { return transcribeDmoName; }
    public void setTranscribeDmoName(String transcribeDmoName) { this.transcribeDmoName = transcribeDmoName; }

    public String getTranscribeDmoId() { return transcribeDmoId; }
    public void setTranscribeDmoId(String transcribeDmoId) { this.transcribeDmoId = transcribeDmoId; }

    public List<TransformConfigInput> getTransformConfigurations() { return transformConfigurations; }
    public void setTransformConfigurations(List<TransformConfigInput> transformConfigurations) { this.transformConfigurations = transformConfigurations; }

    public String getVectorDmoDeveloperName() { return vectorDmoDeveloperName; }
    public void setVectorDmoDeveloperName(String vectorDmoDeveloperName) { this.vectorDmoDeveloperName = vectorDmoDeveloperName; }

    public String getVectorDmoName() { return vectorDmoName; }
    public void setVectorDmoName(String vectorDmoName) { this.vectorDmoName = vectorDmoName; }

    public VectorEmbeddingInput getVectorEmbedding() { return vectorEmbedding; }
    public void setVectorEmbedding(VectorEmbeddingInput vectorEmbedding) { this.vectorEmbedding = vectorEmbedding; }

    public VectorEmbeddingConfigInput getVectorEmbeddingConfiguration() { return vectorEmbeddingConfiguration; }
    public void setVectorEmbeddingConfiguration(VectorEmbeddingConfigInput vectorEmbeddingConfiguration) { this.vectorEmbeddingConfiguration = vectorEmbeddingConfiguration; }

    public List<ProcessingConfigInput> getParsingConfigurations() { return parsingConfigurations; }
    public void setParsingConfigurations(List<ProcessingConfigInput> parsingConfigurations) { this.parsingConfigurations = parsingConfigurations; }

    public List<ProcessingConfigInput> getPreProcessingConfigurations() { return preProcessingConfigurations; }
    public void setPreProcessingConfigurations(List<ProcessingConfigInput> preProcessingConfigurations) { this.preProcessingConfigurations = preProcessingConfigurations; }

    public List<ProcessingConfigInput> getPostProcessingConfigurations() { return postProcessingConfigurations; }
    public void setPostProcessingConfigurations(List<ProcessingConfigInput> postProcessingConfigurations) { this.postProcessingConfigurations = postProcessingConfigurations; }

    public List<SemanticSearchRankingConfigInput> getSemanticSearchRankingConfigurations() { return semanticSearchRankingConfigurations; }
    public void setSemanticSearchRankingConfigurations(List<SemanticSearchRankingConfigInput> semanticSearchRankingConfigurations) { this.semanticSearchRankingConfigurations = semanticSearchRankingConfigurations; }

    public RuleConfigInput getRuleConfiguration() { return ruleConfiguration; }
    public void setRuleConfiguration(RuleConfigInput ruleConfiguration) { this.ruleConfiguration = ruleConfiguration; }

    public String getSourceApp() { return sourceApp; }
    public void setSourceApp(String sourceApp) { this.sourceApp = sourceApp; }

    public String getSourceAppDeveloperName() { return sourceAppDeveloperName; }
    public void setSourceAppDeveloperName(String sourceAppDeveloperName) { this.sourceAppDeveloperName = sourceAppDeveloperName; }
}
