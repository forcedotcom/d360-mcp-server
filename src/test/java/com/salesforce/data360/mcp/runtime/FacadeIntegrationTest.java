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
package com.salesforce.data360.mcp.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.data360.mcp.tools.ExecuteTool;
import com.salesforce.data360.mcp.tools.PayloadExamplesTool;
import com.salesforce.data360.mcp.tools.SearchTool;
import com.salesforce.data360.mcp.tools.datastream.DataStreamTools;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FacadeIntegrationTest {

    @Mock
    private ApplicationContext context;

    private ObjectMapper objectMapper;
    private Validator validator;
    private ToolCallbackRegistry registry;
    private FamilyCatalog catalog;
    private SearchStrategy searchStrategy;

    private SearchTool searchTool;
    private PayloadExamplesTool payloadExamplesTool;
    private ExecuteTool executeTool;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        // Create test beans with @McpTool methods
        TestQueryBean queryBean = new TestQueryBean();
        TestDmoBean dmoBean = new TestDmoBean();

        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"queryBean", "dmoBean"});
        when(context.getType("queryBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getType("dmoBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("queryBean")).thenReturn(queryBean);
        when(context.getBean("dmoBean")).thenReturn(dmoBean);

        // Create registry from mock context
        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        // Create family catalog
        catalog = new FamilyCatalog();

        // Create keyword search strategy
        searchStrategy = new KeywordSearchStrategy();

        // Wire up facade tools
        searchTool = new SearchTool(catalog, searchStrategy);
        payloadExamplesTool = new PayloadExamplesTool(registry, catalog);
        executeTool = new ExecuteTool(registry);
    }

    @Test
    void facadeExposes_onlyThreeTools() {
        // Given - tools are created in setUp

        // Then
        assertThat(searchTool).isNotNull();
        assertThat(payloadExamplesTool).isNotNull();
        assertThat(executeTool).isNotNull();

        // Verify registry has our test tools
        assertThat(registry.get("test_query_sql")).isNotNull();
        assertThat(registry.get("test_dmo_list")).isNotNull();
        assertThat(registry.size()).isEqualTo(2);
    }

    @Test
    void searchThenExecute_workflow() {
        // Step 1: Search for query-related tools
        Map<String, Object> searchResults = searchTool.invoke("query SQL database");

        // Verify search returns results
        assertThat(searchResults).containsKey("results");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) searchResults.get("results");
        assertThat(results).isNotEmpty();

        // Find a family that has query tools
        Map<String, Object> queryFamily = results.stream()
            .filter(r -> "Query".equals(r.get("family")))
            .findFirst()
            .orElse(null);

        assertThat(queryFamily).isNotNull();
        assertThat(queryFamily.get("summary")).isNotNull();

        // Step 2: Get payload examples for a test tool
        Map<String, Object> payloadInfo = payloadExamplesTool.invoke("test_query_sql");

        // Verify payload info contains schema
        assertThat(payloadInfo).containsKey("toolName");
        assertThat(payloadInfo).containsKey("inputSchema");
        assertThat(payloadInfo.get("toolName")).isEqualTo("test_query_sql");

        @SuppressWarnings("unchecked")
        Map<String, Object> inputSchema = (Map<String, Object>) payloadInfo.get("inputSchema");
        assertThat(inputSchema).containsKey("properties");

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) inputSchema.get("properties");
        assertThat(properties).containsKey("sql");
        assertThat(properties).containsKey("dataspace");

        // Step 3: Execute the test tool
        String executeResult = executeTool.invoke("test_query_sql", "{\"sql\":\"SELECT * FROM test\",\"dataspace\":\"default\"}");

        // Verify execution result
        assertThat(executeResult).contains("queryId");
        assertThat(executeResult).contains("test-query-123");
    }

    @Test
    void payloadExamples_listAvailableTools() {
        // When - invoke without toolName
        Map<String, Object> result = payloadExamplesTool.invoke(null);

        // Then
        assertThat(result).containsKey("totalRegisteredTools");
        assertThat(result.get("totalRegisteredTools")).isEqualTo(2);
        assertThat(result).containsKey("hint");
    }

    @Test
    void execute_unknownTool_returnsError() {
        // When
        String result = executeTool.invoke("unknown_tool", "{}");

        // Then
        assertThat(result).contains("error");
        assertThat(result).contains("Unknown tool");
    }

    @Test
    void execute_selfReference_returnsError() {
        // When
        String result = executeTool.invoke("execute", "{}");

        // Then
        assertThat(result).contains("error");
        assertThat(result).contains("not available via execute");
    }

    @Test
    void search_findsMultipleFamilies() {
        // When - search for data model objects
        Map<String, Object> results = searchTool.invoke("data model objects schema");

        // Then
        assertThat(results).containsKey("query");
        assertThat(results).containsKey("results");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> families = (List<Map<String, Object>>) results.get("results");

        // Should find DMO family
        boolean foundDmo = families.stream()
            .anyMatch(f -> "DMO".equals(f.get("family")));
        assertThat(foundDmo).isTrue();
    }

    @Test
    void payloadExamples_unknownTool_returnsError() {
        // When
        Map<String, Object> result = payloadExamplesTool.invoke("unknown_tool");

        // Then
        assertThat(result).containsKey("error");
        assertThat(result.get("error")).asString().contains("Unknown tool");
    }

    // ── Test Helper Beans ──────────────────────────────────────────────────

    /**
     * Test bean simulating a query tool.
     */
    public static class TestQueryBean {
        @McpTool(name = "test_query_sql", description = "Execute SQL query for testing")
        public String querySql(
            @McpToolParam(description = "SQL query to execute") String sql,
            @McpToolParam(description = "Dataspace name", required = false) String dataspace
        ) {
            return "{\"queryId\":\"test-query-123\",\"completionStatus\":\"COMPLETED\"}";
        }
    }

    /**
     * Test bean simulating a DMO tool.
     */
    public static class TestDmoBean {
        @McpTool(name = "test_dmo_list", description = "List data model objects for testing")
        public String listDmos(
            @McpToolParam(description = "Category filter", required = false) String category
        ) {
            return "{\"dmos\":[{\"name\":\"Individual__dlm\"},{\"name\":\"Account__dlm\"}]}";
        }
    }
}
