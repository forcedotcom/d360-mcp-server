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
package com.salesforce.data360.mcp.service;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryService {

    private final Data360Client client;

    public QueryService(Data360Client client) {
        this.client = client;
    }

    public String querySql(String sql, String dataspace, String workloadName,
                           Long rowLimit, Map<String, String> querySettings,
                           String sqlParameters, Integer adaptiveTimeout) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sql", sql);
            if (rowLimit != null) body.put("rowLimit", rowLimit);
            if (querySettings != null) body.put("querySettings", querySettings);
            if (sqlParameters != null) {
                body.put("sqlParameters", ToolUtils.parseJson(sqlParameters, List.class, "sqlParameters"));
            }
            if (adaptiveTimeout != null) body.put("adaptiveTimeout", adaptiveTimeout);

            String path = buildQueryPath("/ssot/query-sql", dataspace, workloadName, null);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String querySqlStatus(String queryId, String dataspace, String workloadName, Integer waitTimeMs) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (waitTimeMs != null) params.put("waitTimeMs", waitTimeMs);

            String path = buildQueryPath("/ssot/query-sql/" + ToolUtils.encodePath(queryId), dataspace, workloadName, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String querySqlRows(String queryId, Long offset, Long rowLimit,
                               Boolean omitSchema, String dataspace, String workloadName) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("offset", offset);
            if (rowLimit != null) params.put("rowLimit", rowLimit);
            if (omitSchema != null) params.put("omitSchema", omitSchema);

            String path = buildQueryPath("/ssot/query-sql/" + ToolUtils.encodePath(queryId) + "/rows", dataspace, workloadName, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String cancelQuerySql(String queryId, String dataspace, String workloadName) {
        try {
            String path = buildQueryPath("/ssot/query-sql/" + ToolUtils.encodePath(queryId), dataspace, workloadName, null);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String queryProfile(String dataModelName, String id, String childDataModelName,
                               String ciName, String searchKey, String fields,
                               Integer batchSize, String filters, Integer offset,
                               String orderby, String dataspace,
                               String dimensions, String measures, String timeGranularity) {
        try {
            StringBuilder pathBuilder = new StringBuilder("/ssot/profile/").append(ToolUtils.encodePath(dataModelName));

            if (id != null) {
                pathBuilder.append("/").append(ToolUtils.encodePath(id));
                if (ciName != null) {
                    pathBuilder.append("/calculated-insights/").append(ToolUtils.encodePath(ciName));
                } else if (childDataModelName != null) {
                    pathBuilder.append("/").append(ToolUtils.encodePath(childDataModelName));
                }
            }

            boolean isCollection = id == null;
            boolean isCalculatedInsight = id != null && ciName != null;
            boolean acceptsAggregationParams = isCollection || isCalculatedInsight;

            Map<String, Object> params = new LinkedHashMap<>();
            if (searchKey != null) params.put("searchKey", searchKey);
            if (fields != null) params.put("fields", fields);
            if (batchSize != null) params.put("batchSize", batchSize);
            if (filters != null) params.put("filters", filters);
            if (offset != null) params.put("offset", offset);
            if (orderby != null) params.put("orderby", orderby);
            if (dataspace != null) params.put("dataspace", dataspace);
            if (acceptsAggregationParams) {
                if (dimensions != null) params.put("dimensions", dimensions);
                if (measures != null) params.put("measures", measures);
                if (timeGranularity != null) params.put("timeGranularity", timeGranularity);
            }

            String path = ToolUtils.buildPath(pathBuilder.toString(), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String getProfileMetadata(String dataModelName, String dataspace) {
        try {
            StringBuilder pathBuilder = new StringBuilder("/ssot/profile/metadata");
            if (dataModelName != null) {
                pathBuilder.append("/").append(ToolUtils.encodePath(dataModelName));
            }

            Map<String, Object> params = new LinkedHashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath(pathBuilder.toString(), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String queryDataGraph(String dataGraphEntityName, String id,
                                 String dataspace, Boolean live,
                                 String projectedFields, String projectedFilters) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);
            if (live != null) params.put("live", live);
            if (projectedFields != null) params.put("projectedFields", projectedFields);
            if (projectedFilters != null) params.put("projectedFilters", projectedFilters);

            String path = ToolUtils.buildPath(
                "/ssot/data-graphs/data/" + ToolUtils.encodePath(dataGraphEntityName) + "/" + ToolUtils.encodePath(id),
                params
            );
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String lookupDataGraph(String dataGraphEntityName, String lookupKeys,
                                  String dataspace, Boolean noCache,
                                  String projectedFields, String projectedFilters) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("lookupKeys", lookupKeys);
            if (dataspace != null) params.put("dataspace", dataspace);
            if (noCache != null) params.put("noCache", noCache);
            if (projectedFields != null) params.put("projectedFields", projectedFields);
            if (projectedFilters != null) params.put("projectedFilters", projectedFilters);

            String path = ToolUtils.buildPath(
                "/ssot/data-graphs/data/" + ToolUtils.encodePath(dataGraphEntityName),
                params
            );
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    public String getDataGraphMetadata(String dataGraphEntityName, String dataspace) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (dataGraphEntityName != null) params.put("dataGraphEntityName", dataGraphEntityName);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-graphs/metadata", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    private String buildQueryPath(String basePath, String dataspace, String workloadName, Map<String, Object> additionalParams) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (dataspace != null) params.put("dataspace", dataspace);
        if (workloadName != null) params.put("workloadName", workloadName);
        if (additionalParams != null) params.putAll(additionalParams);
        return ToolUtils.buildPath(basePath, params);
    }
}
