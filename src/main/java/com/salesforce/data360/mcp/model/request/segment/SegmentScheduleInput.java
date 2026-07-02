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
package com.salesforce.data360.mcp.model.request.segment;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Mirrors {@code ScheduleInputRepresentation} (the abstract CDP segment schedule input).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentScheduleInput {

    @McpToolParam(description = "Definition name", required = false)
    private String definitionName;

    @McpToolParam(description = "Frequency", required = false)
    private String frequency;

    @McpToolParam(description = "Time Info", required = false)
    private SegmentScheduleTimeInput timeInfo;

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public SegmentScheduleTimeInput getTimeInfo() {
        return timeInfo;
    }

    public void setTimeInfo(SegmentScheduleTimeInput timeInfo) {
        this.timeInfo = timeInfo;
    }
}
