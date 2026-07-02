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
import com.salesforce.data360.mcp.model.request.sdm.SdmCalcDimensionCreateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmCalcDimensionUpdateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmCalcMeasurementCreateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmCalcMeasurementUpdateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmDataObjectCreateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmDataObjectUpdateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmMetricCreateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmMetricUpdateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmModelCloneRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmModelCreateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmModelUpdateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmRelationshipCreateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmRelationshipUpdateRequest;
import com.salesforce.data360.mcp.model.request.sdm.SdmSemanticQueryRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data 360 Semantic Data Model (SDM) Tools - 38 tools for semantic layer management.
 * Covers model CRUD, data objects, dimensions, measurements, calculated fields,
 * metrics, relationships, and semantic queries.
 *
 * Authoring APIs hit /ssot/semantic/models/...; the query API hits /semantic-engine/gateway.
 */
@Component
public class SdmTools {

    private final Data360Client client;

    public SdmTools(Data360Client client) {
        this.client = client;
    }

    // ============================================================
    // Model CRUD
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models", verb = "GET")
    @McpTool(
        name = "d360_sdm_list",
        description = "List all semantic data models."
    )
    public String listSemanticModels(
        @McpToolParam(description = "Dataspace name", required = false) String dataspace,
        @McpToolParam(description = "App filter", required = false) String app,
        @McpToolParam(description = "Category filter (Marketing, Commerce, Sales, Service, Other)", required = false) String category,
        @McpToolParam(description = "Label filter", required = false) String label,
        @McpToolParam(description = "Max number of results", required = false) Integer limit,
        @McpToolParam(description = "Offset for pagination", required = false) Integer offset,
        @McpToolParam(description = "Order-by clause", required = false) String orderBy,
        @McpToolParam(description = "Search term", required = false) String searchTerm,
        @McpToolParam(description = "Comma-separated semantic model api names", required = false) String semanticModelApiNames,
        @McpToolParam(description = "Source-creation filter", required = false) String sourceCreation,
        @McpToolParam(description = "Source-creation-name filter", required = false) String sourceCreationName
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("app", app);
            params.put("category", category);
            params.put("dataspace", dataspace);
            params.put("label", label);
            params.put("limit", limit);
            params.put("offset", offset);
            params.put("orderBy", orderBy);
            params.put("searchTerm", searchTerm);
            params.put("semanticModelApiNames", semanticModelApiNames);
            params.put("sourceCreation", sourceCreation);
            params.put("sourceCreationName", sourceCreationName);
            String path = ToolUtils.buildPath("/ssot/semantic/models", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}", verb = "GET")
    @McpTool(
        name = "d360_sdm_get",
        description = "Gets the semantic model."
    )
    public String getSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "ABAC filtering mode", required = false) Boolean abacFilteringMode,
        @McpToolParam(description = "Flag to allow unmapped fields", required = false) Boolean allowUnmapped,
        @McpToolParam(description = "Field name filter", required = false) String fieldName,
        @McpToolParam(description = "Fine-grain security flag", required = false) Boolean fineGrainSecurity,
        @McpToolParam(description = "Flag to include model content", required = false) Boolean includeModelContent,
        @McpToolParam(description = "Flag to include table keys", required = false) Boolean includeTableKeys
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("abacFilteringMode", abacFilteringMode);
            params.put("allowUnmapped", allowUnmapped);
            params.put("fieldName", fieldName);
            params.put("fineGrainSecurity", fineGrainSecurity);
            params.put("includeModelContent", includeModelContent);
            params.put("includeTableKeys", includeTableKeys);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models", verb = "POST")
    @McpTool(
        name = "d360_sdm_create",
        description = "Creates a new semantic model. Body: { apiName, label, description, dataspace (REQUIRED in body) }. Then add data objects, relationships, calc dims/measures via separate tools. DEPENDENCY: All CIs/DMOs must exist before adding as data objects."
    )
    public String createSemanticModel(
        @McpToolParam(description = "The request body for semantic model creation") SdmModelCreateRequest request,
        @McpToolParam(description = "Flag to allow external definitions", required = false) Boolean allowExternalDefinitions,
        @McpToolParam(description = "Flag to allow unmapped fields", required = false) Boolean allowUnmapped,
        @McpToolParam(description = "Flag to inherit data cloud relationships", required = false) Boolean inheritDcRelationship
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("allowExternalDefinitions", allowExternalDefinitions);
            params.put("allowUnmapped", allowUnmapped);
            params.put("inheritDcRelationship", inheritDcRelationship);
            String path = ToolUtils.buildPath("/ssot/semantic/models", params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_sdm_update",
        description = "Updates specific entity fields in the Semantic Model object without affecting child dependencies or relationships."
    )
    public String updateSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "The request body for updating semantic model") SdmModelUpdateRequest request,
        @McpToolParam(description = "Flag to allow unmapped fields", required = false) Boolean allowUnmapped
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("allowUnmapped", allowUnmapped);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId), params);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_delete",
        description = "Deletes a specific Semantic Model."
    )
    public String deleteSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/clone", verb = "POST")
    @McpTool(
        name = "d360_sdm_clone",
        description = "Clones an existing semantic model."
    )
    public String cloneSemanticModel(
        @McpToolParam(description = "Model API name or ID to clone") String modelApiNameOrId,
        @McpToolParam(description = "The request body for semantic model cloning") SdmModelCloneRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/clone";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/validate", verb = "GET")
    @McpTool(
        name = "d360_sdm_validate",
        description = "Validate the semantic model."
    )
    public String validateSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/validate";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/external-dependencies", verb = "GET")
    @McpTool(
        name = "d360_sdm_dependencies",
        description = "Retrieves all external dependencies for a specific model."
    )
    public String getSemanticModelDependencies(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/external-dependencies";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Data Objects (tables within a model)
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects", verb = "POST")
    @McpTool(
        name = "d360_sdm_data_object_create",
        description = "Add an existing Data Cloud Object as a new Semantic Data Object to a specific Semantic Model. Required body: { dataObjectName, label, dataObjectType, shouldIncludeAllFields }. dataObjectType values: 'Dmo' for DMOs, 'Dlo' for DLOs, 'Cio' for Calculated Insights (NOT 'CalculatedInsight'). Use shouldIncludeAllFields: true to auto-include all fields as dimensions/measurements."
    )
    public String createDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "The request body for adding data object to a semantic model") SdmDataObjectCreateRequest request,
        @McpToolParam(description = "Flag to allow unmapped fields", required = false) Boolean allowUnmapped
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("allowUnmapped", allowUnmapped);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects", params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects", verb = "GET")
    @McpTool(
        name = "d360_sdm_data_objects_list",
        description = "Retrieves a list of all objects within a specific Semantic Model."
    )
    public String listDataObjects(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_data_object_get",
        description = "Retrieves the attributes of a specific Semantic Data Object in a specific Semantic Model."
    )
    public String getDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}", verb = "PUT")
    @McpTool(
        name = "d360_sdm_data_object_update",
        description = "Updates the fields in a specific Semantic Data Object in a specific Semantic Model."
    )
    public String updateDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "The request body for updating data object") SdmDataObjectUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_data_object_delete",
        description = "Delete a Semantic Data Object in a specific Semantic Model."
    )
    public String deleteDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "Flag to cascade-delete dependent relationships", required = false) Boolean relationshipCascadeDelete
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("relationshipCascadeDelete", relationshipCascadeDelete);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Dimensions & Measurements
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}/dimensions", verb = "GET")
    @McpTool(
        name = "d360_sdm_dimensions_list",
        description = "Retrieves a list of all Semantic Dimensions in a specific Semantic Model."
    )
    public String listDimensions(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "Dimension name filter", required = false) String name
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("name", name);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId) + "/dimensions", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}/measurements", verb = "GET")
    @McpTool(
        name = "d360_sdm_measurements_list",
        description = "Retrieves a list of all Semantic Measurements in a specific Semantic Model."
    )
    public String listMeasurements(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "Measurement name filter", required = false) String name
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("name", name);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId) + "/measurements", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Calculated Dimensions
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions", verb = "GET")
    @McpTool(
        name = "d360_sdm_calc_dims_list",
        description = "Retrieves a list of all Semantic Calculated Dimensions in a specific Semantic Model."
    )
    public String listCalculatedDimensions(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated dimension name filter", required = false) String name
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("name", name);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions", verb = "POST")
    @McpTool(
        name = "d360_sdm_calc_dim_create",
        description = "Creates a new Semantic Calculated Dimension in a specific Semantic Model. Body: { label, expression, dataType: 'Text'|'Number'|'DateTime' }. Formula syntax uses [DataObject].[Field] references (NOT {field}). Use IF/ELSEIF/ELSE/END for conditionals (not CASE WHEN). Example: IF [MyObj].[field1] > 10 THEN 'High' ELSE 'Low' END"
    )
    public String createCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "The request body for creating calculated dimension") SdmCalcDimensionCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}", verb = "GET")
    @McpTool(
        name = "d360_sdm_calc_dim_get",
        description = "Retrieves the attributes of a specific Semantic Calculated Dimensions in a specific Semantic Model."
    )
    public String getCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated dimension name or ID") String calculatedDimensionNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions/" + ToolUtils.encodePath(calculatedDimensionNameOrId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}", verb = "PUT")
    @McpTool(
        name = "d360_sdm_calc_dim_update",
        description = "Updates the attributes of a specific Semantic Calculated Dimension in a specific Semantic Model."
    )
    public String updateCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated dimension name or ID") String calculatedDimensionNameOrId,
        @McpToolParam(description = "The request body for updating calculated dimension") SdmCalcDimensionUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions/" + ToolUtils.encodePath(calculatedDimensionNameOrId);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_calc_dim_delete",
        description = "Deletes a specific Semantic Calculated Dimension in a specific Semantic Model."
    )
    public String deleteCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated dimension name or ID") String calculatedDimensionNameOrId,
        @McpToolParam(description = "Flag to cascade-delete dependent relationships", required = false) Boolean relationshipCascadeDelete
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("relationshipCascadeDelete", relationshipCascadeDelete);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions/" + ToolUtils.encodePath(calculatedDimensionNameOrId), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Calculated Measurements
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements", verb = "GET")
    @McpTool(
        name = "d360_sdm_calc_measures_list",
        description = "Retrieves a list of all Semantic Calculated Measurements in a specific Semantic Model."
    )
    public String listCalculatedMeasurements(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated measurement name filter", required = false) String name
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("name", name);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements", verb = "POST")
    @McpTool(
        name = "d360_sdm_calc_measure_create",
        description = "Creates a new Semantic Calculated Measurement in a specific Semantic Model. Body: { label, expression, dataType: 'Number', aggregationType: 'UserAgg' }. IMPORTANT: aggregationType 'UserAgg' is REQUIRED for any formula containing aggregation functions (SUM, COUNT, AVG, etc.). Formula syntax uses [DataObject].[Field] references. Use IIF(condition, true_val, false_val) for inline conditionals."
    )
    public String createCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Request body for creating calculated measurement") SdmCalcMeasurementCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements/{mid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_calc_measure_get",
        description = "Retrieves the attributes of a specific Semantic Calculated Measurement in a specific Semantic Model."
    )
    public String getCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated measurement name or ID") String calculatedMeasurementNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements/" + ToolUtils.encodePath(calculatedMeasurementNameOrId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements/{mid}", verb = "PUT")
    @McpTool(
        name = "d360_sdm_calc_measure_update",
        description = "Updates the attributes of a specific Semantic Calculated Measurement in a specific Semantic Model."
    )
    public String updateCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated measurement name or ID") String calculatedMeasurementNameOrId,
        @McpToolParam(description = "Request body for updating calculated measurement") SdmCalcMeasurementUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements/" + ToolUtils.encodePath(calculatedMeasurementNameOrId);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements/{mid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_calc_measure_delete",
        description = "Deletes a specific Semantic Calculated Measurement in a specific Semantic Model."
    )
    public String deleteCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated measurement name or ID") String calculatedMeasurementNameOrId,
        @McpToolParam(description = "Flag to cascade-delete dependent relationships", required = false) Boolean relationshipCascadeDelete
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("relationshipCascadeDelete", relationshipCascadeDelete);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements/" + ToolUtils.encodePath(calculatedMeasurementNameOrId), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Metrics
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics", verb = "GET")
    @McpTool(
        name = "d360_sdm_metrics_list",
        description = "Retrieves a result of Semantic Metrics."
    )
    public String listMetrics(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics/{mid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_metric_get",
        description = "Retrieves a specific Semantic Metric."
    )
    public String getMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Metric name or ID") String metricNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics/" + ToolUtils.encodePath(metricNameOrId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics", verb = "POST")
    @McpTool(
        name = "d360_sdm_metric_create",
        description = "Creates a new semantic metric."
    )
    public String createMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Request body for creating SDM metric") SdmMetricCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            body.put("definition", ToolUtils.parseJson(request.getDefinition(), Map.class, "definition"));
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics/{mid}", verb = "PUT")
    @McpTool(
        name = "d360_sdm_metric_update",
        description = "Updates a specific Semantic Metric."
    )
    public String updateMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Metric name or ID") String metricNameOrId,
        @McpToolParam(description = "Request body for updating SDM metric") SdmMetricUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            if (request.getDefinition() != null) {
                body.put("definition", ToolUtils.parseJson(request.getDefinition(), Map.class, "definition"));
            }
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics/" + ToolUtils.encodePath(metricNameOrId);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics/{mid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_metric_delete",
        description = "Deletes a specific Semantic Metric."
    )
    public String deleteMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Metric name or ID") String metricNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics/" + ToolUtils.encodePath(metricNameOrId);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Relationships
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships", verb = "POST")
    @McpTool(
        name = "d360_sdm_relationship_create",
        description = "Creates a new relationship between specific Semantic Data Objects in a specific Semantic Model. Body format: { label, leftSemanticDefinitionApiName, rightSemanticDefinitionApiName, criteria: [{joinOperator: 'Equals', leftFieldType: 'TableField', leftSemanticFieldApiName, rightFieldType: 'TableField', rightSemanticFieldApiName}], cardinality: 'OneToMany|ManyToOne|OneToOne|ManyToMany', joinType: 'Auto' }. Use the semantic field apiNames from d360_sdm_data_objects_list (e.g. 'Individual_Id18'), NOT the underlying DMO field names."
    )
    public String createRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Request body for creating SDM relationship") SdmRelationshipCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            if (request.getCriteria() != null) {
                body.put("criteria", ToolUtils.parseJson(request.getCriteria(), Map.class, "criteria"));
            }
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships", verb = "GET")
    @McpTool(
        name = "d360_sdm_relationships_list",
        description = "Retrieves a list of relationships between all Semantic Data Objects in a specific Semantic Model."
    )
    public String listRelationships(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships/{rid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_relationship_get",
        description = "Retrieves the attributes for a specific relationship between Semantic Definitions in a specific Semantic Model."
    )
    public String getRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Relationship name or ID") String relationshipNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships/" + ToolUtils.encodePath(relationshipNameOrId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships/{rid}", verb = "PUT")
    @McpTool(
        name = "d360_sdm_relationship_update",
        description = "Updates the attributes for a specific relationship between Semantic Definitions in a specific Semantic Model."
    )
    public String updateRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Relationship name or ID") String relationshipNameOrId,
        @McpToolParam(description = "Request body for updating SDM relationship") SdmRelationshipUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            if (request.getCriteria() != null) {
                body.put("criteria", ToolUtils.parseJson(request.getCriteria(), Map.class, "criteria"));
            }
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships/" + ToolUtils.encodePath(relationshipNameOrId);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships/{rid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_relationship_delete",
        description = "Delete a specific relationship between Semantic Data Objects in a specific Semantic Model."
    )
    public String deleteRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Relationship name or ID") String relationshipNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships/" + ToolUtils.encodePath(relationshipNameOrId);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Formula Metadata & Permissions
    // ============================================================

    @ApiEndpoint(path = "/ssot/semantic/models/formula-metadata", verb = "GET")
    @McpTool(
        name = "d360_sdm_formula_metadata",
        description = "Endpoint to get metadata for supported formulas from off-core semantic engine."
    )
    public String getFormulaMetadata(
        @McpToolParam(description = "Model API name or ID", required = false) String modelApiNameOrId
    ) {
        try {
            String path = "/ssot/semantic/models/formula-metadata";
            if (modelApiNameOrId != null) {
                path = ToolUtils.buildPath(path, Map.of("modelApiNameOrId", modelApiNameOrId));
            }
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/permissions", verb = "GET")
    @McpTool(
        name = "d360_sdm_permissions",
        description = "Gets the semantic permissions."
    )
    public String getSemanticPermissions() {
        try {
            Map result = client.get("/ssot/semantic/permissions", Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Semantic Query (non-SSOT surface: /semantic-engine/gateway)
    // ============================================================

    @ApiEndpoint(path = "/semantic-engine/gateway", verb = "POST")
    @McpTool(
        name = "d360_sdm_query",
        description = "Endpoint to query off-core semantic engine. The body must follow this structure: { semanticModelId, structuredSemanticQuery: { fields: [{expression, alias, rowGrouping?, semanticAggregationMethod?}, ...], options: {limitOptions: {limit: 10}} } }. For regular fields: use tableField with tableName (e.g. 'Norm_Geography' in table 'AccountRevenue'). For calculated dimensions/measurements: use semanticField with just name (e.g. {semanticField: {name: 'Cluster_Size_Tier'}}) — these are model-level, NOT on a specific table."
    )
    public String executeSemanticQuery(
            @McpToolParam(description = "Request body for executing semantic query") SdmSemanticQueryRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            body.put("structuredSemanticQuery", ToolUtils.parseJson(request.getStructuredSemanticQuery(), Map.class, "structuredSemanticQuery"));
            Map result = client.post("/semantic-engine/gateway", body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
