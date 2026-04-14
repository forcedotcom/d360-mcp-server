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
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PayloadExamplesTool {
    private final ToolCallbackRegistry registry;
    private final FamilyCatalog catalog;

    public PayloadExamplesTool(@Lazy ToolCallbackRegistry registry, FamilyCatalog catalog) {
        this.registry = registry;
        this.catalog = catalog;
    }

    @McpTool(name = "payload_examples", description = "Get parameter schema and example payload for a tool. "
        + "Returns the full input schema (parameter names, types, descriptions, required flags) "
        + "and a hand-curated example if available. Use this after search to understand "
        + "the expected input shape before calling execute.")
    public Map<String, Object> invoke(
        @McpToolParam(description = "Tool name, e.g. d360_query_sql. Omit to list available examples.", required = false)
        String toolName
    ) {
        if (toolName == null || toolName.isBlank()) {
            return Map.of(
                "availableExamples", PayloadExamples.listToolsWithExamples(),
                "totalRegisteredTools", registry.size(),
                "hint", "Pass a tool name to see its schema and example."
            );
        }

        String normalized = toolName.trim();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("toolName", normalized);

        // Family info from FamilyCatalog
        FamilyCatalog.ToolInfo toolInfo = catalog.getToolInfo(normalized);
        if (toolInfo != null) {
            response.put("family", toolInfo.family());
        }

        // Schema + description from ToolCallbackRegistry
        ToolCallbackRegistry.ToolEntry entry = registry.get(normalized);
        if (entry != null) {
            response.put("description", entry.description());
            response.put("inputSchema", registry.getSchema(normalized));
        } else if (toolInfo != null) {
            response.put("description", toolInfo.description());
        }

        // Hand-curated example from PayloadExamples
        Map<String, Object> example = PayloadExamples.getPayloadExample(normalized);
        if (example != null) {
            response.put("example", example);
        }

        if (entry == null && toolInfo == null) {
            response.put("error", "Unknown tool: " + normalized + ". Use search to find available tools.");
        }

        return response;
    }
}
