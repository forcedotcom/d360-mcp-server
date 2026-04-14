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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.salesforce.data360.mcp.runtime.ToolCallbackRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExecuteToolTest {

    @Mock
    private ToolCallbackRegistry registry;

    private ExecuteTool executeTool;

    @BeforeEach
    void setUp() {
        executeTool = new ExecuteTool(registry);
    }

    @Test
    void execute_validTool_returnsResult() throws Exception {
        // Given
        String toolName = "d360_query_sql";
        String paramsJson = "{\"sql\":\"SELECT * FROM Individual__dlm\"}";
        String expectedResult = "{\"queryId\":\"query-123\",\"status\":\"COMPLETED\"}";

        when(registry.invoke(toolName, paramsJson)).thenReturn(expectedResult);

        // When
        String result = executeTool.invoke(toolName, paramsJson);

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void execute_unknownTool_returnsError() throws Exception {
        // Given
        String toolName = "unknown_tool";
        String paramsJson = "{}";

        when(registry.invoke(toolName, paramsJson))
            .thenThrow(new IllegalArgumentException("Unknown tool: unknown_tool"));

        // When
        String result = executeTool.invoke(toolName, paramsJson);

        // Then
        assertThat(result).contains("\"error\"");
        assertThat(result).contains("Unknown tool: unknown_tool");
        assertThat(result).contains("\"hint\"");
        assertThat(result).contains("Use search to find available tools");
    }

    @Test
    void execute_selfCall_returnsError() {
        // Given
        String toolName = "execute";

        // When
        String result = executeTool.invoke(toolName, null);

        // Then
        assertThat(result).contains("\"error\"");
        assertThat(result).contains("not available via execute");
        assertThat(result).contains("\"hint\"");
        assertThat(result).contains("Use search and payload_examples instead");
    }

    @Test
    void execute_exceptionDuringInvoke_returnsError() throws Exception {
        // Given
        String toolName = "d360_query_sql";
        String paramsJson = "{\"sql\":\"INVALID SQL\"}";

        when(registry.invoke(toolName, paramsJson))
            .thenThrow(new RuntimeException("Query execution failed"));

        // When
        String result = executeTool.invoke(toolName, paramsJson);

        // Then
        assertThat(result).contains("\"error\"");
        assertThat(result).contains("Query execution failed");
    }

    @Test
    void execute_withHint_onDeserializationError() throws Exception {
        // Given
        String toolName = "d360_segment_create";
        String paramsJson = "{\"invalidField\":\"value\"}";

        ToolCallbackRegistry.ToolEntry entry = new ToolCallbackRegistry.ToolEntry(
            new Object(),
            null,
            toolName,
            "Create segment",
            List.of(
                new ToolCallbackRegistry.ParamEntry("displayName", "Segment name", String.class, true),
                new ToolCallbackRegistry.ParamEntry("segmentType", "Type", String.class, true)
            )
        );

        when(registry.get(toolName)).thenReturn(entry);
        when(registry.invoke(toolName, paramsJson))
            .thenThrow(new RuntimeException(
                new JsonMappingException(null, "Cannot deserialize value of type `String`")));

        // When
        String result = executeTool.invoke(toolName, paramsJson);

        // Then
        assertThat(result).contains("\"error\"");
        assertThat(result).contains("Cannot deserialize");
        assertThat(result).contains("\"hint\"");
        assertThat(result).contains("Valid parameters for 'd360_segment_create'");
        assertThat(result).contains("displayName (String, required)");
        assertThat(result).contains("segmentType (String, required)");
        assertThat(result).contains("Use payload_examples('d360_segment_create')");
    }

    @Test
    void execute_jsonEscaping_worksCorrectly() throws Exception {
        // Given
        String toolName = "d360_query_sql";
        String paramsJson = "{}";

        when(registry.invoke(toolName, paramsJson))
            .thenThrow(new RuntimeException("Error with \"quotes\" and \\ backslash and \n newline"));

        // When
        String result = executeTool.invoke(toolName, paramsJson);

        // Then
        assertThat(result).contains("\\\"quotes\\\"");
        assertThat(result).contains("\\\\");
        assertThat(result).contains("\\n");
    }
}
