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
package com.salesforce.data360.mcp.tools.machinelearning.setup;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.DataProfileRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.OutcomeRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.RowCountRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.SetupFieldsRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Setup-helper query tools for ML model authoring.
 */
@Component
public class ModelSetupHelperTools {

    private static final String BASE = "/ssot/machine-learning";

    private final Data360Client client;

    public ModelSetupHelperTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = BASE + "/query-setup-fields", verb = "POST")
    @McpTool(
        name = "d360_ml_query_setup_fields",
        description = "Enumerate fields and applicable transformations for a data source. "
            + "First call when authoring a model setup — the response describes which fields are available "
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String querySetupFields(
        @Valid @NotNull
        @McpToolParam(description = "Setup-fields query payload. Set 'input' to the data source; optional 'filter', 'join', 'filterFields', 'relatedFieldNames'.")
        SetupFieldsRequest request
    ) {
        try {
            Map result = client.post(BASE + "/query-setup-fields", JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/query-data-profile", verb = "POST")
    @McpTool(
        name = "d360_ml_query_data_profile",
        description = "Profile values on a data source. For numeric fields returns min/max plus the bucketing strategy used; "
            + "for categorical/text fields returns the distinct value list with per-value row counts. "
            + "Use to assess feature usability — spot constant-valued numerics, gauge class balance on categoricals, find dominant categories. "
            + "Pair with d360_ml_query_setup_fields: setup-fields lists what is available; data-profile inspects specific values."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String queryDataProfile(
        @Valid @NotNull
        @McpToolParam(description = "Data-profile query payload. Set 'input' to the data source; optional 'profileFieldNames' (typed list — omit to profile all eligible fields), 'filter', 'join'.")
        DataProfileRequest request
    ) {
        try {
            Map result = client.post(BASE + "/query-data-profile", JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/query-outcome", verb = "POST")
    @McpTool(
        name = "d360_ml_query_outcome",
        description = "List candidate outcome columns (Binary, Discrete, Continuous) in the data source. "
            + "Optional 'predictionType' narrows results to a single prediction type "
            + "(Regression, BinaryClassification, MulticlassClassification). "
            + "Use the response to pick a target field for training."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String queryOutcome(
        @Valid @NotNull
        @McpToolParam(description = "Outcome query payload. Set 'input' to the data source; optional 'predictionType', 'filter', 'join', 'relatedFieldNames'.")
        OutcomeRequest request
    ) {
        try {
            Map result = client.post(BASE + "/query-outcome", JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/query-row-count", verb = "POST")
    @McpTool(
        name = "d360_ml_query_row_count",
        description = "Row count for the data source after filters and joins are applied. "
            + "Use to gate whether there is enough data to train — target ~a few thousand rows for reasonable model quality."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String queryRowCount(
        @Valid @NotNull
        @McpToolParam(description = "Row-count query payload. Set 'input' to the data source; optional 'filter', 'join'.")
        RowCountRequest request
    ) {
        try {
            Map result = client.post(BASE + "/query-row-count", JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
