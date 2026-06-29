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
package com.salesforce.data360.mcp.model.request.datatransform;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for {@code POST /ssot/data-transforms-validation}.
 *
 * <p>The endpoint takes a full {@code DataTransformInputRepresentation} body
 * (same shape as create) and returns a validation report. Inherits the shared
 * fields from {@link DataTransformBaseRequest} and adds the
 * {@code DataTransformInputRepresentation} fields that are not on the base
 * class. REST-hidden UDF properties ({@code capabilities},
 * {@code creationType}, {@code tags}) are intentionally omitted.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTransformValidateRequest extends DataTransformBaseRequest {

    @McpToolParam(description = "Currency ISO code.", required = false)
    private String currencyIsoCode;

    @McpToolParam(
        description = "Definitions for batch transforms (one entry per batch definition).",
        required = false)
    private List<DataTransformDefinitionInput> definitions;

    @McpToolParam(
        description = "Full run frequency. One of NONE, DAILY, WEEKLY, MONTHLY.",
        required = false)
    private String fullRunFrequency;

    @McpToolParam(
        description = "Primary source for the transform when there are table joins.",
        required = false)
    private String primarySource;

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public List<DataTransformDefinitionInput> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<DataTransformDefinitionInput> definitions) {
        this.definitions = definitions;
    }

    public String getFullRunFrequency() {
        return fullRunFrequency;
    }

    public void setFullRunFrequency(String fullRunFrequency) {
        this.fullRunFrequency = fullRunFrequency;
    }

    public String getPrimarySource() {
        return primarySource;
    }

    public void setPrimarySource(String primarySource) {
        this.primarySource = primarySource;
    }
}
