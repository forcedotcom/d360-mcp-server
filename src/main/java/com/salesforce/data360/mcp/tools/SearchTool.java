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
import com.salesforce.data360.mcp.runtime.SearchStrategy;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchTool {
    private final FamilyCatalog catalog;
    private final SearchStrategy searchStrategy;

    public SearchTool(FamilyCatalog catalog, SearchStrategy searchStrategy) {
        this.catalog = catalog;
        this.searchStrategy = searchStrategy;
    }

    @McpTool(name = "search", description = "Search Data 360 tool families by intent. "
        + "Returns matching families with descriptions, available tools, and workflow hints. "
        + "Use this first to discover what tools are available for your task.")
    public Map<String, Object> invoke(
        @McpToolParam(description = "Describe what you want to do.") String query
    ) {
        List<SearchStrategy.ScoredFamily> scored = searchStrategy.search(query, catalog.getAllFamilies(), 8);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);

        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchStrategy.ScoredFamily sf : scored) {
            FamilyCatalog.FamilyEntry family = catalog.getFamily(sf.family());
            if (family == null) continue;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("family", family.family());
            entry.put("summary", family.summary());
            entry.put("tools", family.tools().stream().map(FamilyCatalog.ToolInfo::name).toList());
            results.add(entry);
        }
        response.put("results", results);
        return response;
    }
}
