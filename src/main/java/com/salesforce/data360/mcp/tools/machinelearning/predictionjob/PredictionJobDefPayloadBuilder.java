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

import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefInputConfig;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionsConfigInput;

/**
 * Shared payload-construction helpers for the type-specific create tools.
 * Each type-specific tool exposes simplified parameters and uses these helpers
 * to materialize the full {@link PredictionJobDefCreateRequest} the API needs.
 */
final class PredictionJobDefPayloadBuilder {

    static final String DEFAULT_SCORING_MODE = "Batch";

    private PredictionJobDefPayloadBuilder() {}

    static PredictionsConfigInput outputConfig(String objectName, String objectLabel, String description) {
        PredictionsConfigInput out = new PredictionsConfigInput();
        out.setObjectName(objectName);
        out.setObjectLabel(objectLabel);
        out.setDescription(description);
        return out;
    }

    static PredictionJobDefCreateRequest baseRequest(String type, String apiName, String label,
                                                     String description, AssetReferenceInput model,
                                                     PredictionsConfigInput outputConfig,
                                                     String scoringMode) {
        PredictionJobDefCreateRequest req = new PredictionJobDefCreateRequest();
        req.setType(type);
        req.setApiName(apiName);
        req.setLabel(label != null ? label : apiName);
        req.setDescription(description);
        req.setModel(model);
        req.setOutputConfig(outputConfig);
        req.setScoringMode(scoringMode != null ? scoringMode : DEFAULT_SCORING_MODE);
        return req;
    }

    static PredictionJobDefInputConfig inputConfigBase(AssetReferenceInput dataObject) {
        PredictionJobDefInputConfig cfg = new PredictionJobDefInputConfig();
        cfg.setDataObject(dataObject);
        return cfg;
    }
}
