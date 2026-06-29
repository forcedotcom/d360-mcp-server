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
import com.salesforce.data360.mcp.model.request.metadata.ConnectionCreateRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionDbSchemaCollectionRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionFieldCollectionRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionObjectCollectionRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionPatchRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionTestRequest;
import com.salesforce.data360.mcp.model.request.metadata.PrismMetadataSearchInputRepresentation;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
        description = "POST method for Prism Metadata Search. PREFERRED way to discover Data 360 metadata. Search using natural language instead of listing all entities. The apiName in results maps to entityName in d360_metadata."
    )
    public String searchMetadata(
        @McpToolParam(description = "Request body for Prism Metadata Search") PrismMetadataSearchInputRepresentation request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map result = client.post("/connect/search/metadata/results", body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
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
        @McpToolParam(description = "Dataspace name", required = false) String dataspace
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
        @McpToolParam(description = "Dataspace name", required = false) String dataspace,
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
        @McpToolParam(description = "Connector type to filter by", required = false) String connectorType,
        @McpToolParam(description = "Filter group for the request", required = false) String filterGroup,
        @McpToolParam(description = "Filter by developer name", required = false) String devName,
        @McpToolParam(description = "Filter by label", required = false) String label,
        @McpToolParam(description = "Max number of results to return", required = false) Integer limit,
        @McpToolParam(description = "Row offset for pagination", required = false) Integer offset,
        @McpToolParam(description = "Field to sort results by", required = false) String orderBy,
        @McpToolParam(description = "Filter by organization ID", required = false) String organizationId
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (connectorType != null) params.put("connectorType", connectorType);
            if (filterGroup != null) params.put("filterGroup", filterGroup);
            if (devName != null) params.put("devName", devName);
            if (label != null) params.put("label", label);
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            if (orderBy != null) params.put("orderBy", orderBy);
            if (organizationId != null) params.put("organizationId", organizationId);

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
        @McpToolParam(description = "Filter group for the request", required = false) String filterGroup
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (filterGroup != null) params.put("filterGroup", filterGroup);

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
        description = "Create a new data connection. The body must include 'connectorType' "
            + "along with 'parameters' (not 'params') for connection settings, and 'credentials' for auth. "
            + "See payload_examples for Snowflake and Salesforce body formats."
    )
    public String createConnection(
        @McpToolParam(description = "Request body for creating a connection") ConnectionCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/connections";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_connection_update",
        description = "Update an existing connection. NOTE: Only Salesforce Marketing Cloud connections can be patched via this endpoint. Non-MC connectors (e.g. AwsS3, SFTP, Snowflake) will be rejected with 'Could not resolve type id ... as a subtype of ConnectionPatchInputRepresentation' because the underlying server representation only registers MC subtypes. To modify a non-MC connection, delete and recreate it."
    )
    public String updateConnection(
        @McpToolParam(description = "The connection ID") String connectionId,
        @McpToolParam(description = "Request body for patching a connection") ConnectionPatchRequest request,
        @McpToolParam(description = "Filter group for the request", required = false) String filterGroup
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (filterGroup != null) params.put("filterGroup", filterGroup);

            String path = ToolUtils.buildPath("/ssot/connections/" + ToolUtils.encodePath(connectionId), params);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/connections/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_connection_delete",
        description = "Delete a connection."
    )
    public String deleteConnection(
        @McpToolParam(description = "The connection ID") String connectionId
    ) {
        try {
            String path = "/ssot/connections/" + ToolUtils.encodePath(connectionId);
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
        @McpToolParam(description = "Request body for testing a connection") ConnectionTestRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/connections/actions/test";
            Map result = client.post(path, body, Map.class);
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
        @McpToolParam(description = "Field group for the request", required = false) String fieldGroup,
        @McpToolParam(description = "Filters expression for the request", required = false) String filters,
        @McpToolParam(description = "Order by clause for sorting results", required = false) String orderBy
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (fieldGroup != null) params.put("fieldGroup", fieldGroup);
            if (filters != null) params.put("filters", filters);
            if (orderBy != null) params.put("orderBy", orderBy);

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
        @McpToolParam(description = "The connector type") String connectorType
    ) {
        try {
            String path = "/ssot/connectors/" + ToolUtils.encodePath(connectorType);
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
        @McpToolParam(description = "Request body for listing database schemas") ConnectionDbSchemaCollectionRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/connections/" + ToolUtils.encodePath(connectionId) + "/database-schemas";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
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
        @McpToolParam(description = "Request body for listing connection objects") ConnectionObjectCollectionRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/connections/" + ToolUtils.encodePath(connectionId) + "/objects";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
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
        @McpToolParam(description = "Request body for describing connection object fields") ConnectionFieldCollectionRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/connections/" + ToolUtils.encodePath(connectionId)
                + "/objects/" + ToolUtils.encodePath(resourceName) + "/fields";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
