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
public class TopicClassificationPredictionJobDefTools extends AbstractPredictionJobDefTools {

    private static final String TYPE = "TopicClassification";
    private static final String DEFAULT_MODEL_NAME = "sfdc_ai__DefaultSalesforceTopicClassification";

    public TopicClassificationPredictionJobDefTools(Data360Client client) {
        super(client);
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions", verb = "POST")
    @McpTool(
        name = "d360_prediction_job_def_create_topic_classification",
        description = "Create a TopicClassification prediction job definition. "
            + "Required: apiName, inputDataObject, textFields (1-3 entries), "
            + "topicLabels (2-5 distinct non-blank entries, each ≤ 100 characters), outputObjectName. "
            + "model defaults to a reference by name to '" + DEFAULT_MODEL_NAME + "' (Salesforce-provided hyper classifier) when omitted. "
            + "Only Static topic labels are supported today; Dynamic mapping from a DMO is not yet exposed. "
    )
    public String createTopicClassificationPredictionJobDef(
        @McpToolParam(description = "api name for the prediction job definition.") String apiName,
        @McpToolParam(description = "Reference to the input DMO whose rows will be classified. Provide either id or name.") AssetReferenceInput inputDataObject,
        @McpToolParam(description = "Up to 3 text fields from the input DMO to classify.") List<InputFieldInput> textFields,
        @McpToolParam(description = "Distinct, non-blank topic labels (2-5 entries, each ≤ 100 characters).") List<String> topicLabels,
        @McpToolParam(description = "Api name for the output DMO that will hold topic results.") String outputObjectName,
        @McpToolParam(description = "Reference to the configured model. Defaults to a reference by name to '" + DEFAULT_MODEL_NAME + "' when omitted.", required = false) AssetReferenceInput model,
        @McpToolParam(description = "Display label (defaults to apiName).", required = false) String label,
        @McpToolParam(description = "Free-form description.", required = false) String description,
        @McpToolParam(description = "Display label for the output DMO.", required = false) String outputObjectLabel,
        @McpToolParam(description = "Description for the output DMO.", required = false) String outputDescription,
        @McpToolParam(description = "Scoring mode (Batch, Streaming). Default Batch.", required = false) String scoringMode
    ) {
        if (textFields == null || textFields.isEmpty()) {
            return errorJson("textFields is required for TopicClassification jobs (one or more text fields from the input DMO).");
        }
        if (topicLabels == null || topicLabels.isEmpty()) {
            return errorJson("topicLabels is required for TopicClassification jobs (2-5 distinct non-blank labels).");
        }

        AssetReferenceInput resolvedModel = model != null ? model : defaultModelRef();

        PredictionsConfigInput output = PredictionJobDefPayloadBuilder.outputConfig(outputObjectName, outputObjectLabel, outputDescription);
        PredictionJobDefCreateRequest req = PredictionJobDefPayloadBuilder.baseRequest(
                TYPE, apiName, label, description, resolvedModel, output, scoringMode);

        PredictionJobDefInputConfig inputConfig = PredictionJobDefPayloadBuilder.inputConfigBase(inputDataObject);
        inputConfig.setTextFields(textFields);
        inputConfig.setTopicLabels(topicLabels);
        req.setInputConfig(inputConfig);

        return createPredictionJobDef(req);
    }

    private static AssetReferenceInput defaultModelRef() {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setName(DEFAULT_MODEL_NAME);
        return ref;
    }
}
