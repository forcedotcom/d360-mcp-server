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
import com.salesforce.data360.mcp.model.request.searchindex.SearchIndexCreateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data 360 Search Index Tools - semantic search index management.
 * Maps to /ssot/search-index endpoints.
 */
@Component
public class SearchIndexTools {

    private final Data360Client client;

    public SearchIndexTools(Data360Client client) {
        this.client = client;
    }

    /**
     * Get all search index definition details.
     */
    @ApiEndpoint(path = "/ssot/search-index", verb = "GET")
    @McpTool(
        name = "d360_search_index_list",
        description = "Get all search index definition details."
    )
    public String listSearchIndexes() {
        try {
            String path = "/ssot/search-index";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a search index (semantic search) for unstructured data.
     */
    @ApiEndpoint(path = "/ssot/search-index", verb = "POST")
    @McpTool(
        name = "d360_search_index_create",
        description = "Create a search index (semantic search) for unstructured data. "
            + "IMPORTANT: Before calling this tool, first call d360_search_index_config to retrieve the valid configuration options "
            + "including supported chunking strategies, embedding models, index types, similarity metrics, transcription models, "
            + "image processing models, parsing stages, and per-file-extension defaults. Use those config values to populate the request correctly. "
            + "Required fields: label, developerName, sourceDmoDeveloperName, chunkDmoName, chunkDmoDeveloperName, "
            + "vectorDmoName, vectorDmoDeveloperName, vectorEmbedding, chunkingConfiguration, vectorEmbeddingConfiguration, "
            + "searchType (HYBRID or VECTOR), and transformConfigurations."
    )
    public String createSearchIndex(SearchIndexCreateRequest request) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/search-index";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get search index configuration.
     */
    @ApiEndpoint(path = "/ssot/search-index/config", verb = "GET")
    @McpTool(
        name = "d360_search_index_config",
        description = "Get the search index configuration."
    )
    public String getSearchIndexConfig() {
        try {
            String path = "/ssot/search-index/config";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific search index definition by ID or API name.
     */
    @ApiEndpoint(path = "/ssot/search-index/{id}", verb = "GET")
    @McpTool(
        name = "d360_search_index_get",
        description = "Get information about a specific search index definition record."
    )
    public String getSearchIndex(
        @McpToolParam(description = "ID or API Name of the search index") String searchIndexApiNameOrId
    ) {
        try {
            String path = "/ssot/search-index/" + ToolUtils.encodePath(searchIndexApiNameOrId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a search index definition.
     */
    @ApiEndpoint(path = "/ssot/search-index/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_search_index_delete",
        description = "Delete a search index definition."
    )
    public String deleteSearchIndex(
        @McpToolParam(description = "ID or API Name of the search index") String searchIndexApiNameOrId
    ) {
        try {
            String path = "/ssot/search-index/" + ToolUtils.encodePath(searchIndexApiNameOrId);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get process history for a search index.
     */
    @ApiEndpoint(path = "/ssot/search-index/{id}/process-history", verb = "GET")
    @McpTool(
        name = "d360_search_index_process_history",
        description = "Get the process run history for a specific search index. Returns a list of process history records with status, timestamps, and details. "
            + "NOTE: The offset parameter is 1-based (first page starts at offset=1). "
            + "Pagination is currently unreliable, so only the first page of results is fetched."
    )
    public String getSearchIndexProcessHistory(
        @McpToolParam(description = "ID or API Name of the search index") String searchIndexApiNameOrId,
        @McpToolParam(description = "Maximum number of records to return (default 50)", required = false) Integer limit,
        @McpToolParam(description = "1-based offset for pagination (default 1). Must be >= 1.", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("limit", limit != null ? limit : 50);
            params.put("offset", (offset != null && offset >= 1) ? offset : 1);
            String path = ToolUtils.buildPath(
                "/ssot/search-index/" + ToolUtils.encodePath(searchIndexApiNameOrId) + "/process-history",
                params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing search index (semantic search) record.
     */
    @ApiEndpoint(path = "/ssot/search-index/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_search_index_update",
        description = "Update an existing search index (semantic search) record. "
            + "IMPORTANT: Before calling this tool, first call d360_search_index_config to retrieve valid configuration options "
            + "and d360_search_index_get to retrieve the current index definition. Use those values to populate the request correctly."
    )
    public String updateSearchIndex(
        @McpToolParam(description = "ID or API Name of the search index") String searchIndexApiNameOrId,
        SearchIndexCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/search-index/" + ToolUtils.encodePath(searchIndexApiNameOrId);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
