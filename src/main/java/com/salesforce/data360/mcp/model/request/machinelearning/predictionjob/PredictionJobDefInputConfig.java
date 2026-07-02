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
package com.salesforce.data360.mcp.model.request.machinelearning.predictionjob;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Input configuration for prediction job.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictionJobDefInputConfig {

    @Valid
    @NotNull
    @McpToolParam(description = "The input DMO whose rows are scored.")
    private AssetReferenceInput dataObject;

    @Valid
    @McpToolParam(description = "Predictive types only (Regression, BinaryClassification, MulticlassClassification, Clustering): mappings of model features to DMO fields.", required = false)
    private List<FeatureMappingInput> fields;

    @Valid
    @McpToolParam(description = "Sentiment detection and topic classification only: text fields to score.", required = false)
    private List<InputFieldInput> textFields;

    @McpToolParam(description = "Topic classification only. Distinct topic labels.", required = false)
    private List<String> topicLabels;

    public AssetReferenceInput getDataObject() {
        return dataObject;
    }

    public void setDataObject(AssetReferenceInput dataObject) {
        this.dataObject = dataObject;
    }

    public List<FeatureMappingInput> getFields() {
        return fields;
    }

    public void setFields(List<FeatureMappingInput> fields) {
        this.fields = fields;
    }

    public List<InputFieldInput> getTextFields() {
        return textFields;
    }

    public void setTextFields(List<InputFieldInput> textFields) {
        this.textFields = textFields;
    }

    @JsonIgnore
    public List<String> getTopicLabels() {
        return topicLabels;
    }

    public void setTopicLabels(List<String> topicLabels) {
        this.topicLabels = topicLabels;
    }

    /**
     * Serialize topicLabels as the wire shape the Connect API expects:
     * {@code {type: "Static", values: [...]}}. Only Static is supported today.
     */
    @JsonGetter("topicLabels")
    private Map<String, Object> serializeTopicLabels() {
        if (topicLabels == null) return null;
        Map<String, Object> wire = new LinkedHashMap<>();
        wire.put("type", "Static");
        wire.put("values", topicLabels);
        return wire;
    }

    /**
     * Accept the wire shape on deserialization so a round-trip through the
     * Connect API response (used in tests / playback) reads {@code values}
     * back into the flat {@code List<String>} field.
     */
    @JsonSetter("topicLabels")
    private void deserializeTopicLabels(Map<String, Object> wire) {
        if (wire == null) {
            this.topicLabels = null;
            return;
        }
        Object values = wire.get("values");
        if (values instanceof List<?> list) {
            this.topicLabels = list.stream().map(String::valueOf).toList();
        }
    }
}
