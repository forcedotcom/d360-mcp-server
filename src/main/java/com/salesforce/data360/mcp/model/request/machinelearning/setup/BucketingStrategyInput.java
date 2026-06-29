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
package com.salesforce.data360.mcp.model.request.machinelearning.setup;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Numeric-field bucketing strategy.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BucketingStrategyInput {

    @McpToolParam(description = "Bucketing strategy. One of: EvenWidth, Manual, Percentile. EvenWidth/Percentile use numberOfBuckets; Manual uses explicit buckets[].", required = false)
    private String type;

    @McpToolParam(description = "Number of buckets (EvenWidth / Percentile strategies).", required = false)
    private Integer numberOfBuckets;

    @McpToolParam(description = "Explicit bucket boundaries (Manual strategy only). Each entry is a numeric upper-bound for one bucket.", required = false)
    private List<Double> buckets;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getNumberOfBuckets() { return numberOfBuckets; }
    public void setNumberOfBuckets(Integer numberOfBuckets) { this.numberOfBuckets = numberOfBuckets; }
    public List<Double> getBuckets() { return buckets; }
    public void setBuckets(List<Double> buckets) { this.buckets = buckets; }
}
