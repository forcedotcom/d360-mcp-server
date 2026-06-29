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
 * Mirrors {@code CdpSegmentLookalikeInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentLookalikeInput {

    @McpToolParam(description = "Lookalike Additional Population Criteria", required = false)
    private Long additionalPopulation;

    @McpToolParam(description = "Seed Segment Name", required = false)
    private String seedSegmentName;

    public Long getAdditionalPopulation() {
        return additionalPopulation;
    }

    public void setAdditionalPopulation(Long additionalPopulation) {
        this.additionalPopulation = additionalPopulation;
    }

    public String getSeedSegmentName() {
        return seedSegmentName;
    }

    public void setSeedSegmentName(String seedSegmentName) {
        this.seedSegmentName = seedSegmentName;
    }
}
