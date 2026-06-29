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
import com.salesforce.data360.mcp.model.request.datakit.DataKitDeployRequest;
import com.salesforce.data360.mcp.model.request.datakit.DataKitUndeployRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Data 360 DataKit Tools - Package management for Data 360 components.
 * Maps to /ssot/data-kits endpoints for deploying and managing DataKits.
 */
@Component
public class DataKitTools {

    private final Data360Client client;

    public DataKitTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all data kits in the org.
     * Discover all installed and managed data kits.
     */
    @ApiEndpoint(path = "/ssot/data-kits", verb = "GET")
    @McpTool(
        name = "d360_datakit_list",
        description = "List all data kits (packages)."
    )
    public String listDataKits(
        @McpToolParam(description = "Filter by namespace", required = false) String namespace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (namespace != null) params.put("namespace", namespace);
            String path = ToolUtils.buildPath("/ssot/data-kits", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific data kit by ID.
     * Returns datakit metadata, version info, and status.
     */
    @ApiEndpoint(path = "/ssot/data-kits/{id}", verb = "GET")
    @McpTool(
        name = "d360_datakit_get",
        description = "Get a data kit by ID."
    )
    public String getDataKit(
        @McpToolParam(description = "The data kit developer name") String dataKitDevName,
        @McpToolParam(description = "Filter by FBDK accessible in LOB flag", required = false) Boolean fbdkAccessibleInLob
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (fbdkAccessibleInLob != null) params.put("fbdkAccessibleInLob", fbdkAccessibleInLob);
            String path = ToolUtils.buildPath("/ssot/data-kits/" + ToolUtils.encodePath(dataKitDevName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get the manifest for a data kit.
     * Returns detailed manifest showing all components and dependencies.
     */
    @ApiEndpoint(path = "/ssot/datakit/{id}/manifest", verb = "GET")
    @McpTool(
        name = "d360_datakit_manifest",
        description = "Get a data kit's manifest."
    )
    public String getDataKitManifest(
        @McpToolParam(description = "The data kit developer name") String dataKitDevName
    ) {
        try {
            String path = "/ssot/datakit/" + ToolUtils.encodePath(dataKitDevName) + "/manifest";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Deploy a data kit by developer name.
     */
    @ApiEndpoint(path = "/ssot/data-kits/{dataKitDevName}", verb = "POST")
    @McpTool(
        name = "d360_datakit_deploy",
        description = "Deploy a data kit."
    )
    public String deployDataKit(
        @McpToolParam(description = "The data kit developer name") String dataKitDevName,
        @McpToolParam(description = "Data kit deploy request body") DataKitDeployRequest request,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Enable async deployment mode", required = false) Boolean asyncMode,
        @McpToolParam(description = "Filter by FBDK accessible in LOB flag", required = false) Boolean fbdkAccessibleInLob
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);
            if (asyncMode != null) params.put("asyncMode", asyncMode);
            if (fbdkAccessibleInLob != null) params.put("fbdkAccessibleInLob", fbdkAccessibleInLob);
            String path = ToolUtils.buildPath("/ssot/data-kits/" + ToolUtils.encodePath(dataKitDevName), params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Undeploy a data kit.
     * Remove all components from a datakit.
     */
    @ApiEndpoint(path = "/ssot/data-kits/{id}/undeploy", verb = "POST")
    @McpTool(
        name = "d360_datakit_undeploy",
        description = "Undeploy a data kit."
    )
    public String undeployDataKit(
        @McpToolParam(description = "The data kit developer name") String dataKitDevName,
        @McpToolParam(description = "Data kit undeploy request body") DataKitUndeployRequest request,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Enable async undeploy mode", required = false) Boolean asyncMode
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);
            if (asyncMode != null) params.put("asyncMode", asyncMode);
            String path = ToolUtils.buildPath("/ssot/data-kits/" + ToolUtils.encodePath(dataKitDevName) + "/undeploy", params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get the deployment status for a datakit deployment job.
     * Check deployment progress and results by job ID.
     */
    @ApiEndpoint(path = "/ssot/data-kits/deployment-jobs/{jobId}", verb = "GET")
    @McpTool(
        name = "d360_datakit_deploy_status",
        description = "Get deployment job status."
    )
    public String getDataKitDeploymentStatus(
        @McpToolParam(description = "The deployment job ID") String jobId
    ) {
        try {
            String path = "/ssot/data-kits/deployment-jobs/" + ToolUtils.encodePath(jobId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get the deployment status for a specific datakit component.
     * Check individual component deployment status.
     */
    @ApiEndpoint(path = "/ssot/data-kits/{id}/components/{cid}/deployment-status", verb = "GET")
    @McpTool(
        name = "d360_datakit_component_status",
        description = "Get component status within a data kit."
    )
    public String getDataKitComponentStatus(
        @McpToolParam(description = "The data kit developer name") String dataKitDevName,
        @McpToolParam(description = "The component name") String componentName,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-kits/" + ToolUtils.encodePath(dataKitDevName) + "/components/" + ToolUtils.encodePath(componentName) + "/deployment-status", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * List components available in the org for inclusion in data kits.
     */
    @ApiEndpoint(path = "/ssot/data-kits/available-components", verb = "GET")
    @McpTool(
        name = "d360_datakit_components",
        description = "List components available in the org for inclusion in data kits."
    )
    public String listDataKitComponents(
        @McpToolParam(description = "Filter by component type", required = false) String componentType,
        @McpToolParam(description = "Filter by data kit developer name", required = false) String dataKitDevName,
        @McpToolParam(description = "Maximum number of results to return", required = false) Integer limit,
        @McpToolParam(description = "Number of results to skip", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (componentType != null) params.put("componentType", componentType);
            if (dataKitDevName != null) params.put("dataKitDevName", dataKitDevName);
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            String path = ToolUtils.buildPath("/ssot/data-kits/available-components", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get dependencies for a specific datakit component.
     * See what other components this one depends on.
     */
    @ApiEndpoint(path = "/ssot/data-kits/{id}/components/{cid}/dependencies", verb = "GET")
    @McpTool(
        name = "d360_datakit_component_deps",
        description = "Get component dependencies."
    )
    public String getDataKitComponentDependencies(
        @McpToolParam(description = "The data kit developer name") String dataKitDevName,
        @McpToolParam(description = "The component name") String componentName,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Filter by component type", required = false) String componentType
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);
            if (componentType != null) params.put("componentType", componentType);
            String path = ToolUtils.buildPath("/ssot/data-kits/" + ToolUtils.encodePath(dataKitDevName) + "/components/" + ToolUtils.encodePath(componentName) + "/dependencies", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
