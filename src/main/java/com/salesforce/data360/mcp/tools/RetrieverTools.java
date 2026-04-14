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
import com.salesforce.data360.mcp.model.request.retriever.RetrieverConfigurationCreateRequest;
import com.salesforce.data360.mcp.model.request.retriever.RetrieverConfigurationUpdateRequest;
import com.salesforce.data360.mcp.model.request.retriever.RetrieverCreateRequest;
import com.salesforce.data360.mcp.model.request.retriever.RetrieverUpdateRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data 360 Retriever Tools - RAG retriever lifecycle management.
 * Maps to /ssot/machine-learning/retrievers endpoints (CRUD via Data360Client).
 */
@Component
public class RetrieverTools {

    private static final String BASE_PATH = "/machine-learning/retrievers";

    private final Data360Client client;

    public RetrieverTools(Data360Client client) {
        this.client = client;
    }

    // ── Retriever CRUD ──────────────────────────────────────────────────

    @McpTool(
        name = "d360_retriever_list",
        description = "List all RAG retrievers. Supports filtering by sourceDmo, isActive, isDefault, queryType (NoCode/ProCode/Ensemble), and search keyword. "
            + "Returns a paginated collection of retrievers with their configurations. "
            + "Use this to find retriever API names (the API name is in the 'name' field of each returned retriever)."
    )
    public String listRetrievers(
        @McpToolParam(description = "Maximum number of retrievers to return", required = false) Integer limit,
        @McpToolParam(description = "Number of retrievers to skip for pagination", required = false) Integer offset,
        @McpToolParam(description = "Search keyword to filter retrievers by name", required = false) String search,
        @McpToolParam(description = "Sort by field (e.g., Name, CreatedDate, LastModifiedDate)", required = false) String sortBy,
        @McpToolParam(description = "Sort order: ASC or DESC", required = false) String sortOrder,
        @McpToolParam(description = "Filter by source DMO id or fully qualified name", required = false) String sourceDmo,
        @McpToolParam(description = "Filter by active status", required = false) Boolean isActive,
        @McpToolParam(description = "Filter by default status", required = false) Boolean isDefault,
        @McpToolParam(description = "Filter by query type: NoCode, ProCode, or Ensemble", required = false) String queryType
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            if (search != null) params.put("search", search);
            if (sortBy != null) params.put("sortBy", sortBy);
            if (sortOrder != null) params.put("sortOrder", sortOrder);
            if (sourceDmo != null) params.put("sourceDmo", sourceDmo);
            if (isActive != null) params.put("isActive", isActive);
            if (isDefault != null) params.put("isDefault", isDefault);
            if (queryType != null) params.put("queryType", queryType);
            String path = ToolUtils.buildPath(BASE_PATH, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_get",
        description = "Get a specific RAG retriever by ID or API name. Returns the retriever with its latest and active configurations, "
            + "including queryType, data source type, output fields, filters, and number of results. "
            + "Use this to inspect a retriever's setup before querying it."
    )
    public String getRetriever(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName
    ) {
        try {
            String path = BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_create",
        description = "Create a new RAG retriever. "
            + "IMPORTANT: Before calling this tool, call d360_search_index_list to find available search indexes "
            + "that can be used as input sources for the retriever configuration. "
            + "Required fields: label, configuration. The configuration MUST include: queryType (NoCode/ProCode/Ensemble), "
            + "input ({id: '<search_index_id>'}), outputFields (array of {relatedDmoName, relatedDmoFieldName, label, relationships: []}), "
            + "numberOfResults (top-K), isActive (boolean). "
            + "Optional: description, dataSourceType (SearchIndex, Web), dataSpaces. "
            + "After creating a retriever, use d360_query_sql with hybrid_search() SQL to test it with sample queries."
    )
    public String createRetriever(RetrieverCreateRequest request) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map result = client.post(BASE_PATH, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_update",
        description = "Update an existing RAG retriever's label or description."
    )
    public String updateRetriever(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName,
        RetrieverUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_delete",
        description = "Delete a RAG retriever and all its configurations."
    )
    public String deleteRetriever(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName
    ) {
        try {
            String path = BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName);
            client.delete(path);
            return JsonUtil.toJson(Map.of("status", "deleted", "retriever", retrieverIdOrName));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ── Retriever Configuration CRUD ────────────────────────────────────

    @McpTool(
        name = "d360_retriever_config_list",
        description = "List all configurations (versions) for a specific retriever."
    )
    public String listRetrieverConfigurations(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName,
        @McpToolParam(description = "Maximum number of configurations to return", required = false) Integer limit,
        @McpToolParam(description = "Number of configurations to skip for pagination", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            String path = ToolUtils.buildPath(
                BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName) + "/configurations",
                params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_config_get",
        description = "Get a specific configuration (version) for a retriever. Returns detailed configuration including queryType, output fields, "
            + "filters, number of results, citation settings, and referenced retrievers (for Ensemble type). "
            + "The active configuration is what the retriever uses at runtime."
    )
    public String getRetrieverConfiguration(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName,
        @McpToolParam(description = "The configuration ID or name") String configurationIdOrName
    ) {
        try {
            String path = BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName)
                + "/configurations/" + ToolUtils.encodePath(configurationIdOrName);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_config_create",
        description = "Create a new configuration (version) for a retriever. "
            + "Required: queryType (NoCode, ProCode, or Ensemble). "
            + "For NoCode: provide input (source search index reference), outputFields, numberOfResults, isActive. "
            + "For Ensemble: provide referencedRetrievers (list of child retriever references). "
            + "For ProCode: provide input, outputFields, retrieverQueryTemplate. "
            + "The numberOfResults setting determines the default top-K when querying the retriever. "
            + "The queryFilter (NoCode) or queryFilterFields/resultFilterFields (ProCode) control which documents are eligible for retrieval."
    )
    public String createRetrieverConfiguration(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName,
        RetrieverConfigurationCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName) + "/configurations";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_config_update",
        description = "Update a retriever configuration. Currently supports setting the configuration as active or inactive. "
            + "At most one configuration can be active within a retriever."
    )
    public String updateRetrieverConfiguration(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName,
        @McpToolParam(description = "The configuration ID or name") String configurationIdOrName,
        RetrieverConfigurationUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName)
                + "/configurations/" + ToolUtils.encodePath(configurationIdOrName);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_retriever_config_delete",
        description = "Delete a specific configuration (version) from a retriever."
    )
    public String deleteRetrieverConfiguration(
        @McpToolParam(description = "The retriever ID or name") String retrieverIdOrName,
        @McpToolParam(description = "The configuration ID or name") String configurationIdOrName
    ) {
        try {
            String path = BASE_PATH + "/" + ToolUtils.encodePath(retrieverIdOrName)
                + "/configurations/" + ToolUtils.encodePath(configurationIdOrName);
            client.delete(path);
            return JsonUtil.toJson(Map.of("status", "deleted", "retriever", retrieverIdOrName, "configuration", configurationIdOrName));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
