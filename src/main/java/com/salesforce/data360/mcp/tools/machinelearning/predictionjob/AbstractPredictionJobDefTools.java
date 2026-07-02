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
package com.salesforce.data360.mcp.tools.machinelearning.predictionjob;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefCreateRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;

import java.util.Map;

/**
 * Base class for prediction-job-definition tools. Holds the shared
 * {@link Data360Client} and the common create-prediction-job-def call used by
 * the generic tool ({@link PredictionJobDefTools}) and the type-specific tools
 * (Regression, Binary, Multiclass, Sentiment, Topic, Clustering).
 */
public abstract class AbstractPredictionJobDefTools {

    protected static final String BASE_PATH = "/ssot/machine-learning/prediction-job-definitions";

    protected final Data360Client client;

    protected AbstractPredictionJobDefTools(Data360Client client) {
        this.client = client;
    }

    /**
     * POST a {@link PredictionJobDefCreateRequest} to the create endpoint.
     */
    protected String createPredictionJobDef(PredictionJobDefCreateRequest request) {
        try {
            Map result = client.post(BASE_PATH, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    protected static String errorJson(String message) {
        return JsonUtil.toJson(Map.of("error", message));
    }
}
