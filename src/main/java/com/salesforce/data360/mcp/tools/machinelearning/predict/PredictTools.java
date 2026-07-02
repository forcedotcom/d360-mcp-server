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
package com.salesforce.data360.mcp.tools.machinelearning.predict;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.predict.PredictRequest;
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
 * Synchronous prediction tool — submits raw feature rows to a configured model
 * and returns per-row Success / Error results.
 */
@Component
public class PredictTools {

    private static final String BASE = "/ssot/machine-learning/predict";

    private final Data360Client client;

    public PredictTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = BASE, verb = "POST")
    @McpTool(
        name = "d360_ml_predict",
        description = "Run synchronous prediction against a configured model. "
            + "Targets a configured model by id or developer name. "
            + "Each call submits one or more raw feature rows; max 200 rows per call. fieldNames must match the configured model's input feature schema, "
            + "and every row's length must equal fieldNames.length. ALL row values are strings on the wire — numeric features must be string-encoded (\"25\", not 25). "
            + "Returns predictions[] with per-row results — each row carries status=Success (with predictedValue, factors[], prescriptions[]) or status=Error"
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String predict(
        @Valid @NotNull @McpToolParam(description = "Predict body — model (configured-model id or developer name), fieldNames, rows, optional settings.") PredictRequest request
    ) {
        try {
            Map result = client.post(BASE, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
