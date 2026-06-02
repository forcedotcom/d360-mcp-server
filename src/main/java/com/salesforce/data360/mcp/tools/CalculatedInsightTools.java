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
import com.salesforce.data360.mcp.model.request.calculatedinsight.CalculatedInsightCreateRequest;
import com.salesforce.data360.mcp.model.request.calculatedinsight.CalculatedInsightUpdateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Data 360 Calculated Insight Tools - CRUD and run operations for calculated insights.
 * Maps to /ssot/calculated-insights endpoints.
 *
 * CI SQL rules:
 *   - Columns must use table.column format: MyDmo__dlm.MyField__c
 *   - GROUP BY must use fully qualified names, not aliases
 *   - COUNT(DISTINCT ...) is NOT supported — use APPROX_COUNT_DISTINCT()
 *   - Subqueries / subquery aliases are NOT supported
 *   - CAST(... AS FLOAT) is NOT supported — compute raw counts, do division downstream
 *   - CURRENT_DATE - INTERVAL '30' DAY is NOT supported — no date arithmetic in CI SQL
 *   - Do NOT pass dimensions/measures arrays — the API auto-derives them from the expression
 */
@Component
public class CalculatedInsightTools {

    private final Data360Client client;

    public CalculatedInsightTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all Calculated Insights.
     * Returns a list of all CI definitions in the org.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights", verb = "GET")
    @McpTool(
        name = "d360_ci_list",
        description = "List all calculated insights."
    )
    public String listCalculatedInsights(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific Calculated Insight by name.
     * Returns the full CI definition including expression, dimensions, measures, and status.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/{ciName}", verb = "GET")
    @McpTool(
        name = "d360_ci_get",
        description = "Get a calculated insight by name."
    )
    public String getCalculatedInsight(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(ciName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new Calculated Insight.
     * Required fields: apiName (must end with __cio), displayName, definitionType, publishScheduleInterval, expression.
     * The API auto-derives dimensions/measures from the expression - do not include them.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights", verb = "POST")
    @McpTool(
        name = "d360_ci_create",
        description = "Create a calculated insight. Required body fields: apiName (must end with __cio), displayName, definitionType ('CALCULATED_METRIC'), publishScheduleInterval ('SYSTEM_MANAGED'), expression (CI SQL). CI SQL rules: columns must use table.column format (e.g. ssot__Individual__dlm.ssot__Id__c), GROUP BY must use fully qualified names (not aliases), COUNT(DISTINCT) is not supported (use APPROX_COUNT_DISTINCT), subqueries are not supported, CAST(... AS FLOAT) is not supported (compute raw counts instead), CURRENT_DATE - INTERVAL is not supported (no date arithmetic). Do NOT include dimensions/measures arrays — the API derives them from the expression."
    )
    public String createCalculatedInsight(
        CalculatedInsightCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            // Idempotent: check if CI already exists
            try {
                Map<String, Object> params = new HashMap<>();
                if (dataspace != null) params.put("dataspace", dataspace);

                String checkPath = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(request.getApiName()), params);
                Map existing = client.get(checkPath, Map.class);

                // If we get here, it exists - return it with a flag
                Map<String, Object> response = new HashMap<>(existing);
                response.put("_alreadyExisted", true);
                return JsonUtil.toJson(response);
            } catch (ApiException e) {
                // Does not exist (404), proceed with creation
                if (e.getStatusCode() != 404) {
                    throw e;
                }
            }

            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights", params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing Calculated Insight.
     * Supports partial updates via PATCH. Only include fields you want to change.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/{ciName}", verb = "PATCH")
    @McpTool(
        name = "d360_ci_update",
        description = "Update a calculated insight."
    )
    public String updateCalculatedInsight(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        CalculatedInsightUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(ciName), params);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a Calculated Insight.
     * Removes the CI definition and stops future runs. Does not delete historical data.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/{ciName}", verb = "DELETE")
    @McpTool(
        name = "d360_ci_delete",
        description = "Delete a calculated insight."
    )
    public String deleteCalculatedInsight(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(ciName), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Run a Calculated Insight manually.
     * Triggers an immediate execution of the CI. Returns a job/run ID to check status.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/{ciName}/actions/run", verb = "POST")
    @McpTool(
        name = "d360_ci_run",
        description = "Trigger a run of a calculated insight."
    )
    public String runCalculatedInsight(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(ciName) + "/actions/run", params);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get the run status of a Calculated Insight.
     * Returns the current execution status, progress, and completion info.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/{ciName}/actions/run", verb = "GET")
    @McpTool(
        name = "d360_ci_run_status",
        description = "Get the status of a CI run."
    )
    public String getCalculatedInsightRunStatus(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(ciName) + "/actions/run", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Query Calculated Insight results.
     * Returns the computed metrics with dimensions, measures, filters, and pagination.
     */
    @ApiEndpoint(path = "/ssot/insight/calculated-insights/{ciName}", verb = "GET")
    @McpTool(
        name = "d360_insights_query",
        description = "Query calculated insight data with dimensions, measures, filters, and time granularity."
    )
    public String queryInsights(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        @McpToolParam(description = "Comma-separated dimension names", required = false) String dimensions,
        @McpToolParam(description = "Comma-separated measure names", required = false) String measures,
        @McpToolParam(description = "Order by clause", required = false) String orderby,
        @McpToolParam(description = "Filters expression", required = false) String filters,
        @McpToolParam(description = "Batch size for pagination", required = false) Integer batchSize,
        @McpToolParam(description = "Offset for pagination", required = false) Integer offset,
        @McpToolParam(description = "Time granularity (e.g., 'DAY', 'MONTH')", required = false) String timeGranularity,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dimensions != null) params.put("dimensions", dimensions);
            if (measures != null) params.put("measures", measures);
            if (orderby != null) params.put("orderby", orderby);
            if (filters != null) params.put("filters", filters);
            if (batchSize != null) params.put("batchSize", batchSize);
            if (offset != null) params.put("offset", offset);
            if (timeGranularity != null) params.put("timeGranularity", timeGranularity);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/insight/calculated-insights/" + ToolUtils.encodePath(ciName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Enable a calculated insight.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/{ciName}/actions/enable", verb = "POST")
    @McpTool(
        name = "d360_ci_enable",
        description = "Enable a calculated insight."
    )
    public String enableCalculatedInsight(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(ciName) + "/actions/enable", params);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Disable a calculated insight.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/{ciName}/actions/disable", verb = "POST")
    @McpTool(
        name = "d360_ci_disable",
        description = "Disable a calculated insight."
    )
    public String disableCalculatedInsight(
        @McpToolParam(description = "The calculated insight API name") String ciName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/" + ToolUtils.encodePath(ciName) + "/actions/disable", params);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Validate a calculated insight.
     */
    @ApiEndpoint(path = "/ssot/calculated-insights/actions/validate", verb = "POST")
    @McpTool(
        name = "d360_ci_validate",
        description = "Validate a calculated insight."
    )
    public String validateCalculatedInsight(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/calculated-insights/actions/validate", params);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get metadata for Calculated Insights.
     * Lists all CIs or details for a specific one, including available dimensions and measures.
     */
    @ApiEndpoint(path = "/ssot/insight/metadata", verb = "GET")
    @McpTool(
        name = "d360_insights_metadata",
        description = "Get metadata for calculated insights. Lists all CIs or details for a specific one."
    )
    public String getInsightsMetadata(
        @McpToolParam(description = "Optional calculated insight API name for specific metadata", required = false) String ciName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            StringBuilder pathBuilder = new StringBuilder("/ssot/insight/metadata");
            if (ciName != null) {
                pathBuilder.append("/").append(ToolUtils.encodePath(ciName));
            }

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath(pathBuilder.toString(), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
