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
import com.salesforce.data360.mcp.client.ClientContext;
import com.salesforce.data360.mcp.runtime.FamilyCatalog;
import com.salesforce.data360.mcp.runtime.HybridSearchStrategy;
import com.salesforce.data360.mcp.runtime.KeywordSearchStrategy;
import com.salesforce.data360.mcp.runtime.SearchStrategy;
import com.salesforce.data360.mcp.runtime.ToolCallbackRegistry;
import com.salesforce.data360.mcp.runtime.VectorSearchStrategy;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Data360RuntimeConfiguration {

    public static final List<String> FACADE_TOOL_NAMES = List.of("search", "payload_examples", "execute");

    @Bean
    FamilyCatalog familyCatalog() {
        return new FamilyCatalog();
    }

    @Bean
    SearchStrategy searchStrategy(AppProperties properties,
                                   FamilyCatalog catalog,
                                   @Autowired(required = false) EmbeddingModel embeddingModel) {
        KeywordSearchStrategy keyword = new KeywordSearchStrategy();
        return switch (properties.getSearchStrategy()) {
            case "vector" -> {
                if (embeddingModel == null)
                    throw new IllegalStateException(
                        "search-strategy=vector requires OPENAI_API_KEY and SPRING_AI_MODEL_EMBEDDING=openai");
                yield new VectorSearchStrategy(embeddingModel, catalog.getAllFamilies());
            }
            case "hybrid" -> {
                if (embeddingModel == null)
                    throw new IllegalStateException(
                        "search-strategy=hybrid requires OPENAI_API_KEY and SPRING_AI_MODEL_EMBEDDING=openai");
                yield new HybridSearchStrategy(keyword,
                    new VectorSearchStrategy(embeddingModel, catalog.getAllFamilies()));
            }
            default -> keyword;
        };
    }

    @Bean
    List<McpServerFeatures.SyncToolSpecification> syncTools(@Lazy ToolCallbackRegistry registry,
                                                              ObjectMapper objectMapper) {
        List<McpServerFeatures.SyncToolSpecification> specs = new ArrayList<>(FACADE_TOOL_NAMES.size());

        for (String toolName : FACADE_TOOL_NAMES) {
            ToolCallbackRegistry.ToolEntry entry = registry.get(toolName);
            if (entry == null) {
                throw new IllegalStateException("Required MCP tool not found: " + toolName);
            }

            McpSchema.Tool tool = new McpSchema.Tool(
                entry.name(), null, entry.description(),
                registry.getJsonSchema(toolName), null, null, null);

            specs.add(new McpServerFeatures.SyncToolSpecification(tool,
                (exchange, request) -> {
                    if (exchange != null) {
                        McpSchema.Implementation clientInfo = exchange.getClientInfo();
                        if (clientInfo != null) {
                            ClientContext.set(clientInfo.name() + "/" + clientInfo.version());
                        }
                    }
                    try {
                        Object result = registry.invokeForResult(toolName, request.arguments());
                        String json = result == null ? "{}"
                            : (result instanceof String s ? s : objectMapper.writeValueAsString(result));
                        return new McpSchema.CallToolResult(
                            List.of(new McpSchema.TextContent(json)), false, null, null);
                    } catch (Exception e) {
                        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        return new McpSchema.CallToolResult(
                            List.of(new McpSchema.TextContent("Error: " + msg)), true, null, null);
                    } finally {
                        ClientContext.clear();
                    }
                }));
        }
        return specs;
    }
}
