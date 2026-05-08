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
package com.salesforce.data360.mcp.tools;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.searchindex.ChunkingConfigInput;
import com.salesforce.data360.mcp.model.request.searchindex.SearchIndexCreateRequest;
import com.salesforce.data360.mcp.model.request.searchindex.TransformConfigInput;
import com.salesforce.data360.mcp.model.request.searchindex.VectorEmbeddingConfigInput;
import com.salesforce.data360.mcp.model.request.searchindex.VectorEmbeddingInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchIndexToolsTest {

    @Mock
    private Data360Client client;

    private SearchIndexTools searchIndexTools;

    @BeforeEach
    void setUp() {
        searchIndexTools = new SearchIndexTools(client);
    }

    // ============================================================
    // List Search Indexes
    // ============================================================

    @Test
    void testListSearchIndexes_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "semanticSearchDefinitionDetails", List.of(
                Map.of("id", "idx-123", "label", "Case Search Index")
            ),
            "totalSize", 1
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = searchIndexTools.listSearchIndexes();

        // Then
        assertThat(result).contains("idx-123", "Case Search Index");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/search-index");
    }

    @Test
    void testListSearchIndexes_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(403, "User does not have permission to access", "/ssot/search-index"));

        // When
        String result = searchIndexTools.listSearchIndexes();

        // Then
        assertThat(result).contains("error", "403");
    }

    @Test
    void testListSearchIndexes_connectionError() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException("Connection timeout", new RuntimeException("timeout")));

        // When
        String result = searchIndexTools.listSearchIndexes();

        // Then
        assertThat(result).contains("error", "Connection timeout");
    }

    // ============================================================
    // Create Search Index
    // ============================================================

    @Test
    void testCreateSearchIndex_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "idx-456",
            "vectorEmbedding", Map.of("vectorEmbeddingRelatedFields", List.of())
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        SearchIndexCreateRequest request = new SearchIndexCreateRequest();
        request.setLabel("Case_search_index");
        request.setDeveloperName("Case_search_index");
        request.setSourceDmoDeveloperName("ssot__Case__dlm");
        request.setChunkDmoName("Case_search_index chunk");
        request.setChunkDmoDeveloperName("Case_search_index_chunk");
        request.setVectorDmoName("Case_search_index index");
        request.setVectorDmoDeveloperName("Case_search_index_index");
        request.setSearchType("VECTOR");
        VectorEmbeddingInput vectorEmbedding = new VectorEmbeddingInput();
        vectorEmbedding.setVectorEmbeddingRelatedFields(List.of());
        request.setVectorEmbedding(vectorEmbedding);

        ChunkingConfigInput chunkingConfig = new ChunkingConfigInput();
        chunkingConfig.setFieldLevelConfigurations(List.of());
        request.setChunkingConfiguration(chunkingConfig);

        VectorEmbeddingConfigInput vecConfig = new VectorEmbeddingConfigInput();
        vecConfig.setSimilarityMetric("COSINE");
        request.setVectorEmbeddingConfiguration(vecConfig);

        TransformConfigInput transformConfig = new TransformConfigInput();
        transformConfig.setTransformType("CHUNKING");
        request.setTransformConfigurations(List.of(transformConfig));

        String result = searchIndexTools.createSearchIndex(request);

        // Then
        assertThat(result).contains("idx-456");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/search-index");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Case_search_index");
        assertThat(bodyCaptor.getValue()).containsEntry("searchType", "VECTOR");
        assertThat(bodyCaptor.getValue()).containsEntry("sourceDmoDeveloperName", "ssot__Case__dlm");
    }

    @Test
    void testCreateSearchIndex_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad request", "/ssot/search-index"));

        // When
        SearchIndexCreateRequest request = new SearchIndexCreateRequest();
        request.setLabel("Bad Index");
        request.setSearchType("VECTOR");

        String result = searchIndexTools.createSearchIndex(request);

        // Then
        assertThat(result).contains("error", "Bad request", "400");
    }

    // ============================================================
    // Get Search Index Config
    // ============================================================

    @Test
    void testGetSearchIndexConfig_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "config", "{\"version\":\"1.0\"}"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = searchIndexTools.getSearchIndexConfig();

        // Then
        assertThat(result).contains("config");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/search-index/config");
    }

    @Test
    void testGetSearchIndexConfig_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Server error", "/ssot/search-index/config"));

        // When
        String result = searchIndexTools.getSearchIndexConfig();

        // Then
        assertThat(result).contains("error", "500");
    }

    // ============================================================
    // Get Search Index by ID
    // ============================================================

    @Test
    void testGetSearchIndex_success() {
        // Given
        String searchIndexId = "idx-123";
        Map<String, Object> mockResponse = Map.of(
            "semanticSearchDefinitionDetails", List.of(
                Map.of("id", searchIndexId, "label", "Case Search Index", "searchType", "VECTOR")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = searchIndexTools.getSearchIndex(searchIndexId);

        // Then
        assertThat(result).contains(searchIndexId, "VECTOR");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/search-index/idx-123");
    }

    @Test
    void testGetSearchIndex_notFound() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(422, "Unprocessable entity", "/ssot/search-index/nonexistent"));

        // When
        String result = searchIndexTools.getSearchIndex("nonexistent");

        // Then
        assertThat(result).contains("error", "422");
    }

    // ============================================================
    // Delete Search Index
    // ============================================================

    @Test
    void testDeleteSearchIndex_success() {
        // Given
        String searchIndexId = "idx-123";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = searchIndexTools.deleteSearchIndex(searchIndexId);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/search-index/idx-123");
    }

    @Test
    void testDeleteSearchIndex_errorHandling() {
        // Given
        when(client.delete(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(403, "User does not have permission", "/ssot/search-index/idx-123"));

        // When
        String result = searchIndexTools.deleteSearchIndex("idx-123");

        // Then
        assertThat(result).contains("error", "403");
    }

    // ============================================================
    // Update Search Index
    // ============================================================

    @Test
    void testUpdateSearchIndex_success() {
        // Given
        String searchIndexId = "idx-123";
        Map<String, Object> mockResponse = Map.of(
            "id", "testId",
            "vectorEmbedding", Map.of("vectorEmbeddingRelatedFields", List.of())
        );

        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        SearchIndexCreateRequest request = new SearchIndexCreateRequest();
        request.setLabel("Updated_search_index");
        request.setDeveloperName("Case_search_index");
        request.setSourceDmoDeveloperName("ssot__Case__dlm");
        request.setChunkDmoName("Case_search_index chunk");
        request.setChunkDmoDeveloperName("Case_search_index_chunk");
        request.setVectorDmoName("Case_search_index index");
        request.setVectorDmoDeveloperName("Case_search_index_index");
        request.setSearchType("VECTOR");
        VectorEmbeddingInput vectorEmbedding = new VectorEmbeddingInput();
        vectorEmbedding.setVectorEmbeddingRelatedFields(List.of());
        request.setVectorEmbedding(vectorEmbedding);

        ChunkingConfigInput chunkingConfig = new ChunkingConfigInput();
        chunkingConfig.setFieldLevelConfigurations(List.of());
        request.setChunkingConfiguration(chunkingConfig);

        VectorEmbeddingConfigInput vecConfig = new VectorEmbeddingConfigInput();
        vecConfig.setSimilarityMetric("COSINE");
        request.setVectorEmbeddingConfiguration(vecConfig);

        TransformConfigInput transformConfig = new TransformConfigInput();
        transformConfig.setTransformType("CHUNKING");
        request.setTransformConfigurations(List.of(transformConfig));

        String result = searchIndexTools.updateSearchIndex(searchIndexId, request);

        // Then
        assertThat(result).contains("testId");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/search-index/idx-123");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Updated_search_index");
    }

    @Test
    void testUpdateSearchIndex_errorHandling() {
        // Given
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid request", "/ssot/search-index/idx-123"));

        // When
        SearchIndexCreateRequest request = new SearchIndexCreateRequest();
        request.setLabel("Bad Update");
        request.setSearchType("INVALID");

        String result = searchIndexTools.updateSearchIndex("idx-123", request);

        // Then
        assertThat(result).contains("error", "Invalid request", "400");
    }

    @Test
    void testUpdateSearchIndex_connectionError() {
        // Given
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException("Connection refused", new RuntimeException("refused")));

        // When
        SearchIndexCreateRequest request = new SearchIndexCreateRequest();
        request.setLabel("Test");

        String result = searchIndexTools.updateSearchIndex("idx-123", request);

        // Then
        assertThat(result).contains("error", "Connection refused");
    }

    // ============================================================
    // Get Search Index Process History
    // ============================================================

    @Test
    void testGetSearchIndexProcessHistory_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "processHistories", List.of(
                Map.of("id", "ph-001", "status", "COMPLETED", "startTime", "2026-04-01T00:00:00Z")
            ),
            "totalSize", 1
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = searchIndexTools.getSearchIndexProcessHistory("idx-123", null, null);

        // Then
        assertThat(result).contains("ph-001", "COMPLETED");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/search-index/idx-123/process-history?limit=50&offset=1");
    }

    @Test
    void testGetSearchIndexProcessHistory_withPagination() {
        // Given
        Map<String, Object> mockResponse = Map.of("processHistories", List.of(), "totalSize", 0);

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = searchIndexTools.getSearchIndexProcessHistory("idx-123", 10, 20);

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/search-index/idx-123/process-history", "limit=10", "offset=20");
    }

    @Test
    void testGetSearchIndexProcessHistory_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/search-index/nonexistent/process-history"));

        // When
        String result = searchIndexTools.getSearchIndexProcessHistory("nonexistent", null, null);

        // Then
        assertThat(result).contains("error", "404");
    }

    @Test
    void testGetSearchIndexProcessHistory_connectionError() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException("Connection timeout", new RuntimeException("timeout")));

        // When
        String result = searchIndexTools.getSearchIndexProcessHistory("idx-123", null, null);

        // Then
        assertThat(result).contains("error", "Connection timeout");
    }
}
