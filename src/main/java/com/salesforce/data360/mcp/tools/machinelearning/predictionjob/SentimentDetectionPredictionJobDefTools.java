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
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.InputFieldInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefInputConfig;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionsConfigInput;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SentimentDetectionPredictionJobDefTools extends AbstractPredictionJobDefTools {

    private static final String TYPE = "SentimentDetection";
    private static final String DEFAULT_MODEL_NAME = "sfdc_ai__DefaultSalesforceSentimentAnalysis";

    public SentimentDetectionPredictionJobDefTools(Data360Client client) {
        super(client);
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions", verb = "POST")
    @McpTool(
        name = "d360_prediction_job_def_create_sentiment_detection",
        description = "Create a SentimentDetection prediction job definition. "
            + "Required: apiName, inputDataObject, textFields (1-3 entries), outputObjectName. "
            + "model defaults to a reference by name to '" + DEFAULT_MODEL_NAME + "' (Salesforce-provided sentiment model) when omitted. "
            + "scoringMode defaults to 'Batch'."
    )
    public String createSentimentDetectionPredictionJobDef(
        @McpToolParam(description = "api name for the prediction job definition.") String apiName,
        @McpToolParam(description = "Reference to the input DMO whose rows will be scored. Provide either id or name.") AssetReferenceInput inputDataObject,
        @McpToolParam(description = "Up to 3 text fields from the input DMO to score for sentiment. Each entry: { mappedField, updateScore? }.") List<InputFieldInput> textFields,
        @McpToolParam(description = "Api name for the output DMO that will hold sentiment results.") String outputObjectName,
        @McpToolParam(description = "Reference to the configured model. Defaults to a reference by name to '" + DEFAULT_MODEL_NAME + "' when omitted.", required = false) AssetReferenceInput model,
        @McpToolParam(description = "Display label (defaults to apiName).", required = false) String label,
        @McpToolParam(description = "Free-form description.", required = false) String description,
        @McpToolParam(description = "Display label for the output DMO.", required = false) String outputObjectLabel,
        @McpToolParam(description = "Description for the output DMO.", required = false) String outputDescription,
        @McpToolParam(description = "Scoring mode (Batch, Streaming). Default Batch.", required = false) String scoringMode
    ) {
        if (textFields == null || textFields.isEmpty()) {
            return errorJson("textFields is required for SentimentDetection jobs (one or more text fields from the input DMO).");
        }

        AssetReferenceInput resolvedModel = model != null ? model : defaultModelRef();

        PredictionsConfigInput output = PredictionJobDefPayloadBuilder.outputConfig(outputObjectName, outputObjectLabel, outputDescription);
        PredictionJobDefCreateRequest req = PredictionJobDefPayloadBuilder.baseRequest(
                TYPE, apiName, label, description, resolvedModel, output, scoringMode);

        PredictionJobDefInputConfig inputConfig = PredictionJobDefPayloadBuilder.inputConfigBase(inputDataObject);
        inputConfig.setTextFields(textFields);
        req.setInputConfig(inputConfig);

        return createPredictionJobDef(req);
    }

    private static AssetReferenceInput defaultModelRef() {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setName(DEFAULT_MODEL_NAME);
        return ref;
    }
}
