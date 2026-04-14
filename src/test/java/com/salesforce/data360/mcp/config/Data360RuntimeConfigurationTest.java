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
package com.salesforce.data360.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.data360.mcp.runtime.FamilyCatalog;
import com.salesforce.data360.mcp.runtime.HybridSearchStrategy;
import com.salesforce.data360.mcp.runtime.KeywordSearchStrategy;
import com.salesforce.data360.mcp.runtime.SearchStrategy;
import com.salesforce.data360.mcp.runtime.ToolCallbackRegistry;
import com.salesforce.data360.mcp.runtime.VectorSearchStrategy;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Data360RuntimeConfigurationTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private ToolCallbackRegistry registry;

    private final Data360RuntimeConfiguration config = new Data360RuntimeConfiguration();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void searchStrategy_keyword_default() {
        // Given
        AppProperties properties = new AppProperties();
        properties.setSearchStrategy("keyword");
        FamilyCatalog catalog = new FamilyCatalog();

        // When
        SearchStrategy strategy = config.searchStrategy(properties, catalog, null);

        // Then
        assertThat(strategy).isInstanceOf(KeywordSearchStrategy.class);
    }

    @Test
    void searchStrategy_keyword_whenNoStrategySpecified() {
        // Given - default search strategy
        AppProperties properties = new AppProperties();
        FamilyCatalog catalog = new FamilyCatalog();

        // When
        SearchStrategy strategy = config.searchStrategy(properties, catalog, null);

        // Then
        assertThat(strategy).isInstanceOf(KeywordSearchStrategy.class);
    }

    @Test
    void searchStrategy_vector_requiresEmbeddingModel() {
        // Given
        AppProperties properties = new AppProperties();
        properties.setSearchStrategy("vector");
        FamilyCatalog catalog = new FamilyCatalog();

        // When/Then
        assertThatThrownBy(() -> config.searchStrategy(properties, catalog, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("search-strategy=vector requires OPENAI_API_KEY and SPRING_AI_MODEL_EMBEDDING=openai");
    }

    @Test
    void searchStrategy_vector_success() {
        // Given
        AppProperties properties = new AppProperties();
        properties.setSearchStrategy("vector");
        FamilyCatalog catalog = new FamilyCatalog();

        // Mock embedding responses with dummy vectors
        float[] dummyVector = new float[]{0.1f, 0.2f, 0.3f};
        when(embeddingModel.call(any(EmbeddingRequest.class)))
            .thenAnswer(invocation -> {
                EmbeddingRequest request = invocation.getArgument(0);
                List<Embedding> embeddings = request.getInstructions().stream()
                    .map(text -> new Embedding(dummyVector, 0))
                    .toList();
                return new EmbeddingResponse(embeddings);
            });

        // When
        SearchStrategy strategy = config.searchStrategy(properties, catalog, embeddingModel);

        // Then
        assertThat(strategy).isInstanceOf(VectorSearchStrategy.class);
    }

    @Test
    void searchStrategy_hybrid_requiresEmbeddingModel() {
        // Given
        AppProperties properties = new AppProperties();
        properties.setSearchStrategy("hybrid");
        FamilyCatalog catalog = new FamilyCatalog();

        // When/Then
        assertThatThrownBy(() -> config.searchStrategy(properties, catalog, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("search-strategy=hybrid requires OPENAI_API_KEY and SPRING_AI_MODEL_EMBEDDING=openai");
    }

    @Test
    void searchStrategy_hybrid_success() {
        // Given
        AppProperties properties = new AppProperties();
        properties.setSearchStrategy("hybrid");
        FamilyCatalog catalog = new FamilyCatalog();

        // Mock embedding responses with dummy vectors
        float[] dummyVector = new float[]{0.1f, 0.2f, 0.3f};
        when(embeddingModel.call(any(EmbeddingRequest.class)))
            .thenAnswer(invocation -> {
                EmbeddingRequest request = invocation.getArgument(0);
                List<Embedding> embeddings = request.getInstructions().stream()
                    .map(text -> new Embedding(dummyVector, 0))
                    .toList();
                return new EmbeddingResponse(embeddings);
            });

        // When
        SearchStrategy strategy = config.searchStrategy(properties, catalog, embeddingModel);

        // Then
        assertThat(strategy).isInstanceOf(HybridSearchStrategy.class);
    }

    @Test
    void searchStrategy_unknown_fallsBackToKeyword() {
        // Given
        AppProperties properties = new AppProperties();
        properties.setSearchStrategy("unknown");
        FamilyCatalog catalog = new FamilyCatalog();

        // When
        SearchStrategy strategy = config.searchStrategy(properties, catalog, null);

        // Then
        assertThat(strategy).isInstanceOf(KeywordSearchStrategy.class);
    }

    @Test
    void familyCatalog_createsSuccessfully() {
        // When
        FamilyCatalog catalog = config.familyCatalog();

        // Then
        assertThat(catalog).isNotNull();
        assertThat(catalog.getAllFamilies()).isNotEmpty();
        assertThat(catalog.getAllToolNames()).isNotEmpty();
    }

    @Test
    void syncTools_exportsOnlyFacadeTools_withRegistrySchemas() {
        ToolCallbackRegistry.ToolEntry searchEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "search",
            "Search facade",
            List.of(new ToolCallbackRegistry.ParamEntry("query", "Search query", String.class, true)));
        ToolCallbackRegistry.ToolEntry payloadEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "payload_examples",
            "Payload facade",
            List.of(new ToolCallbackRegistry.ParamEntry("toolName", "Tool name", String.class, false)));
        ToolCallbackRegistry.ToolEntry executeEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "execute",
            "Execute facade",
            List.of(new ToolCallbackRegistry.ParamEntry("toolName", "Tool name", String.class, true)));

        McpSchema.JsonSchema searchSchema = new McpSchema.JsonSchema(
            "object",
            Map.of("query", Map.of("type", "string")),
            List.of("query"),
            null,
            null,
            null);
        McpSchema.JsonSchema payloadSchema = new McpSchema.JsonSchema(
            "object",
            Map.of("toolName", Map.of("type", "string")),
            List.of(),
            null,
            null,
            null);
        McpSchema.JsonSchema executeSchema = new McpSchema.JsonSchema(
            "object",
            Map.of("toolName", Map.of("type", "string")),
            List.of("toolName"),
            null,
            null,
            null);

        when(registry.get("search")).thenReturn(searchEntry);
        when(registry.get("payload_examples")).thenReturn(payloadEntry);
        when(registry.get("execute")).thenReturn(executeEntry);
        when(registry.getJsonSchema("search")).thenReturn(searchSchema);
        when(registry.getJsonSchema("payload_examples")).thenReturn(payloadSchema);
        when(registry.getJsonSchema("execute")).thenReturn(executeSchema);

        List<McpServerFeatures.SyncToolSpecification> specs = config.syncTools(registry, objectMapper);

        assertThat(specs).hasSize(3);
        assertThat(specs)
            .extracting(spec -> spec.tool().name())
            .containsExactly("search", "payload_examples", "execute");
        assertThat(specs)
            .extracting(spec -> spec.tool().inputSchema())
            .containsExactly(searchSchema, payloadSchema, executeSchema);
    }

    @Test
    void syncTools_missingFacadeRegistration_failsFast() {
        ToolCallbackRegistry.ToolEntry searchEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "search",
            "Search facade",
            List.of());
        ToolCallbackRegistry.ToolEntry payloadEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "payload_examples",
            "Payload facade",
            List.of());

        when(registry.get("search")).thenReturn(searchEntry);
        when(registry.get("payload_examples")).thenReturn(payloadEntry);
        when(registry.get("execute")).thenReturn(null);
        when(registry.getJsonSchema("search")).thenReturn(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null));
        when(registry.getJsonSchema("payload_examples")).thenReturn(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null));

        assertThatThrownBy(() -> config.syncTools(registry, objectMapper))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Required MCP tool not found: execute");
    }

    @Test
    void syncTools_handlerSerializesStructuredResult() throws Exception {
        ToolCallbackRegistry.ToolEntry searchEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "search",
            "Search facade",
            List.of(new ToolCallbackRegistry.ParamEntry("query", "Search query", String.class, true)));
        ToolCallbackRegistry.ToolEntry payloadEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "payload_examples",
            "Payload facade",
            List.of());
        ToolCallbackRegistry.ToolEntry executeEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "execute",
            "Execute facade",
            List.of());
        McpSchema.JsonSchema emptySchema = new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null);

        when(registry.get("search")).thenReturn(searchEntry);
        when(registry.get("payload_examples")).thenReturn(payloadEntry);
        when(registry.get("execute")).thenReturn(executeEntry);
        when(registry.getJsonSchema("search")).thenReturn(emptySchema);
        when(registry.getJsonSchema("payload_examples")).thenReturn(emptySchema);
        when(registry.getJsonSchema("execute")).thenReturn(emptySchema);
        when(registry.invokeForResult(eq("search"), eq(Map.of("query", "segment"))))
            .thenReturn(Map.of("results", List.of("Query")));

        List<McpServerFeatures.SyncToolSpecification> specs = config.syncTools(registry, objectMapper);

        McpSchema.CallToolResult result = specs.get(0)
            .callHandler()
            .apply(null, new McpSchema.CallToolRequest("search", Map.of("query", "segment")));

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).hasSize(1);
        assertThat(((McpSchema.TextContent) result.content().get(0)).text())
            .isEqualTo("{\"results\":[\"Query\"]}");
        verify(registry).invokeForResult("search", Map.of("query", "segment"));
    }

    @Test
    void syncTools_handlerWrapsErrors() throws Exception {
        ToolCallbackRegistry.ToolEntry searchEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "search",
            "Search facade",
            List.of());
        ToolCallbackRegistry.ToolEntry payloadEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "payload_examples",
            "Payload facade",
            List.of());
        ToolCallbackRegistry.ToolEntry executeEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "execute",
            "Execute facade",
            List.of());
        McpSchema.JsonSchema emptySchema = new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null);

        when(registry.get("search")).thenReturn(searchEntry);
        when(registry.get("payload_examples")).thenReturn(payloadEntry);
        when(registry.get("execute")).thenReturn(executeEntry);
        when(registry.getJsonSchema("search")).thenReturn(emptySchema);
        when(registry.getJsonSchema("payload_examples")).thenReturn(emptySchema);
        when(registry.getJsonSchema("execute")).thenReturn(emptySchema);
        when(registry.invokeForResult(eq("search"), eq(Map.of())))
            .thenThrow(new IllegalArgumentException("bad input"));

        List<McpServerFeatures.SyncToolSpecification> specs = config.syncTools(registry, objectMapper);

        McpSchema.CallToolResult result = specs.get(0)
            .callHandler()
            .apply(null, new McpSchema.CallToolRequest("search", Map.of()));

        assertThat(result.isError()).isTrue();
        assertThat(((McpSchema.TextContent) result.content().get(0)).text())
            .isEqualTo("Error: bad input");
    }
}
