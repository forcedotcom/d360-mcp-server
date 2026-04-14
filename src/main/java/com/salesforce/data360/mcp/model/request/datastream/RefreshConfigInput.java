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

import java.util.List;
import java.util.Map;

/**
 * Refresh configuration for data stream.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshConfigInput {

    @McpToolParam(description = "Refresh mode: TOTAL_REPLACE, UPSERT, INCREMENTAL, REPLACE, NEAR_REAL_TIME_INCREMENTAL, PARTIAL_UPDATE", required = false)
    private String refreshMode;

    @McpToolParam(description = "Whether acceleration is enabled", required = false)
    private Boolean isAccelerationEnabled;

    @McpToolParam(description = "Data Stream Refresh frequency configurations", required = false)
    private DataStreamFrequencyInput frequency;

    @McpToolParam(description = "Additional attributes", required = false)
    private Map<String, Object> additionalAttributes;

    public String getRefreshMode() {
        return refreshMode;
    }

    public void setRefreshMode(String refreshMode) {
        this.refreshMode = refreshMode;
    }

    public Boolean getIsAccelerationEnabled() {
        return isAccelerationEnabled;
    }

    public void setIsAccelerationEnabled(Boolean isAccelerationEnabled) {
        this.isAccelerationEnabled = isAccelerationEnabled;
    }

    public DataStreamFrequencyInput getFrequency() {
        return frequency;
    }

    public void setFrequency(DataStreamFrequencyInput frequency) {
        this.frequency = frequency;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }
}
