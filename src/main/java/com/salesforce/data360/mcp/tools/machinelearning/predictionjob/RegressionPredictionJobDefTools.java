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
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.FeatureMappingInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefInputConfig;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefSettingsInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionsConfigInput;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Type-specific create tool for Regression prediction job definitions.
 * Pre-shapes the payload (type, model ref, input/output configs).
 */
@Component
public class RegressionPredictionJobDefTools extends AbstractPredictionJobDefTools {

    private static final String TYPE = "Regression";

    public RegressionPredictionJobDefTools(Data360Client client) {
        super(client);
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions", verb = "POST")
    @McpTool(
        name = "d360_prediction_job_def_create_regression",
        description = "Create a Regression prediction job definition. "
            + "Required: apiName, model, inputDataObject, fields (model-feature ↔ DMO-field mappings), outputObjectName. "
            + "regressionSettings ranges enforced server-side: maxTopContributors 0-3, maxPrescriptions 0-3, prescriptionThreshold 0-100. "
            + "scoringMode defaults to 'Batch'."
    )
    public String createRegressionPredictionJobDef(
        @McpToolParam(description = "api name for the prediction job definition.") String apiName,
        @McpToolParam(description = "Reference to a regression-trained configured model. Provide either id or name.") AssetReferenceInput model,
        @McpToolParam(description = "Reference to the input DMO whose rows will be scored. Provide either id or name.") AssetReferenceInput inputDataObject,
        @McpToolParam(description = "Mappings of model features to input-DMO fields.") List<FeatureMappingInput> fields,
        @McpToolParam(description = "Api name for the output DMO that will hold predictions.") String outputObjectName,
        @McpToolParam(description = "Display label for the job (defaults to apiName).", required = false) String label,
        @McpToolParam(description = "Free-form description.", required = false) String description,
        @McpToolParam(description = "Display label for the output DMO.", required = false) String outputObjectLabel,
        @McpToolParam(description = "Description for the output DMO.", required = false) String outputDescription,
        @McpToolParam(description = "Scoring mode (Batch, Streaming). Default Batch.", required = false) String scoringMode,
        @McpToolParam(description = "Regression settings: maxTopContributors 0-3, maxPrescriptions 0-3, prescriptionThreshold 0-100.", required = false) PredictionJobDefSettingsInput regressionSettings
    ) {
        if (fields == null || fields.isEmpty()) {
            return errorJson("fields is required for Regression jobs (mappings of model features to input-DMO fields).");
        }

        PredictionsConfigInput output = PredictionJobDefPayloadBuilder.outputConfig(outputObjectName, outputObjectLabel, outputDescription);
        PredictionJobDefCreateRequest req = PredictionJobDefPayloadBuilder.baseRequest(
                TYPE, apiName, label, description, model, output, scoringMode);

        PredictionJobDefInputConfig inputConfig = PredictionJobDefPayloadBuilder.inputConfigBase(inputDataObject);
        inputConfig.setFields(fields);
        req.setInputConfig(inputConfig);

        if (regressionSettings != null) {
            req.setRegressionSettings(regressionSettings);
        }

        return createPredictionJobDef(req);
    }
}
