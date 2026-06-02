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
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data 360 Metadata Discovery Tools - Entity search and field definitions.
 * Maps to /connect/search/metadata/results, /ssot/metadata, and /ssot/metadata-entities endpoints.
 */
@Component
public class MetadataTools {

    private final Data360Client client;

    public MetadataTools(Data360Client client) {
        this.client = client;
    }

    /**
     * Search Data 360 metadata using natural language.
     * PREFERRED way to discover entities. The apiName in results maps to entityName in d360_metadata.
     */
    @ApiEndpoint(path = "/connect/search/metadata/results", verb = "POST")
    @McpTool(
        name = "d360_metadata_search",
        description = "PREFERRED way to discover Data 360 metadata. Search using natural language instead of listing all entities. The apiName in results maps to entityName in d360_metadata."
    )
    public String searchMetadata(
        @McpToolParam(description = "Natural language search query") String query,
        @McpToolParam(description = "Max results to return (default: 10)", required = false) Integer limit,
        @McpToolParam(description = "Offset for pagination", required = false) Integer offset,
        @McpToolParam(description = "JSON array of metadata types to filter (e.g., [\"DataModelObject\"])", required = false) String metadataTypes,
        @McpToolParam(description = "JSON array of tags to filter (e.g., [\"crm\"])", required = false) String tags
    ) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("query", query);

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("limit", limit != null ? limit : 10);
            if (offset != null) {
                pagination.put("offset", offset);
            }
            body.put("pagination", pagination);

            List<Map<String, Object>> filters = new ArrayList<>();
            if (metadataTypes != null && !metadataTypes.isEmpty()) {
                List<String> typesList = ToolUtils.parseJson(metadataTypes, List.class, "metadataTypes");
                filters.add(Map.of("field", "metadataType", "values", typesList));
            }
            if (tags != null && !tags.isEmpty()) {
                List<String> tagsList = ToolUtils.parseJson(tags, List.class, "tags");
                filters.add(Map.of("field", "tags", "values", tagsList));
            }
            if (!filters.isEmpty()) {
                body.put("filters", filters);
            }

