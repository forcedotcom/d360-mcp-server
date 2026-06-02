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
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformCreateRequest;
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformPrepareRequest;
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformScheduleRequest;
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformUpdateRequest;
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformValidateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Data 360 Data Transform Tools - CRUD operations for data transformation jobs.
 * Maps to /ssot/data-transforms endpoints.
 */
@Component
public class DataTransformTools {

    // Constants for field names and validation
    private static final String VALIDATION_STATUS = "validationStatus";
    private static final String ISSUES = "issues";
    private static final String OUTPUT_DATA_OBJECTS = "outputDataObjects";
    private static final String FIELDS = "fields";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String LABEL = "label";
    private static final String CATEGORY = "category";
    private static final String MESSAGE = "message";
    private static final String PREPARED_PAYLOAD = "preparedPayload";
    private static final String SUGGESTIONS = "suggestions";

    // Constants for type values
    private static final String DATA_LAKE_OBJECT = "dataLakeObject";
    private static final String DATA_MODEL_OBJECT = "dataModelObject";
    private static final String PROFILE = "Profile";
    private static final String ENGAGEMENT = "Engagement";

    // Constants for pattern matching
    private static final String TIMESTAMP = "timestamp";
    private static final String DATETIME = "datetime";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String DLM_SUFFIX = "__dlm";
    private static final String DLL_SUFFIX = "__dll";
    private static final String DLO_SUFFIX = "__dlo";
    private static final String ID_FIELD = "Id__c";
    private static final String ID_FIELD_LOWER = "id__c";
    private static final String ID_SUFFIX = "_id__c";
    private static final String NODES = "nodes";
    private static final String COMPILED_CODE = "compiled_code";

    private final Data360Client client;

