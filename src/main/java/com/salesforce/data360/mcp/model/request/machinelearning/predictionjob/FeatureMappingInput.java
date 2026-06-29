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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Mapping between a model feature and the DMO field that supplies it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureMappingInput {

    @Valid
    @NotNull
    @McpToolParam(description = "The model feature this mapping fills.")
    private AssetReferenceInput modelField;

    @Valid
    @NotNull
    @McpToolParam(description = "The DMO field whose value is fed into the model feature.")
    private MappedFieldInput mappedField;

    @McpToolParam(description = "When true, an update to this field re-scores predictions for streaming jobs. Default false.", required = false)
    private Boolean updateScore;

    public AssetReferenceInput getModelField() {
        return modelField;
    }

    public void setModelField(AssetReferenceInput modelField) {
        this.modelField = modelField;
    }

    public MappedFieldInput getMappedField() {
        return mappedField;
    }

    public void setMappedField(MappedFieldInput mappedField) {
        this.mappedField = mappedField;
    }

    public Boolean getUpdateScore() {
        return updateScore;
    }

    public void setUpdateScore(Boolean updateScore) {
        this.updateScore = updateScore;
    }
}
