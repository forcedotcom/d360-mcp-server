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
package com.salesforce.data360.mcp.model.request.machinelearning.predict;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictRequest {

    @Valid
    @NotNull
    @McpToolParam(description = "Configured model reference (id or developer name)")
    private AssetReferenceInput model;

    @NotEmpty
    @McpToolParam(description = "Feature names matching the configured model's input feature schema. Order must align with each row in 'rows'.")
    private List<String> fieldNames;

    @NotEmpty
    @McpToolParam(description = "Feature value rows. Each inner row's length must equal fieldNames.length.")
    private List<List<String>> rows;

    @Valid
    @McpToolParam(description = "Optional response-shaping settings.", required = false)
    private PredictSettings settings;

    /**
     * this is fixed and not exposed as a tool parameter.
     */
    public String getType() {
        return "RawData";
    }

    public AssetReferenceInput getModel() {
        return model;
    }

    public void setModel(AssetReferenceInput model) {
        this.model = model;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

    public PredictSettings getSettings() {
        return settings;
    }

    public void setSettings(PredictSettings settings) {
        this.settings = settings;
    }
}
