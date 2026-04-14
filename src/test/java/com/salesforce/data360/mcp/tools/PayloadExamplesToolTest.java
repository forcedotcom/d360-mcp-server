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

import com.salesforce.data360.mcp.runtime.FamilyCatalog;
import com.salesforce.data360.mcp.runtime.ToolCallbackRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayloadExamplesToolTest {

    private static final int STUB_REGISTRY_SIZE = 7;

    @Mock
    private ToolCallbackRegistry registry;

    @Mock
    private FamilyCatalog catalog;

    private PayloadExamplesTool payloadExamplesTool;

    @BeforeEach
    void setUp() {
        payloadExamplesTool = new PayloadExamplesTool(registry, catalog);
    }

    @Test
    void invoke_nullToolName_returnsAvailableExamples() {
        // Given
        when(registry.size()).thenReturn(STUB_REGISTRY_SIZE);

        // When
        Map<String, Object> result = payloadExamplesTool.invoke(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKey("availableExamples");
        assertThat(result.get("totalRegisteredTools")).isEqualTo(STUB_REGISTRY_SIZE);
        assertThat(result.get("hint")).isEqualTo("Pass a tool name to see its schema and example.");
    }

    @Test
    void invoke_blankToolName_returnsAvailableExamples() {
        // Given
        when(registry.size()).thenReturn(STUB_REGISTRY_SIZE);

        // When
        Map<String, Object> result = payloadExamplesTool.invoke("  ");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKey("availableExamples");
        assertThat(result.get("totalRegisteredTools")).isEqualTo(STUB_REGISTRY_SIZE);
        assertThat(result.get("hint")).isEqualTo("Pass a tool name to see its schema and example.");
    }

    @Test
    void invoke_knownTool_returnsSchemaAndExample() {
        // Given
        String toolName = "d360_query_sql";

        FamilyCatalog.ToolInfo toolInfo = new FamilyCatalog.ToolInfo(
            toolName,
            "Query",
            "Execute SQL query against Data 360",
            "Use dataspace parameter for multi-tenant environments"
        );

        ToolCallbackRegistry.ToolEntry toolEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            toolName,
            "Execute SQL query",
            List.of(
                new ToolCallbackRegistry.ParamEntry("sql", "SQL query", String.class, true),
                new ToolCallbackRegistry.ParamEntry("dataspace", "Dataspace name", String.class, false)
            )
        );

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("sql", Map.of("type", "string", "description", "SQL query"));
        properties.put("dataspace", Map.of("type", "string", "description", "Dataspace name"));
        schema.put("properties", properties);
        schema.put("required", List.of("sql"));

        when(catalog.getToolInfo(toolName)).thenReturn(toolInfo);
        when(registry.get(toolName)).thenReturn(toolEntry);
        when(registry.getSchema(toolName)).thenReturn(schema);

        // When
        Map<String, Object> result = payloadExamplesTool.invoke(toolName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("toolName")).isEqualTo(toolName);
        assertThat(result.get("family")).isEqualTo("Query");
        assertThat(result.get("description")).isEqualTo("Execute SQL query");
        assertThat(result.get("inputSchema")).isEqualTo(schema);
        assertThat(result).doesNotContainKey("error");
    }

    @Test
    void invoke_unknownTool_returnsError() {
        // Given
        String toolName = "unknown_tool";

        when(catalog.getToolInfo(toolName)).thenReturn(null);
        when(registry.get(toolName)).thenReturn(null);

        // When
        Map<String, Object> result = payloadExamplesTool.invoke(toolName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("toolName")).isEqualTo(toolName);
        assertThat(result.get("error")).isEqualTo("Unknown tool: unknown_tool. Use search to find available tools.");
    }

    @Test
    void invoke_toolInCatalogButNotRegistry_usesDescriptionFromCatalog() {
        // Given
        String toolName = "d360_some_tool";

        FamilyCatalog.ToolInfo toolInfo = new FamilyCatalog.ToolInfo(
            toolName,
            "SomeFamily",
            "Description from catalog",
            null
        );

        when(catalog.getToolInfo(toolName)).thenReturn(toolInfo);
        when(registry.get(toolName)).thenReturn(null);

        // When
        Map<String, Object> result = payloadExamplesTool.invoke(toolName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("toolName")).isEqualTo(toolName);
        assertThat(result.get("family")).isEqualTo("SomeFamily");
        assertThat(result.get("description")).isEqualTo("Description from catalog");
        assertThat(result).doesNotContainKey("inputSchema");
        assertThat(result).doesNotContainKey("error");
    }

    @Test
    void invoke_toolInRegistryButNotCatalog_noFamilyInfo() {
        // Given
        String toolName = "d360_some_tool";

        ToolCallbackRegistry.ToolEntry toolEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            toolName,
            "Description from registry",
            List.of()
        );

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of());

        when(catalog.getToolInfo(toolName)).thenReturn(null);
        when(registry.get(toolName)).thenReturn(toolEntry);
        when(registry.getSchema(toolName)).thenReturn(schema);

        // When
        Map<String, Object> result = payloadExamplesTool.invoke(toolName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("toolName")).isEqualTo(toolName);
        assertThat(result.get("description")).isEqualTo("Description from registry");
        assertThat(result.get("inputSchema")).isEqualTo(schema);
        assertThat(result).doesNotContainKey("family");
        assertThat(result).doesNotContainKey("error");
    }

    @Test
    void invoke_trimmedToolName() {
        // Given
        String toolName = "  d360_query_sql  ";

        FamilyCatalog.ToolInfo toolInfo = new FamilyCatalog.ToolInfo(
            "d360_query_sql",
            "Query",
            "Execute SQL query",
            null
        );

        ToolCallbackRegistry.ToolEntry toolEntry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            "d360_query_sql",
            "Execute SQL query",
            List.of()
        );

        when(catalog.getToolInfo("d360_query_sql")).thenReturn(toolInfo);
        when(registry.get("d360_query_sql")).thenReturn(toolEntry);
        when(registry.getSchema("d360_query_sql")).thenReturn(Map.of("type", "object"));

        // When
        Map<String, Object> result = payloadExamplesTool.invoke(toolName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("toolName")).isEqualTo("d360_query_sql");
        assertThat(result).doesNotContainKey("error");
    }
}