    public DataTransformTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all data transforms in the org.
     * Discover all transformation jobs.
     */
    @ApiEndpoint(path = "/ssot/data-transforms", verb = "GET")
    @McpTool(
        name = "d360_transform_list",
        description = "List all data transforms."
    )
    public String listDataTransforms(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific data transform by ID.
     * Returns transform definition, SQL, target DMO, and schedule.
     */
    @ApiEndpoint(path = "/ssot/data-transforms/{id}", verb = "GET")
    @McpTool(
        name = "d360_transform_get",
        description = "Get a data transform."
    )
    public String getDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Validate and prepare a data transform for creation.
     * Returns a complete payload with auto-detected primary key and category.
     */
    @ApiEndpoint(path = "/ssot/data-transforms-validation", verb = "POST")
    @McpTool(
        name = "d360_transform_prepare",
        description = "Validate and prepare a data transform for creation. This tool:\n" +
                "1. Validates the transform definition (DCSQL manifest or SQL expression)\n" +
                "2. Extracts output schema from validation response\n" +
                "3. Auto-detects primary key field and category\n" +
                "4. Returns a complete payload ready for creation\n\n" +
                "Use this BEFORE d360_transform_create to ensure your transform is valid and output schema is complete.\n\n" +
                "Supports both BATCH+DCSQL and STREAMING+SQL transforms."
    )
    public String prepareDataTransform(
        @McpToolParam(description = "Data transform preparation request body") DataTransformPrepareRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            // Step 1: Validate the transform definition
            Map<String, Object> validationBody = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms-validation", dataspace);
            Map<String, Object> validationResponse = client.post(path, validationBody, Map.class);

            // Step 2: Check for validation errors
            List<Object> issues = (List<Object>) validationResponse.get(ISSUES);
            if (issues != null && !issues.isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put(VALIDATION_STATUS, "invalid");
                errorResult.put("errors", issues);
                errorResult.put(MESSAGE, "Transform validation failed. Fix the errors and try again.");
                return JsonUtil.toJson(errorResult);
            }

            // Step 3: Extract output data objects from validation response
            Map<String, Object> outputDataObjectsMap = (Map<String, Object>) validationResponse.get(OUTPUT_DATA_OBJECTS);
            if (outputDataObjectsMap == null || outputDataObjectsMap.isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put(VALIDATION_STATUS, "invalid");
                errorResult.put(MESSAGE, "Validation did not return output schema. Please check your transform definition.");
                return JsonUtil.toJson(errorResult);
            }

            //you  Step 4: Determine output type (DLO vs DMO) based on input sources
            String outputType = determineOutputType(request, validationResponse);

            // Step 5: Extract fields and enrich with metadata
            List<Map<String, Object>> enrichedOutputDataObjects = new ArrayList<>();
            Map<String, String> suggestions = new HashMap<>();

            for (Map.Entry<String, Object> entry : outputDataObjectsMap.entrySet()) {
                String transformName = entry.getKey();
                List<Map<String, Object>> dloList = (List<Map<String, Object>>) entry.getValue();

                for (Map<String, Object> dlo : dloList) {
                    List<Map<String, Object>> fields = (List<Map<String, Object>>) dlo.get(FIELDS);
                    String dloName = (String) dlo.get(NAME);

                    if (fields != null && !fields.isEmpty()) {
                        // Auto-detect primary key
                        String primaryKeyField = detectPrimaryKeyField(fields);

                        // Auto-detect category (Profile vs Engagement)
                        String category = detectCategory(dloName, fields, request.getType());

                        suggestions.put("primaryKeyField_" + dloName, primaryKeyField);
                        suggestions.put("category_" + dloName, category);

                        // Enrich fields with isPrimaryKey flag
                        for (Map<String, Object> field : fields) {
                            String fieldName = (String) field.get(NAME);
                            field.put("isPrimaryKey", fieldName.equals(primaryKeyField));
                        }

                        // For Engagement category, detect and mark eventTimestamp field
                        if (ENGAGEMENT.equals(category)) {
                            String eventTimestampField = detectEventTimestampField(fields);
                            if (eventTimestampField != null) {
                                for (Map<String, Object> field : fields) {
                                    String fieldName = (String) field.get(NAME);
                                    if (fieldName.equals(eventTimestampField)) {
                                        field.put("isEventTimestamp", true);
                                    }
                                }
                                suggestions.put("eventTimestampField_" + dloName, eventTimestampField);
                            }
                        }

                        // Build enriched output data object
                        Map<String, Object> enrichedDlo = new HashMap<>();
                        enrichedDlo.put(NAME, dloName);
                        enrichedDlo.put(LABEL, dlo.getOrDefault(LABEL, dloName));
                        enrichedDlo.put(CATEGORY, category);
                        enrichedDlo.put(TYPE, outputType);
                        enrichedDlo.put("fields", fields);

                        enrichedOutputDataObjects.add(enrichedDlo);
                    }
                }
            }

            // Step 6: Build complete prepared payload
            Map<String, Object> preparedPayload = JsonUtil.toMap(request);
            Map<String, Object> definition = (Map<String, Object>) preparedPayload.get("definition");
            if (definition != null) {
                definition.put(OUTPUT_DATA_OBJECTS, enrichedOutputDataObjects);
            }

            // Step 7: Add validation for DMO transforms
            if (DATA_MODEL_OBJECT.equals(outputType) && (dataspace == null || dataspace.trim().isEmpty())) {
                suggestions.put("warning", "DMO transforms require a dataspace. Please provide dataspace parameter.");
            }

            // Step 8: Return result with suggestions
            Map<String, Object> result = new HashMap<>();
            result.put(VALIDATION_STATUS, "valid");
            result.put(PREPARED_PAYLOAD, preparedPayload);
            result.put(SUGGESTIONS, suggestions);
            result.put(MESSAGE, "Transform validated successfully. Review the suggested primary key and category. " +
                    "Modify preparedPayload if needed, then call d360_transform_create with this payload.");

            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new data transform with a complete, validated payload.
     * IMPORTANT: Call d360_transform_prepare FIRST to validate and generate output schema.
     */
    @ApiEndpoint(path = "/ssot/data-transforms", verb = "POST")
    @McpTool(
        name = "d360_transform_create",
        description = "Create a data transform with a complete, validated payload.\n\n" +
                "IMPORTANT: Call d360_transform_prepare FIRST to validate and generate output schema.\n" +
                "This tool expects a complete transform payload with outputDataObjects already populated.\n\n" +
                "Supports two types:\n" +
                "1. BATCH transforms with DCSQL definition - dbt-like multi-node workflows\n" +
                "2. STREAMING transforms with SQL definition - real-time SQL processing\n\n" +
                "The tool will:\n" +
                "- Validate that outputDataObjects are complete\n" +
                "- Create the transform\n" +
                "- Auto-create DLOs based on outputDataObjects\n\n" +
                "See payload examples for d360_transform_create_dcsql_batch and d360_transform_create_sql_streaming."
    )
    public String createDataTransform(
        @McpToolParam(description = "Data transform creation request body") DataTransformCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            // Create the transform - the API will validate completeness
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Auto-detect primary key field from a list of fields.
     * Looks for Id__c, *_id__c patterns, or returns first field as fallback.
     */
    private String detectPrimaryKeyField(List<Map<String, Object>> fields) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        // Look for fields named "Id__c" or "id__c" (case-insensitive)
        for (Map<String, Object> field : fields) {
            String name = (String) field.get(NAME);
            if (name != null && (name.equalsIgnoreCase(ID_FIELD) || name.equalsIgnoreCase(ID_FIELD_LOWER))) {
                return name;
            }
        }

        // Look for fields ending with "_id__c"
        for (Map<String, Object> field : fields) {
            String name = (String) field.get(NAME);
            if (name != null && name.toLowerCase().endsWith(ID_SUFFIX)) {
                return name;
            }
        }

        // Fallback: return first field
        return (String) fields.get(0).get(NAME);
    }

    /**
     * Detect category based on DLO name, fields, and transform type.
     * Returns "Engagement" for event-like data (daily append with timestamps),
     * "Profile" for entity data (upsert with primary keys).
     */
    private String detectCategory(String dloName, List<Map<String, Object>> fields, String transformType) {
        if (dloName == null) {
            return PROFILE;
        }

        String lowerName = dloName.toLowerCase();

        // Check for engagement/event patterns in name
        String[] engagementPatterns = {
            "event", "activity", "interaction", "engagement", "click", "view",
            "impression", "transaction", "session", "log", "history"
        };

        for (String pattern : engagementPatterns) {
            if (lowerName.contains(pattern)) {
                // Verify it has timestamp fields to confirm engagement pattern
                if (hasTimestampField(fields)) {
                    return ENGAGEMENT;
                }
            }
        }

        // Check for profile/entity patterns in name
        String[] profilePatterns = {
            "individual", "account", "contact", "customer", "user", "profile",
            "lead", "person", "member", "party"
        };

        for (String pattern : profilePatterns) {
            if (lowerName.contains(pattern)) {
                return PROFILE;
            }
        }

        // Default to Profile for entity-like data
        return PROFILE;
    }

    /**
     * Check if fields contain timestamp-like columns.
     */
    private boolean hasTimestampField(List<Map<String, Object>> fields) {
        if (fields == null || fields.isEmpty()) {
            return false;
        }

        for (Map<String, Object> field : fields) {
            String name = (String) field.get(NAME);
            String type = (String) field.get(TYPE);

            if (name != null) {
                String lowerName = name.toLowerCase();
                // Check for timestamp/date patterns in field name
                if (lowerName.contains(TIMESTAMP) || lowerName.contains(DATETIME) ||
                    lowerName.contains(DATE) || lowerName.contains(TIME) ||
                    lowerName.endsWith("_at") || lowerName.endsWith("_dt")) {
                    return true;
                }
            }

            // Check for timestamp/date field types
            if (type != null) {
                String lowerType = type.toLowerCase();
                if (lowerType.contains(TIMESTAMP) || lowerType.contains(DATETIME) ||
                    lowerType.contains(DATE)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Detect event timestamp field for Engagement category.
     * Looks for timestamp/datetime fields with event-related patterns.
     */
    private String detectEventTimestampField(List<Map<String, Object>> fields) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        // Priority 1: Look for explicit event timestamp patterns
        String[] eventPatterns = {"eventtimestamp", "event_timestamp", "eventtime", "event_time"};
        for (String pattern : eventPatterns) {
            for (Map<String, Object> field : fields) {
                String name = (String) field.get(NAME);
                if (name != null && name.toLowerCase().replace("__c", "").equals(pattern)) {
                    return name;
                }
            }
        }

        // Priority 2: Look for timestamp/datetime fields
        for (Map<String, Object> field : fields) {
            String name = (String) field.get(NAME);
            String type = (String) field.get(TYPE);

            if (name != null) {
                String lowerName = name.toLowerCase();
                // Common timestamp field patterns
                if (lowerName.contains(TIMESTAMP) || lowerName.contains(DATETIME) ||
                    lowerName.endsWith("_at") || lowerName.endsWith("_dt")) {
                    return name;
                }
            }

            // Check type
            if (type != null && (type.toLowerCase().contains(TIMESTAMP) ||
                                  type.toLowerCase().contains(DATETIME))) {
                return name;
            }
        }

        // Priority 3: Look for any date field
        for (Map<String, Object> field : fields) {
            String name = (String) field.get(NAME);
            String type = (String) field.get(TYPE);

            if ((name != null && name.toLowerCase().contains(DATE)) ||
                (type != null && type.toLowerCase().contains(DATE))) {
                return name;
            }
        }

        return null;
    }

    /**
     * Determine output type (dataLakeObject vs dataModelObject) based on input sources.
     * If all inputs are DMO, output is DMO. If all inputs are DLO, output is DLO.
     */
    private String determineOutputType(DataTransformPrepareRequest request, Map<String, Object> validationResponse) {
        try {
            // Check if definition has manifest (DCSQL) or expression (SQL)
            if (request.getDefinition() == null) {
                return DATA_LAKE_OBJECT;
            }

            Map<String, Object> manifest = request.getDefinition().getManifest();
            String expression = request.getDefinition().getExpression();

            // For SQL streaming, analyze the FROM clause
            if (expression != null && !expression.trim().isEmpty()) {
                return analyzeSourcesFromSQL(expression);
            }

            // For DCSQL, analyze manifest nodes
            if (manifest != null) {
                return analyzeSourcesFromManifest(manifest);
            }

        } catch (Exception e) {
            // Default to DLO on any parsing error - log for debugging
            // Exception intentionally caught to provide safe default
        }

        return DATA_LAKE_OBJECT;
    }

    /**
     * Analyze SQL expression to determine if sources are DLO or DMO.
     */
    private String analyzeSourcesFromSQL(String sql) {
        if (sql == null) {
            return DATA_LAKE_OBJECT;
        }

        String lowerSql = sql.toLowerCase();

        // Check for DMO patterns (ends with __dlm)
        if (lowerSql.contains(DLM_SUFFIX)) {
            // Check if there are any DLO sources (__dll or __dlo)
            if (lowerSql.contains(DLL_SUFFIX) || lowerSql.contains(DLO_SUFFIX)) {
                // Mixed sources - default to DLO
                return DATA_LAKE_OBJECT;
            }
            // All sources are DMO
            return DATA_MODEL_OBJECT;
        }

        // Default to DLO
        return DATA_LAKE_OBJECT;
    }

    /**
     * Analyze DCSQL manifest to determine if sources are DLO or DMO.
     */
    private String analyzeSourcesFromManifest(Map<String, Object> manifest) {
        try {
            Map<String, Object> nodes = (Map<String, Object>) manifest.get(NODES);
            if (nodes == null || nodes.isEmpty()) {
                return DATA_LAKE_OBJECT;
            }

            boolean hasDMO = false;
            boolean hasDLO = false;

            // Check all node SQL for source patterns
            for (Map.Entry<String, Object> nodeEntry : nodes.entrySet()) {
                Map<String, Object> node = (Map<String, Object>) nodeEntry.getValue();
                String compiledCode = (String) node.get(COMPILED_CODE);

                if (compiledCode != null) {
                    String lowerCode = compiledCode.toLowerCase();
                    if (lowerCode.contains(DLM_SUFFIX)) {
                        hasDMO = true;
                    }
                    if (lowerCode.contains(DLL_SUFFIX) || lowerCode.contains(DLO_SUFFIX)) {
                        hasDLO = true;
                    }
                }
            }

            // If all sources are DMO, output is DMO
            if (hasDMO && !hasDLO) {
                return DATA_MODEL_OBJECT;
            }

        } catch (Exception e) {
            // Ignore parsing errors - provide safe default
            // Exception intentionally caught to gracefully handle malformed manifest
        }

        return DATA_LAKE_OBJECT;
    }

    /**
     * Update an existing data transform.
     * Can update description, SQL, and other properties.
     */
    @ApiEndpoint(path = "/ssot/data-transforms/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_transform_update",
        description = "Update a data transform."
    )
    public String updateDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Data transform update request body") DataTransformUpdateRequest request,
        @McpToolParam(description = "Optional dataspace query parameter", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a data transform.
     * Deletes transform and stops any scheduled runs.
     */
    @ApiEndpoint(path = "/ssot/data-transforms/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_transform_delete",
        description = "Delete a data transform."
    )
    public String deleteDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Run a data transform manually.
     * Manually trigger transform execution outside normal schedule.
     */
    @ApiEndpoint(path = "/ssot/data-transforms/{id}/actions/run", verb = "POST")
    @McpTool(
        name = "d360_transform_run",
        description = "Run a data transform."
    )
    public String runDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId) + "/actions/run", dataspace);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Quick SQL validation for streaming transforms.
     * For batch transforms or full validation with schema enrichment, use d360_transform_prepare instead.
     */
    @ApiEndpoint(path = "/ssot/data-transforms-validation", verb = "POST")
    @McpTool(
        name = "d360_transform_validate",
        description = "Quick SQL validation. Pass raw SQL string to check syntax.\n" +
                "Use case: Quick validation of streaming SQL expressions.\n" +
                "For full transform validation with schema enrichment, use d360_transform_prepare."
    )
    public String validateDataTransform(
        @McpToolParam(description = "Data transform validation request body with SQL") DataTransformValidateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms-validation", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get the schedule for a data transform.
     * Check when transform runs (frequency, time, day of week).
     */
    @ApiEndpoint(path = "/ssot/data-transforms/{id}/schedule", verb = "GET")
    @McpTool(
        name = "d360_transform_schedule_get",
        description = "Get transform schedule."
    )
    public String getDataTransformSchedule(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId) + "/schedule", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Set or update the schedule for a data transform.
     * Configure when transform runs (frequency: DAILY, WEEKLY, MONTHLY).
     */
    @ApiEndpoint(path = "/ssot/data-transforms/{id}/schedule", verb = "PUT")
    @McpTool(
        name = "d360_transform_schedule_set",
        description = "Set transform schedule."
    )
    public String setDataTransformSchedule(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Data transform schedule request body") DataTransformScheduleRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId) + "/schedule", dataspace);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
