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
import com.salesforce.data360.mcp.model.request.segment.SegmentCreateRequest;
import com.salesforce.data360.mcp.model.request.segment.SegmentUpdateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 Segment Tools - segment management and publishing.
 * Maps to /ssot/segments endpoints.
 */
@Component
public class SegmentTools {

    private final Data360Client client;

    public SegmentTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all segments in the organization.
     */
    @ApiEndpoint(path = "/ssot/segments", verb = "GET")
    @McpTool(
        name = "d360_segment_list",
        description = "List all segments."
    )
    public String listSegments(
        @McpToolParam(description = "Dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Maximum number of records to return", required = false) Integer batchSize,
        @McpToolParam(description = "Filter expression to narrow results", required = false) String filters,
        @McpToolParam(description = "Number of records to skip for pagination", required = false) Integer offset,
        @McpToolParam(description = "Field to order results by", required = false) String orderBy
    ) {
        try {
            Map<String, Object> params = new java.util.LinkedHashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);
            if (batchSize != null) params.put("batchSize", batchSize);
            if (filters != null) params.put("filters", filters);
            if (offset != null) params.put("offset", offset);
            if (orderBy != null) params.put("orderBy", orderBy);
            String path = ToolUtils.buildPath("/ssot/segments", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a segment by record ID or API name.
     * The additional filter params are only applicable when using an API name.
     */
    @ApiEndpoint(path = "/ssot/segments/{id}", verb = "GET")
    @McpTool(
        name = "d360_segment_get",
        description = "Get a segment by record ID or API name. "
            + "Pass the 18-char record ID (1sg prefix) or the segment API name. "
            + "The filter parameters (batchSize, filters, offset, orderBy, dataspace) are only applicable when using an API name — they have no effect when passing a record ID."
    )
    public String getSegment(
        @McpToolParam(description = "The segment record ID (18-char, 1sg prefix) or API name") String segmentIdOrApiName,
        @McpToolParam(description = "Dataspace name (only applicable when using API name)", required = false) String dataspace,
        @McpToolParam(description = "Maximum number of records to return (only applicable when using API name)", required = false) Integer batchSize,
        @McpToolParam(description = "Filter expression to narrow results (only applicable when using API name)", required = false) String filters,
        @McpToolParam(description = "Number of records to skip for pagination (only applicable when using API name)", required = false) Integer offset,
        @McpToolParam(description = "Field to order results by (only applicable when using API name)", required = false) String orderBy
    ) {
        try {
            Map<String, Object> params = new java.util.LinkedHashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);
            if (batchSize != null) params.put("batchSize", batchSize);
            if (filters != null) params.put("filters", filters);
            if (offset != null) params.put("offset", offset);
            if (orderBy != null) params.put("orderBy", orderBy);
            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentIdOrApiName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new segment.
     * Requires displayName, segmentOnApiName, segmentType, and other configuration.
     */
    @ApiEndpoint(path = "/ssot/segments", verb = "POST")
    @McpTool(
        name = "d360_segment_create",
        description = "Create a new segment. DEPENDENCY: All CIs referenced in segment filters must be in ACTIVE status before creation. " +
            "For SQL-defined segments use segmentType 'Dbt' with this shape: { displayName, segmentOnApiName, segmentType: 'Dbt', publishSchedule, includeDbt.models.models[].{name, sql} }. " +
            "Do NOT set segmentCreationFlow for Dbt segments. SQL goes in includeDbt.models.models[].sql (note the doubly-nested models wrapper). " +
            "publishSchedule values: NoRefresh, One, Two, Four, Six, Twelve, TwentyFour. " +
            "dataSpace is a query param (the dataspace tool parameter), NOT a body field. " +
            "Dbt SQL validation rules: " +
            "(1) tables and columns must exist in metadata for the dataspace; " +
            "(2) primary SELECT items must be unaliased, fully-qualified column references like 'Individuals__dlm.Col__c'. Table aliases (FROM Individuals__dlm i / FROM Individuals__dlm AS i) are NOT supported on the create endpoint and are rejected with 'Need api name of DMO'; always use fully-qualified DMO names in SELECT, ON, and WHERE clauses. DISTINCT and ORDER BY are allowed; column aliases (col AS x), CASE, function calls, and arithmetic in the primary SELECT list are NOT (these are fine inside WHERE / HAVING / GROUP BY); " +
            "(3) project the primary key of segmentOnApiName AND its key qualifier when one is configured in metadata (e.g. for Individuals__dlm project both IndividualId__c and KQ_IndividualId__c) — omitting the KQ fails with 'You must project a key qualifier along with the primary key'; " +
            "(4) join ON-clause must be a single bare EQUALS between two qualified identifiers on the primary key (e.g. 'Individuals__dlm.IndividualId__c = SalesOrders__dlm.IndividualId__c'). NO additional ANDed predicates in ON. Filter predicates, range/threshold checks, status filters, and date scoping cannot live in ON — they MUST go in WHERE (note: this changes LEFT JOIN semantics; see ANTI-MATCH pattern). 'IS NOT DISTINCT FROM' is rejected on this endpoint with 'Invalid SQL. Please provide a valid SQL.'. Compound foreign keys (two EQUALS ANDed) in ON are rejected with 'Incorrect join condition structure: Only use equality based comparisons comparing the two join keys directly'; " +
            "(5) join keys must correspond to a relationship defined in the DMO graph; references in a multi-join chain are limited to the last 2 tables; tables cannot repeat; CROSS / COMMA / NATURAL / LEFT_SEMI / LEFT_ANTI joins are not supported; ON clause is required (USING is not); " +
            "(6) only a fixed set of SQL constructs is allowed. EXISTS, NOT EXISTS, EXCEPT, MINUS, LATERAL, and bare correlated subqueries are NOT supported. UNION and INTERSECT are allowed but subject to structural rules. CTEs (WITH) are subject to structural rules too; " +
            "(7) FROM must contain DMO identifiers only — derived tables like 'JOIN (SELECT ...) sub ON ...' are rejected with 'Need api name of DMO'; " +
            "(8) CAST is restricted to varchar/int/decimal/date/timestamp/boolean, and varchar requires an explicit precision (e.g. cast(x as varchar(100))); " +
            "(9) subqueries are not allowed — they must be modelled as joins; " +
            "(10) generated dialect must conform to ANSI SQL standards. " +
            "ENGINE-PARITY GUARDS in INNER joins: ALWAYS add three guards to WHERE that reproduce the engine's defensive PK+KQ join semantics, since rule 4 forbids compound EQUALS in ON. " +
            "(a) NULL-safe KQ equality 'kq1 = kq2 OR (kq1 IS NULL AND kq2 IS NULL)' — textbook expansion of NULL-safe equality, accepted because boolean composition in WHERE is unrestricted, reproduces PK+KQ join semantics regardless of whether KQ is nullable or multi-valued. " +
            "(b) Empty-string PK guard 'trim(cast(t1.PK as varchar(255))) <> '' ' on the projected PK of segmentOnApiName — filters out blank PKs the engine treats as missing. Apply to the LEFT-side PK in joins and to the PK in no-join queries too. " +
            "(c) IS NOT NULL on every filter field before value comparisons (e.g. 'AND t2.Amount IS NOT NULL AND t2.Amount > 5000') and before any aggregate input (place the IS NOT NULL in WHERE, then GROUP BY / HAVING SUM(...) on the same field). " +
            "Default INNER-join shape: '... ON Individuals__dlm.IndividualId__c = SalesOrders__dlm.IndividualId__c WHERE (Individuals__dlm.KQ_IndividualId__c = SalesOrders__dlm.KQ_IndividualId__c OR (Individuals__dlm.KQ_IndividualId__c IS NULL AND SalesOrders__dlm.KQ_IndividualId__c IS NULL)) AND trim(cast(Individuals__dlm.IndividualId__c as varchar(255))) <> '' AND SalesOrders__dlm.TotalAmount__c IS NOT NULL AND <other filters on TotalAmount__c>'. " +
            "EXCEPTION — LEFT JOIN / anti-match: do NOT add KQ-parity, empty-PK trim, or IS NOT NULL clauses to WHERE for LEFT JOINs. Any predicate referencing the right side in WHERE collapses the LEFT JOIN to INNER and breaks 'no related row' semantics. The IS NOT NULL guard for the filter field belongs INSIDE the CASE expression in HAVING SUM(CASE WHEN ...) = 0, not in WHERE; the KQ-parity and trim guards are unneeded here because non-matching rows simply contribute 0 to SUM and are correctly kept. " +
            "Canonical patterns (always use fully-qualified DMO names — no aliases; INNER joins always include all three engine-parity WHERE guards): " +
            "SIMPLE attribute equality (no join, e.g. FirstName = 'Robot'): SELECT Individuals__dlm.IndividualId__c, Individuals__dlm.KQ_IndividualId__c FROM Individuals__dlm WHERE Individuals__dlm.FirstName__c = 'Robot' AND trim(cast(Individuals__dlm.IndividualId__c as varchar(255))) <> ''. " +
            "ANY-MATCH 'at least one related row matching P' (e.g. individuals with at least one order > 5000): SELECT DISTINCT Individuals__dlm.IndividualId__c, Individuals__dlm.KQ_IndividualId__c FROM Individuals__dlm JOIN SalesOrders__dlm ON Individuals__dlm.IndividualId__c = SalesOrders__dlm.IndividualId__c WHERE (Individuals__dlm.KQ_IndividualId__c = SalesOrders__dlm.KQ_IndividualId__c OR (Individuals__dlm.KQ_IndividualId__c IS NULL AND SalesOrders__dlm.KQ_IndividualId__c IS NULL)) AND trim(cast(Individuals__dlm.IndividualId__c as varchar(255))) <> '' AND SalesOrders__dlm.TotalAmount__c IS NOT NULL AND SalesOrders__dlm.TotalAmount__c > 5000. " +
            "AGGREGATE 'SUM/COUNT/AVG over related rows passes threshold' (e.g. SUM(amount) > 1000, or COUNT of qualifying orders > 1): SELECT Individuals__dlm.IndividualId__c, Individuals__dlm.KQ_IndividualId__c FROM Individuals__dlm JOIN SalesOrders__dlm ON Individuals__dlm.IndividualId__c = SalesOrders__dlm.IndividualId__c WHERE (Individuals__dlm.KQ_IndividualId__c = SalesOrders__dlm.KQ_IndividualId__c OR (Individuals__dlm.KQ_IndividualId__c IS NULL AND SalesOrders__dlm.KQ_IndividualId__c IS NULL)) AND trim(cast(Individuals__dlm.IndividualId__c as varchar(255))) <> '' AND SalesOrders__dlm.TotalAmount__c IS NOT NULL GROUP BY Individuals__dlm.IndividualId__c, Individuals__dlm.KQ_IndividualId__c HAVING SUM(SalesOrders__dlm.TotalAmount__c) > 1000. For 'count of orders > 10000 is at least 2', extend WHERE with AND SalesOrders__dlm.TotalAmount__c > 10000 and HAVING COUNT(SalesOrders__dlm.SalesOrderId__c) > 1. " +
            "ANTI-MATCH 'no related row matching P' (e.g. no order over 10000): cannot be expressed with the natural 'LEFT JOIN ... ON pk=fk AND P WHERE ... IS NULL' shape because of rule 4. Use HAVING SUM(CASE)=0 instead. ENGINE-PARITY DIVERGENCE: do NOT add KQ-parity, empty-PK trim, or IS NOT NULL clauses to WHERE here; predicates referencing the right side in WHERE collapse the LEFT JOIN to INNER and drop individuals with zero matches. The IS NOT NULL guard for the filter field belongs INSIDE the CASE expression: SELECT Individuals__dlm.IndividualId__c, Individuals__dlm.KQ_IndividualId__c FROM Individuals__dlm LEFT JOIN SalesOrders__dlm ON Individuals__dlm.IndividualId__c = SalesOrders__dlm.IndividualId__c GROUP BY Individuals__dlm.IndividualId__c, Individuals__dlm.KQ_IndividualId__c HAVING SUM(CASE WHEN SalesOrders__dlm.TotalAmount__c IS NOT NULL AND SalesOrders__dlm.TotalAmount__c > 10000 THEN 1 ELSE 0 END) = 0. LEFT JOIN preserves individuals with zero orders (CASE over the synthesized NULL row evaluates to 0, SUM=0, individual kept); any matching big order forces SUM>0 and the individual is dropped. " +
            "Common rejection messages and which rule they map to: " +
            "'Invalid SQL. Please provide a valid SQL.' — unrecognized syntax such as IS NOT DISTINCT FROM or LEFT ANTI JOIN (rules 4 / 5); " +
            "'Incorrect join condition structure: Only use equality based comparisons comparing the two join keys directly' — extra predicate or compound EQUALS in ON-clause (rule 4); " +
            "'Sql kind: \\'EXISTS\\' not supported' (also EXCEPT, MINUS, etc.) — disallowed SQL construct (rule 6); " +
            "'Need api name of DMO' — table alias in FROM ('FROM Individuals__dlm i', rule 2) OR derived table / subquery in FROM (rule 7); " +
            "'You must project a key qualifier along with the primary key' — missing KQ in primary SELECT (rule 3); " +
            "'No relationship exists between the provided keys' — join keys aren't a declared DMO relationship (rule 5); " +
            "'Primary select should only contain unaliased fully qualified identifiers' — CASE / function / aliased column in primary SELECT (rule 2). " +
            "Other segment types (Dynamic, Lookalike, Realtimez, Waterfall, EinsteinGptSegmentsUI) use their own criteria fields, not includeDbt."
    )
    public String createSegment(
        @McpToolParam(description = "Request body for creating segment") SegmentCreateRequest request,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = ToolUtils.buildPath("/ssot/segments", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing segment.
     * Can update displayName, publishSchedule, and other metadata.
     */
    @ApiEndpoint(path = "/ssot/segments/{segmentApiName}", verb = "PATCH")
    @McpTool(
        name = "d360_segment_update",
        description = "Update a segment."
    )
    public String updateSegment(
        @McpToolParam(description = "The segment API name to update") String segmentApiName,
        @McpToolParam(description = "Request body for updating segment") SegmentUpdateRequest request,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentApiName), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a segment by API name.
     * The path requires the segment API name (developer name), not the ID.
     * Irreversible.
     */
    @ApiEndpoint(path = "/ssot/segments/{segmentApiName}", verb = "DELETE")
    @McpTool(
        name = "d360_segment_delete",
        description = "Delete a segment by API name (developer name). Irreversible."
    )
    public String deleteSegment(
        @McpToolParam(description = "The segment API name (developer name)") String segmentApiName,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentApiName), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Publish/activate a segment.
     * Triggers segment calculation and activation.
     */
    @ApiEndpoint(path = "/ssot/segments/{id}/actions/publish", verb = "POST")
    @McpTool(
        name = "d360_segment_publish",
        description = "Publish/activate a segment."
    )
    public String publishSegment(
        @McpToolParam(description = "The segment ID to publish") String segmentId
    ) {
        try {
            String path = "/ssot/segments/" + ToolUtils.encodePath(segmentId) + "/actions/publish";
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Deactivate a segment by API name or Id. Inverse of publish.
     */
    @ApiEndpoint(path = "/ssot/segments/{apiName}/actions/deactivate", verb = "POST")
    @McpTool(
        name = "d360_segment_deactivate",
        description = "Deactivate a segment by API name (developer name) or ID. Inverse of publish."
    )
    public String deactivateSegment(
        @McpToolParam(description = "The segment ID or API name (developer name)") String segmentApiNameOrId
    ) {
        try {
            String path = "/ssot/segments/" + ToolUtils.encodePath(segmentApiNameOrId) + "/actions/deactivate";
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
