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

/**
 * Frequency configuration for data stream refresh.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataStreamFrequencyInput {

    @McpToolParam(description = "Frequency type: Daily, Hourly, Minutely, Monthly, MonthlyRelative, Weekly, None, Transform", required = false)
    private String frequencyType;

    @McpToolParam(description = "Hours to run", required = false)
    private List<Integer> hours;

    @McpToolParam(description = "Days of month to refresh", required = false)
    private List<Integer> refreshDayOfMonth;

    @McpToolParam(description = "Day of week: Monday through Sunday", required = false)
    private String refreshDayOfWeek;

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public List<Integer> getHours() {
        return hours;
    }

    public void setHours(List<Integer> hours) {
        this.hours = hours;
    }

    public List<Integer> getRefreshDayOfMonth() {
        return refreshDayOfMonth;
    }

    public void setRefreshDayOfMonth(List<Integer> refreshDayOfMonth) {
        this.refreshDayOfMonth = refreshDayOfMonth;
    }

    public String getRefreshDayOfWeek() {
        return refreshDayOfWeek;
    }

    public void setRefreshDayOfWeek(String refreshDayOfWeek) {
        this.refreshDayOfWeek = refreshDayOfWeek;
    }
}
