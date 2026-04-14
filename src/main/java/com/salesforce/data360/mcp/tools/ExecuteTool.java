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

import com.salesforce.data360.mcp.runtime.ToolCallbackRegistry;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ExecuteTool {

    static final Set<String> BLOCKED_TOOLS = Set.of(
        "execute"
    );

    private final ToolCallbackRegistry registry;

    public ExecuteTool(@Lazy ToolCallbackRegistry registry) {
        this.registry = registry;
    }

    @McpTool(name = "execute", description = "Execute a Data 360 tool by name. "
        + "Use search to find the tool, payload_examples to see expected parameters, "
        + "then pass the tool name and parameters here.")
    public String invoke(
        @McpToolParam(description = "Tool name, e.g. d360_query_sql")
        String toolName,
        @McpToolParam(description = "Parameters as a JSON string matching the tool's schema. "
            + "Use payload_examples to see the expected shape.", required = false)
        String paramsJson
    ) {
        try {
            if (BLOCKED_TOOLS.contains(toolName)) {
                return errorJson(
                    "Tool '" + toolName + "' is not available via execute.",
                    "Use search and payload_examples instead.");
            }
            return registry.invoke(toolName, paramsJson);
        } catch (IllegalArgumentException e) {
            String hint = "Use search to find available tools.";
            ToolCallbackRegistry.ToolEntry entry = registry.get(toolName);
            if (entry != null) {
                hint = parameterHint(toolName, entry);
            }
            return errorJson(e.getMessage(), hint);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e.getCause() != null) msg = e.getCause().getMessage();
            String hint = "";
            ToolCallbackRegistry.ToolEntry entry = registry.get(toolName);
            if (entry != null && msg != null && (msg.contains("Unrecognized field") || msg.contains("Cannot deserialize"))) {
                hint = parameterHint(toolName, entry);
            }
            return errorJson(msg, hint);
        }
    }

    private static String parameterHint(String toolName, ToolCallbackRegistry.ToolEntry entry) {
        return "Valid parameters for '" + toolName + "': " +
            entry.params().stream()
                .map(p -> p.name() + " (" + p.type().getSimpleName() +
                     (p.required() ? ", required" : ", optional") + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("none") +
            ". Use payload_examples('" + toolName + "') to see the expected shape.";
    }

    private static String errorJson(String message, String hint) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("error", message != null ? message : "null");
        if (hint != null && !hint.isEmpty()) result.put("hint", hint);
        return JsonUtil.toJson(result);
    }
}
