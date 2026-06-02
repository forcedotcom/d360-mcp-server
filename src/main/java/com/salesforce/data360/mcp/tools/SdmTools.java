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
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}", verb = "GET")
    @McpTool(
        name = "d360_sdm_get",
        description = "Get a semantic data model by API name or ID."
    )
    public String getSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models", verb = "POST")
    @McpTool(
        name = "d360_sdm_create",
        description = "Create a semantic data model shell. Body: { apiName, label, description, dataspace (REQUIRED in body) }. Then add data objects, relationships, calc dims/measures via separate tools. DEPENDENCY: All CIs/DMOs must exist before adding as data objects."
    )
    public String createSemanticModel(
        SdmModelCreateRequest request,
        @McpToolParam(description = "Optional dataspace query parameter", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_sdm_update",
        description = "Update a semantic data model."
    )
    public String updateSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        SdmModelUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_delete",
        description = "Delete a semantic data model."
    )
    public String deleteSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/clone", verb = "POST")
    @McpTool(
        name = "d360_sdm_clone",
        description = "Clone a semantic data model."
    )
    public String cloneSemanticModel(
        @McpToolParam(description = "Model API name or ID to clone") String modelApiNameOrId,
        SdmModelCloneRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/clone", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/validate", verb = "POST")
    @McpTool(
        name = "d360_sdm_validate",
        description = "Validate a semantic data model."
    )
    public String validateSemanticModel(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/validate", dataspace);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/dependencies", verb = "GET")
    @McpTool(
        name = "d360_sdm_dependencies",
        description = "Get dependencies of a semantic model."
    )
    public String getSemanticModelDependencies(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/dependencies", dataspace);
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
        description = "Add a data object to a semantic model. Required body: { dataObjectName, label, dataObjectType, shouldIncludeAllFields }. dataObjectType values: 'Dmo' for DMOs, 'Dlo' for DLOs, 'Cio' for Calculated Insights (NOT 'CalculatedInsight'). Use shouldIncludeAllFields: true to auto-include all fields as dimensions/measurements."
    )
    public String createDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        SdmDataObjectCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects", verb = "GET")
    @McpTool(
        name = "d360_sdm_data_objects_list",
        description = "List data objects in a semantic model."
    )
    public String listDataObjects(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_data_object_get",
        description = "Get a data object from a semantic model."
    )
    public String getDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}", verb = "PATCH")
    @McpTool(
        name = "d360_sdm_data_object_update",
        description = "Update a data object in a semantic model."
    )
    public String updateDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        SdmDataObjectUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_data_object_delete",
        description = "Delete a data object from a semantic model."
    )
    public String deleteDataObject(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId), dataspace);
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
        description = "List dimensions for a data object in a semantic model."
    )
    public String listDimensions(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId) + "/dimensions", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/data-objects/{oid}/measurements", verb = "GET")
    @McpTool(
        name = "d360_sdm_measurements_list",
        description = "List measurements for a data object in a semantic model."
    )
    public String listMeasurements(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Data object name or ID") String dataObjectNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/data-objects/" + ToolUtils.encodePath(dataObjectNameOrId) + "/measurements", dataspace);
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
        description = "List calculated dimensions in a semantic model."
    )
    public String listCalculatedDimensions(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions", verb = "POST")
    @McpTool(
        name = "d360_sdm_calc_dim_create",
        description = "Create a calculated dimension in a semantic model. Body: { label, expression, dataType: 'Text'|'Number'|'DateTime' }. Formula syntax uses [DataObject].[Field] references (NOT {field}). Use IF/ELSEIF/ELSE/END for conditionals (not CASE WHEN). Example: IF [MyObj].[field1] > 10 THEN 'High' ELSE 'Low' END"
    )
    public String createCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        SdmCalcDimensionCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}", verb = "GET")
    @McpTool(
        name = "d360_sdm_calc_dim_get",
        description = "Get a calculated dimension from a semantic model."
    )
    public String getCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated dimension ID") String calculatedDimensionId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions/" + ToolUtils.encodePath(calculatedDimensionId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}", verb = "PATCH")
    @McpTool(
        name = "d360_sdm_calc_dim_update",
        description = "Update a calculated dimension in a semantic model."
    )
    public String updateCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated dimension ID") String calculatedDimensionId,
        SdmCalcDimensionUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions/" + ToolUtils.encodePath(calculatedDimensionId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_calc_dim_delete",
        description = "Delete a calculated dimension from a semantic model."
    )
    public String deleteCalculatedDimension(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated dimension ID") String calculatedDimensionId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-dimensions/" + ToolUtils.encodePath(calculatedDimensionId), dataspace);
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
        description = "List calculated measurements in a semantic model."
    )
    public String listCalculatedMeasurements(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements", verb = "POST")
    @McpTool(
        name = "d360_sdm_calc_measure_create",
        description = "Create a calculated measurement in a semantic model. Body: { label, expression, dataType: 'Number', aggregationType: 'UserAgg' }. IMPORTANT: aggregationType 'UserAgg' is REQUIRED for any formula containing aggregation functions (SUM, COUNT, AVG, etc.). Formula syntax uses [DataObject].[Field] references. Use IIF(condition, true_val, false_val) for inline conditionals."
    )
    public String createCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        SdmCalcMeasurementCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements/{mid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_calc_measure_get",
        description = "Get a calculated measurement from a semantic model."
    )
    public String getCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated measure ID") String calculatedMeasureId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements/" + ToolUtils.encodePath(calculatedMeasureId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements/{mid}", verb = "PATCH")
    @McpTool(
        name = "d360_sdm_calc_measure_update",
        description = "Update a calculated measurement in a semantic model."
    )
    public String updateCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated measure ID") String calculatedMeasureId,
        SdmCalcMeasurementUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements/" + ToolUtils.encodePath(calculatedMeasureId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/calculated-measurements/{mid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_calc_measure_delete",
        description = "Delete a calculated measurement from a semantic model."
    )
    public String deleteCalculatedMeasurement(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Calculated measure ID") String calculatedMeasureId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/calculated-measurements/" + ToolUtils.encodePath(calculatedMeasureId), dataspace);
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
        description = "List metrics in a semantic model."
    )
    public String listMetrics(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics/{mid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_metric_get",
        description = "Get a metric from a semantic model."
    )
    public String getMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Metric name or ID") String metricNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics/" + ToolUtils.encodePath(metricNameOrId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics", verb = "POST")
    @McpTool(
        name = "d360_sdm_metric_create",
        description = "Create a metric in a semantic model."
    )
    public String createMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        SdmMetricCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            body.put("definition", ToolUtils.parseJson(request.getDefinition(), Map.class, "definition"));
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics/{mid}", verb = "PATCH")
    @McpTool(
        name = "d360_sdm_metric_update",
        description = "Update a metric in a semantic model."
    )
    public String updateMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Metric name or ID") String metricNameOrId,
        SdmMetricUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            if (request.getDefinition() != null) {
                body.put("definition", ToolUtils.parseJson(request.getDefinition(), Map.class, "definition"));
            }
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics/" + ToolUtils.encodePath(metricNameOrId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/metrics/{mid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_metric_delete",
        description = "Delete a metric from a semantic model."
    )
    public String deleteMetric(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Metric name or ID") String metricNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/metrics/" + ToolUtils.encodePath(metricNameOrId), dataspace);
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
        description = "Create a relationship between data objects in a semantic model. Body format: { label, leftSemanticDefinitionApiName, rightSemanticDefinitionApiName, criteria: [{joinOperator: 'Equals', leftFieldType: 'TableField', leftSemanticFieldApiName, rightFieldType: 'TableField', rightSemanticFieldApiName}], cardinality: 'OneToMany|ManyToOne|OneToOne|ManyToMany', joinType: 'Auto' }. Use the semantic field apiNames from d360_sdm_data_objects_list (e.g. 'Individual_Id18'), NOT the underlying DMO field names."
    )
    public String createRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        SdmRelationshipCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            if (request.getCriteria() != null) {
                body.put("criteria", ToolUtils.parseJson(request.getCriteria(), Map.class, "criteria"));
            }
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships", verb = "GET")
    @McpTool(
        name = "d360_sdm_relationships_list",
        description = "List relationships in a semantic model."
    )
    public String listRelationships(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships/{rid}", verb = "GET")
    @McpTool(
        name = "d360_sdm_relationship_get",
        description = "Get a relationship from a semantic model."
    )
    public String getRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Relationship ID") String relationshipId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships/" + ToolUtils.encodePath(relationshipId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships/{rid}", verb = "PATCH")
    @McpTool(
        name = "d360_sdm_relationship_update",
        description = "Update a relationship in a semantic model."
    )
    public String updateRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Relationship ID") String relationshipId,
        SdmRelationshipUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            if (request.getCriteria() != null) {
                body.put("criteria", ToolUtils.parseJson(request.getCriteria(), Map.class, "criteria"));
            }
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships/" + ToolUtils.encodePath(relationshipId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/semantic/models/{id}/relationships/{rid}", verb = "DELETE")
    @McpTool(
        name = "d360_sdm_relationship_delete",
        description = "Delete a relationship from a semantic model."
    )
    public String deleteRelationship(
        @McpToolParam(description = "Model API name or ID") String modelApiNameOrId,
        @McpToolParam(description = "Relationship ID") String relationshipId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/semantic/models/" + ToolUtils.encodePath(modelApiNameOrId) + "/relationships/" + ToolUtils.encodePath(relationshipId), dataspace);
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
        description = "Get supported formula metadata for semantic models."
    )
    public String getFormulaMetadata(
        @McpToolParam(description = "Optional model API name or ID", required = false) String modelApiNameOrId
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
        description = "Get semantic model permissions."
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
        description = "Execute a semantic query against a semantic data model. The body must follow this structure: { semanticModelId, structuredSemanticQuery: { fields: [{expression, alias, rowGrouping?, semanticAggregationMethod?}, ...], options: {limitOptions: {limit: 10}} } }. For regular fields: use tableField with tableName (e.g. 'Norm_Geography' in table 'AccountRevenue'). For calculated dimensions/measurements: use semanticField with just name (e.g. {semanticField: {name: 'Cluster_Size_Tier'}}) — these are model-level, NOT on a specific table."
    )
    public String executeSemanticQuery(
        SdmSemanticQueryRequest request
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