            Map result = client.post("/connect/search/metadata/results", body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get full metadata (including fields) for a specific Data 360 entity.
     * IMPORTANT: Always provide entityName to avoid massive responses. Use d360_metadata_search first.
     */
    @ApiEndpoint(path = "/ssot/metadata", verb = "GET")
    @McpTool(
        name = "d360_metadata",
        description = "Get full metadata (including fields) for a specific Data 360 entity. IMPORTANT: Always provide entityName to avoid massive responses. "
            + "Use d360_metadata_entities first to discover entity names. Use entityCategory='DirectoryTable' for unstructured file DMOs."
    )
    public String getMetadata(
        @McpToolParam(description = "Entity name to get metadata for (REQUIRED)") String entityName,
        @McpToolParam(description = "Entity type (e.g., DataModelObject, CalculatedInsight)", required = false) String entityType,
        @McpToolParam(description = "Entity category: Profile, Engagement, Other, DirectoryTable. "
            + "Use 'DirectoryTable' for unstructured file DMOs.", required = false) String entityCategory,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("entityName", entityName);
            if (entityType != null) params.put("entityType", entityType);
            if (entityCategory != null) params.put("entityCategory", entityCategory);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/metadata", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * List paginated metadata entities by category and type.
     * PREFER d360_metadata_search over this tool for discovery.
     */
    @ApiEndpoint(path = "/ssot/metadata-entities", verb = "GET")
    @McpTool(
        name = "d360_metadata_entities",
        description = "List paginated metadata entities by category and type. PREFER d360_metadata_search over this tool. "
            + "Use entityCategory='DirectoryTable' for unstructured file DMOs."
    )
    public String getMetadataEntities(
        @McpToolParam(description = "Entity type: CalculatedInsight, DataModelObject, or DataLakeObject", required = false) String entityType,
        @McpToolParam(description = "Entity category: Profile, Engagement, Other, DirectoryTable. "
            + "Use 'DirectoryTable' for unstructured file DMOs.", required = false) String entityCategory,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Next batch ID for pagination continuation", required = false) String nextBatchId
    ) {
        try {
            if (nextBatchId != null) {
                // Paginated request using nextBatchId
                Map<String, Object> params = new HashMap<>();
                if (dataspace != null) params.put("dataspace", dataspace);

                String path = ToolUtils.buildPath("/ssot/metadata-entities/" + ToolUtils.encodePath(nextBatchId), params);
                Map result = client.get(path, Map.class);
                return JsonUtil.toJson(result);
            } else {
                // Initial request
                Map<String, Object> params = new HashMap<>();
                if (entityType != null) params.put("entityType", entityType);
                if (entityCategory != null) params.put("entityCategory", entityCategory);
                if (dataspace != null) params.put("dataspace", dataspace);

                String path = ToolUtils.buildPath("/ssot/metadata-entities", params);
                Map result = client.get(path, Map.class);
                return JsonUtil.toJson(result);
            }
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Connections CRUD
    // ============================================================

    @ApiEndpoint(path = "/ssot/connections", verb = "GET")
    @McpTool(
        name = "d360_connection_list",
        description = "List all data connections."
    )
    public String listConnections(
        @McpToolParam(description = "Connector type to filter by") String connectorType,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("connectorType", connectorType);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connections", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/{id}", verb = "GET")
    @McpTool(
        name = "d360_connection_get",
        description = "Get a specific connection."
    )
    public String getConnection(
        @McpToolParam(description = "The connection ID") String connectionId,
        @McpToolParam(description = "Connector type") String connectorType,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("connectorType", connectorType);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connections/" + ToolUtils.encodePath(connectionId), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections", verb = "POST")
    @McpTool(
        name = "d360_connection_create",
        description = "Create a new data connection. The 'connectorType' parameter is a query param (passed separately). "
            + "The body uses 'parameters' (not 'params') for connection settings, and 'credentials' for auth. "
            + "See payload_examples for Snowflake and Salesforce body formats."
    )
    public String createConnection(
        @McpToolParam(description = "Connection body as JSON string") String body,
        @McpToolParam(description = "Connector type") String connectorType,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> bodyMap = ToolUtils.parseJson(body, Map.class, "body");

            Map<String, Object> params = new HashMap<>();
            params.put("connectorType", connectorType);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connections", params);
            Map result = client.post(path, bodyMap, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_connection_update",
        description = "Update an existing connection."
    )
    public String updateConnection(
        @McpToolParam(description = "The connection ID") String connectionId,
        @McpToolParam(description = "Update body as JSON string") String body,
        @McpToolParam(description = "Connector type") String connectorType,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> bodyMap = ToolUtils.parseJson(body, Map.class, "body");

            Map<String, Object> params = new HashMap<>();
            params.put("connectorType", connectorType);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connections/" + ToolUtils.encodePath(connectionId), params);
            Map result = client.patch(path, bodyMap, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_connection_delete",
        description = "Delete a connection."
    )
    public String deleteConnection(
        @McpToolParam(description = "The connection ID") String connectionId,
        @McpToolParam(description = "Connector type") String connectorType,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("connectorType", connectorType);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connections/" + ToolUtils.encodePath(connectionId), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/actions/test", verb = "POST")
    @McpTool(
        name = "d360_connection_test",
        description = "Test a connection configuration."
    )
    public String testConnection(
        @McpToolParam(description = "Connection test body as JSON string") String body,
        @McpToolParam(description = "Connector type") String connectorType,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> bodyMap = ToolUtils.parseJson(body, Map.class, "body");

            Map<String, Object> params = new HashMap<>();
            params.put("connectorType", connectorType);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connections/actions/test", params);
            Map result = client.post(path, bodyMap, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connection-endpoints", verb = "GET")
    @McpTool(
        name = "d360_connection_endpoints",
        description = "List available connection endpoints for Data 360 integrations."
    )
    public String listConnectionEndpoints(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connection-endpoints", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Connectors
    // ============================================================

    @ApiEndpoint(path = "/ssot/connectors", verb = "GET")
    @McpTool(
        name = "d360_connector_list",
        description = "List available connector types."
    )
    public String listConnectors(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connectors", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connectors/{type}", verb = "GET")
    @McpTool(
        name = "d360_connector_metadata",
        description = "Get metadata/schema for a specific connector type."
    )
    public String getConnectorMetadata(
        @McpToolParam(description = "The connector type") String connectorType,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/connectors/" + ToolUtils.encodePath(connectorType), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Connection Schema Discovery
    // ============================================================

    @ApiEndpoint(path = "/ssot/connections/{id}/database-schemas", verb = "POST")
    @McpTool(
        name = "d360_connection_db_schemas_list",
        description = "List database schemas available in a connection (e.g., Snowflake schemas). "
            + "Pass database name and other connector-specific properties via advancedAttributes (e.g., {\"database\":\"MY_DB\"})."
    )
    public String listConnectionDbSchemas(
        @McpToolParam(description = "The connection ID") String connectionId,
        @McpToolParam(description = "JSON object of connector-specific properties (e.g., {\"database\":\"MY_DB\"})") String advancedAttributes,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> attrs = ToolUtils.parseJson(advancedAttributes, Map.class, "advancedAttributes");
            Map<String, Object> body = new HashMap<>();
            body.put("advancedAttributes", attrs);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath(
                "/ssot/connections/" + ToolUtils.encodePath(connectionId) + "/database-schemas",
                params
            );
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/{id}/objects", verb = "POST")
    @McpTool(
        name = "d360_connection_objects_list",
        description = "List objects/tables available in a connection. For DB connections, pass database and schema "
            + "via advancedAttributes (e.g., {\"database\":\"MY_DB\",\"schema\":\"PUBLIC\"})."
    )
    public String listConnectionObjects(
        @McpToolParam(description = "The connection ID") String connectionId,
        @McpToolParam(description = "JSON object of connector-specific properties (e.g., {\"database\":\"MY_DB\",\"schema\":\"PUBLIC\"})", required = false) String advancedAttributes,
        @McpToolParam(description = "JSON object of filter criteria for narrowing the result set", required = false) String filters,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = new HashMap<>();
            if (advancedAttributes != null && !advancedAttributes.isEmpty()) {
                body.put("advancedAttributes", ToolUtils.parseJson(advancedAttributes, Map.class, "advancedAttributes"));
            }
            if (filters != null && !filters.isEmpty()) {
                body.put("filters", ToolUtils.parseJson(filters, Map.class, "filters"));
            }

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath(
                "/ssot/connections/" + ToolUtils.encodePath(connectionId) + "/objects",
                params
            );
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/{id}/objects/{resourceName}/fields", verb = "POST")
    @McpTool(
        name = "d360_connection_object_fields_describe",
        description = "Describe field-level schema (names, types, primary keys) of an object in a connection. "
            + "Use d360_connection_objects_list first to discover the resourceName."
    )
    public String describeConnectionObjectFields(
        @McpToolParam(description = "The connection ID") String connectionId,
        @McpToolParam(description = "The object/table name (from d360_connection_objects_list)") String resourceName,
        @McpToolParam(description = "JSON object of connector-specific properties (e.g., {\"database\":\"MY_DB\",\"schema\":\"PUBLIC\"})", required = false) String advancedAttributes,
        @McpToolParam(description = "JSON object of filter criteria for narrowing the field set", required = false) String filters,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = new HashMap<>();
            if (advancedAttributes != null && !advancedAttributes.isEmpty()) {
                body.put("advancedAttributes", ToolUtils.parseJson(advancedAttributes, Map.class, "advancedAttributes"));
            }
            if (filters != null && !filters.isEmpty()) {
                body.put("filters", ToolUtils.parseJson(filters, Map.class, "filters"));
            }

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath(
                "/ssot/connections/" + ToolUtils.encodePath(connectionId)
                    + "/objects/" + ToolUtils.encodePath(resourceName) + "/fields",
                params
            );
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
