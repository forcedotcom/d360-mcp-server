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
import com.salesforce.data360.mcp.model.request.mapping.FieldMappingAddRequest;
import com.salesforce.data360.mcp.model.request.mapping.MappingCreateRequest;
import com.salesforce.data360.mcp.model.request.mapping.MappingUpdateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Data 360 Data Model Object Mapping Tools - CRUD operations for DLO-to-DMO field mappings.
 * Maps to /ssot/data-model-object-mappings/* endpoints.
 */
@Component
public class MappingTools {

    private final Data360Client client;

    public MappingTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List DMO mappings by DMO developer name or source CRM object name.
     * At least one of dmoDeveloperName or sourceObjectName must be provided.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings", verb = "GET")
    @McpTool(
        name = "d360_dmo_mapping_list",
        description = "List DMO mappings filtered by DMO developer name or CRM source object name. "
            + "Provide either dmoDeveloperName or sourceObjectName (at least one is required)."
    )
    public String listMappingsBySource(
        @McpToolParam(description = "DMO developer name (e.g. 'ssot__Account__dlm'). Required if sourceObjectName is not provided.", required = false) String dmoDeveloperName,
        @McpToolParam(description = "DLO developer name to further filter results", required = false) String dloDeveloperName,
        @McpToolParam(description = "Salesforce CRM source object name (e.g. 'Account'). Required if dmoDeveloperName is not provided.", required = false) String sourceObjectName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        if (dmoDeveloperName == null && sourceObjectName == null) {
            return JsonUtil.toJson(Map.of("error", "Either dmoDeveloperName or sourceObjectName is required"));
        }

        try {
            Map<String, Object> params = new HashMap<>();
            if (dmoDeveloperName != null) params.put("dmoDeveloperName", dmoDeveloperName);
            if (dloDeveloperName != null) params.put("dloDeveloperName", dloDeveloperName);
            if (sourceObjectName != null) params.put("sourceObjectName", sourceObjectName);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-model-object-mappings", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific DMO mapping.
     * Use this to inspect the field-level mapping between a DLO and DMO.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings/{mappingName}", verb = "GET")
    @McpTool(
        name = "d360_dmo_mapping_get",
        description = "Get a specific DMO mapping."
    )
    public String getDataModelObjectMapping(
        @McpToolParam(description = "Developer name (e.g. Account_std_map_Account_1775572837882) of the Mapping") String mappingDeveloperName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-model-object-mappings/" + ToolUtils.encodePath(mappingDeveloperName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new DMO mapping.
     * PREREQUISITE: Call d360_dlo_get first to get source field API names, and d360_dmo_get for target field API names.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings", verb = "POST")
    @McpTool(
        name = "d360_dmo_mapping_create",
        description = "Create a single DMO mapping (one source-to-DMO mapping per call). "
            + "For standard objects, prefer d360_standard_mapping_create which creates ALL target DMO mappings in one call. "
            + "Use this tool for custom mappings where you need to specify field mappings manually. "
            + "IMPORTANT: DLO field names include a __c suffix (e.g., customer_id__c not customer_id). Use d360_dlo_get to get exact field names. "
            + "For Engagement DLOs, the event date field MUST be included in the mapping, and source/target field types must match exactly "
            + "(Date→Date, DateTime→DateTime). If the target DMO has no field matching the DLO event date type, create a custom DMO with a matching field."
    )
    public String createDataModelObjectMapping(
        MappingCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-model-object-mappings", params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing DMO mapping.
     * Use this to add, remove, or change field mappings between a DLO and DMO.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings/{mappingName}", verb = "PATCH")
    @McpTool(
        name = "d360_dmo_mapping_update",
        description = "Update an existing DMO mapping."
    )
    public String updateDataModelObjectMapping(
        @McpToolParam(description = "Developer name (e.g. Account_std_map_Account_1775572837882) of the Mapping") String mappingName,
        MappingUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-model-object-mappings/" + ToolUtils.encodePath(mappingName), params);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a DMO mapping.
     * Use this to remove a DLO-to-DMO field mapping. The data stream and DMO still exist but data will no longer flow into the target DMO from that source.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings/{mappingName}", verb = "DELETE")
    @McpTool(
        name = "d360_dmo_mapping_delete",
        description = "Delete a DMO mapping."
    )
    public String deleteDataModelObjectMapping(
        @McpToolParam(description = "Developer name (e.g. Account_std_map_Account_1775572837882) of the Mapping") String mappingName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-model-object-mappings/" + ToolUtils.encodePath(mappingName), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Add field mappings to an existing object mapping.
     * Use this to add new DLO-to-DMO field-level mappings without recreating the entire object mapping.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings/{mappingName}/field-mappings", verb = "PATCH")
    @McpTool(
        name = "d360_dmo_field_mapping_add",
        description = "Add field mappings to an existing object mapping between a DLO and DMO."
    )
    public String addFieldMappings(
        @McpToolParam(description = "Developer name of the object mapping (e.g. Contact_00D000000000000_map_ContactPointAddress_1714460254874)") String mappingName,
        FieldMappingAddRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-model-object-mappings/" + ToolUtils.encodePath(mappingName) + "/field-mappings", params);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a single field mapping from an object mapping.
     * Use this to remove an individual field-level mapping without deleting the entire object mapping.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings/{mappingName}/field-mappings/{fieldMappingName}", verb = "DELETE")
    @McpTool(
        name = "d360_dmo_field_mapping_delete",
        description = "Delete a single field mapping from an existing object mapping."
    )
    public String deleteFieldMapping(
        @McpToolParam(description = "Developer name of the object mapping") String mappingName,
        @McpToolParam(description = "Developer name of the field mapping to delete (e.g. MailingCity__c_fieldmap_ssot__CityId__c)") String fieldMappingName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath(
                "/ssot/data-model-object-mappings/" + ToolUtils.encodePath(mappingName)
                    + "/field-mappings/" + ToolUtils.encodePath(fieldMappingName), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
