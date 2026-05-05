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

import com.salesforce.data360.mcp.service.QueryService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueryTools {

    private final QueryService queryService;

    public QueryTools(QueryService queryService) {
        this.queryService = queryService;
    }

    @McpTool(
        name = "d360_query_sql",
        description = "Execute a Data 360 SQL query (V3 API - preferred). Returns metadata, status, and first chunk of data. Use d360_query_sql_rows to paginate."
    )
    public String querySql(
        @McpToolParam(description = "The DC SQL query to execute") String sql,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Optional workload name", required = false) String workloadName,
        @McpToolParam(description = "Max rows to return in first response", required = false) Integer rowLimit,
        @McpToolParam(description = "Key-value map of query settings", required = false) Map<String, String> querySettings,
        @McpToolParam(description = "JSON string of parameterized query values (array)", required = false) String sqlParameters,
        @McpToolParam(description = "Adaptive timeout in milliseconds", required = false) Integer adaptiveTimeout
    ) {
        return queryService.querySql(sql, dataspace, workloadName, rowLimit, querySettings, sqlParameters, adaptiveTimeout);
    }

    @McpTool(
        name = "d360_query_sql_status",
        description = "Get the status of a running/completed query by queryId. Returns completionStatus, rowCount, chunkCount, progress."
    )
    public String querySqlStatus(
        @McpToolParam(description = "The query ID to check") String queryId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Optional workload name", required = false) String workloadName,
        @McpToolParam(description = "Wait time in milliseconds for long-polling", required = false) Integer waitTimeMs
    ) {
        return queryService.querySqlStatus(queryId, dataspace, workloadName, waitTimeMs);
    }

    @McpTool(
        name = "d360_query_sql_rows",
        description = "Fetch paginated rows from a completed query. Use offset and rowLimit for pagination."
    )
    public String querySqlRows(
        @McpToolParam(description = "The query ID to fetch rows from") String queryId,
        @McpToolParam(description = "Starting offset for pagination") Integer offset,
        @McpToolParam(description = "Max rows to return", required = false) Integer rowLimit,
        @McpToolParam(description = "Omit schema in response", required = false) Boolean omitSchema,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Optional workload name", required = false) String workloadName
    ) {
        return queryService.querySqlRows(queryId, offset, rowLimit, omitSchema, dataspace, workloadName);
    }

    @McpTool(
        name = "d360_query_sql_cancel",
        description = "Cancel a running query and release resources."
    )
    public String cancelQuerySql(
        @McpToolParam(description = "The query ID to cancel") String queryId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Optional workload name", required = false) String workloadName
    ) {
        return queryService.cancelQuerySql(queryId, dataspace, workloadName);
    }

    @McpTool(
        name = "d360_profile_query",
        description = "Query unified profile data. Supports searching by model name, ID, child models, and calculated insights on profiles."
    )
    public String queryProfile(
        @McpToolParam(description = "The data model name (e.g., Individual, Account)") String dataModelName,
        @McpToolParam(description = "Optional profile ID", required = false) String id,
        @McpToolParam(description = "Optional child data model name", required = false) String childDataModelName,
        @McpToolParam(description = "Optional calculated insight name", required = false) String ciName,
        @McpToolParam(description = "Search key for filtering", required = false) String searchKey,
        @McpToolParam(description = "Comma-separated field list", required = false) String fields,
        @McpToolParam(description = "Batch size for pagination", required = false) Integer batchSize,
        @McpToolParam(description = "Filters expression", required = false) String filters,
        @McpToolParam(description = "Offset for pagination", required = false) Integer offset,
        @McpToolParam(description = "Order by clause", required = false) String orderby,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        return queryService.queryProfile(dataModelName, id, childDataModelName, ciName, searchKey, fields, batchSize, filters, offset, orderby, dataspace);
    }

    @McpTool(
        name = "d360_profile_metadata",
        description = "Get metadata for profile (unified) data model objects. Omit dataModelName to list all profile models."
    )
    public String getProfileMetadata(
        @McpToolParam(description = "Optional data model name to get specific profile metadata", required = false) String dataModelName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        return queryService.getProfileMetadata(dataModelName, dataspace);
    }

    @McpTool(
        name = "d360_datagraph_query",
        description = "Query a specific data graph record by entity name and ID."
    )
    public String queryDataGraph(
        @McpToolParam(description = "The data graph entity name") String dataGraphEntityName,
        @McpToolParam(description = "The record ID") String id,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Set true for real-time data", required = false) Boolean live
    ) {
        return queryService.queryDataGraph(dataGraphEntityName, id, dataspace, live);
    }

    @McpTool(
        name = "d360_datagraph_lookup",
        description = "Lookup data graph records by lookup keys."
    )
    public String lookupDataGraph(
        @McpToolParam(description = "The data graph entity name") String dataGraphEntityName,
        @McpToolParam(description = "Lookup keys (comma-separated)") String lookupKeys,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Bypass cache", required = false) Boolean noCache
    ) {
        return queryService.lookupDataGraph(dataGraphEntityName, lookupKeys, dataspace, noCache);
    }

    @McpTool(
        name = "d360_datagraph_metadata",
        description = "Get metadata for data graphs."
    )
    public String getDataGraphMetadata(
        @McpToolParam(description = "Optional data graph entity name for specific metadata", required = false) String dataGraphEntityName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        return queryService.getDataGraphMetadata(dataGraphEntityName, dataspace);
    }
}
