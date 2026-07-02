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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Data 360 Snowflake Connection Tools.
 * Includes Snowflake connection creation and general connection listing.
 */
@Component
public class SnowflakeConnectionTools {

    private final Data360Client client;

    public SnowflakeConnectionTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = "/ssot/connections", verb = "GET")
    @McpTool(
        name = "d360_snowflake_connection_list",
        description = "List Data 360 connections for a connector type. Use connectorType=SNOWFLAKE to list Snowflake connections."
    )
    public String listConnections(
        @McpToolParam(description = "Connector type to list, for example SNOWFLAKE, SalesforceDotCom, or SalesforceMarketingCloud.") String connectorType,
        @McpToolParam(description = "Developer name filter.", required = false) String devName,
        @McpToolParam(description = "Label filter.", required = false) String label,
        @McpToolParam(description = "Max number of results.", required = false) Integer limit,
        @McpToolParam(description = "Row offset.", required = false) Integer offset,
        @McpToolParam(description = "Sort field, for example label, createdDate, or lastModifiedDate.", required = false) String orderBy,
        @McpToolParam(description = "Organization ID filter.", required = false) String organizationId,
        @McpToolParam(description = "Filter group for the request.", required = false) String filterGroup
    ) {
        try {
            validateRequired("connectorType", connectorType);

            Map<String, Object> query = new LinkedHashMap<>();
            query.put("connectorType", connectorType.trim());
            putIfNotBlank(query, "devName", devName);
            putIfNotBlank(query, "label", label);
            query.put("limit", limit);
            query.put("offset", offset);
            putIfNotBlank(query, "orderBy", orderBy);
            putIfNotBlank(query, "organizationId", organizationId);
            putIfNotBlank(query, "filterGroup", filterGroup);

            Map result = client.get(ToolUtils.buildPath("/ssot/connections", query), Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a Snowflake connection using the already-authenticated Data 360 token.
     * The private key must be provided as content, not a filesystem path.
     */
    @ApiEndpoint(path = "/ssot/connections", verb = "POST")
    @McpTool(
        name = "d360_connection_create_snowflake",
        description = "Create a Snowflake connection in Data 360 using the existing authenticated Data 360 token. Provide the private key content directly, not a file path."
    )
    public String createSnowflakeConnection(
        @McpToolParam(description = "Human-readable label for the Snowflake connection.") String label,
        @McpToolParam(description = "Developer name for the connection. Optional; derived from label if omitted.", required = false) String name,
        @McpToolParam(description = "Snowflake username.") String username,
        @McpToolParam(description = "Snowflake private key content as a string. Pass the key material directly; do not pass a local file path.") String privateKey,
        @McpToolParam(description = "Snowflake account URL, for example https://my-account.snowflakecomputing.com.") String accountUrl,
        @McpToolParam(description = "Snowflake region, for example us-west-2.") String region,
        @McpToolParam(description = "Snowflake warehouse.") String warehouse,
        @McpToolParam(description = "Private key passphrase.", required = false) String passphrase,
        @McpToolParam(description = "Whether to use a private network route. Defaults to false.", required = false) Boolean hasPrivateNetworkRoute,
        @McpToolParam(description = "Private Network Route identifier when hasPrivateNetworkRoute=true.", required = false) String outboundNetworkConnection
    ) {
        try {
            validateRequired("label", label);
            validateRequired("username", username);
            validateRequired("privateKey", privateKey);
            validateRequired("accountUrl", accountUrl);
            validateRequired("region", region);
            validateRequired("warehouse", warehouse);

            String connectionName = isBlank(name) ? deriveConnectionName(label) : name.trim();

            List<Map<String, Object>> credentials = new ArrayList<>();
            credentials.add(param("authenticationOption", "KeyPair"));
            credentials.add(param("user", username.trim()));
            credentials.add(param("privateKey", privateKey.trim()));
            if (!isBlank(passphrase)) {
                credentials.add(param("passphrase", passphrase.trim()));
            }

            List<Map<String, Object>> parameters = new ArrayList<>();
            boolean usePrivateRoute = Boolean.TRUE.equals(hasPrivateNetworkRoute);
            parameters.add(param("hasPrivateNetworkRoute", String.valueOf(usePrivateRoute)));
            if (usePrivateRoute) {
                validateRequired("outboundNetworkConnection", outboundNetworkConnection);
                parameters.add(param("outboundnetworkconnection", outboundNetworkConnection.trim()));
            } else {
                parameters.add(param("accountUrl", accountUrl.trim()));
            }
            parameters.add(param("region", region.trim()));
            parameters.add(param("warehouse", warehouse.trim()));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("connectorType", "SNOWFLAKE");
            body.put("label", label.trim());
            body.put("name", connectionName);
            body.put("method", "Ingress");
            body.put("credentials", credentials);
            body.put("parameters", parameters);

            Map result = client.post("/ssot/connections", body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    private static Map<String, Object> param(String name, String value) {
        return Map.of(
            "paramName", name,
            "value", value
        );
    }

    private static void validateRequired(String fieldName, String value) {
        if (isBlank(value)) {
            throw new IllegalArgumentException("Missing required parameter: " + fieldName);
        }
    }

    private static void putIfNotBlank(Map<String, Object> query, String key, String value) {
        if (!isBlank(value)) {
            query.put(key, value.trim());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String deriveConnectionName(String label) {
        String normalized = label.trim().toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Could not derive a connection name from label.");
        }
        return normalized;
    }

}
