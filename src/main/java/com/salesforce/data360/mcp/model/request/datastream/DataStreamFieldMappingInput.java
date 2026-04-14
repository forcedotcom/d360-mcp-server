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
package com.salesforce.data360.mcp.model.request.datastream;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Field mapping for data stream.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataStreamFieldMappingInput {

    @McpToolParam(description = "Source field label", required = false)
    private String sourceFieldLabel;

    @McpToolParam(description = "Target field name", required = false)
    private String targetFieldName;

    @McpToolParam(description = "Target field return type: Boolean, Currency, Date, DateTime, Email, Number, Percent, Phone, Text, Url", required = false)
    private String targetFieldReturntype;

    @McpToolParam(description = "Transformation formula", required = false)
    private String transformationFormula;

    public String getSourceFieldLabel() {
        return sourceFieldLabel;
    }

    public void setSourceFieldLabel(String sourceFieldLabel) {
        this.sourceFieldLabel = sourceFieldLabel;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    public String getTargetFieldReturntype() {
        return targetFieldReturntype;
    }

    public void setTargetFieldReturntype(String targetFieldReturntype) {
        this.targetFieldReturntype = targetFieldReturntype;
    }

    public String getTransformationFormula() {
        return transformationFormula;
    }

    public void setTransformationFormula(String transformationFormula) {
        this.transformationFormula = transformationFormula;
    }
}
