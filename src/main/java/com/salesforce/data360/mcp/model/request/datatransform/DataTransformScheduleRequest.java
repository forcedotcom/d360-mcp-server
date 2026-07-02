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
 * Flat union of {@code CdpScheduleInputRepresentation} (abstract) and every
 * concrete subtype: {@code CdpDailyScheduleInputRepresentation},
 * {@code CdpHourlyScheduleInputRepresentation},
 * {@code CdpMonthlyRelativeScheduleInputRepresentation},
 * {@code CdpMonthlySpecificScheduleInputRepresentation} (frequency value
 * {@code Monthly}),
 * {@code CdpNoneScheduleInputRepresentation},
 * {@code CdpWeeklyScheduleInputRepresentation}.
 *
 * <p>The discriminator is {@code frequency}; only fields applicable to the
 * chosen frequency should be populated. {@code @JsonInclude(NON_NULL)} drops
 * unused fields on the wire.
 *
 * <p>The UDF {@code shouldForceSpecifiedMinutes} property is
 * {@code hidden="REST"} and is intentionally omitted.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTransformScheduleRequest {

    // ---- CdpScheduleInputRepresentation (shared) ----

    @McpToolParam(description = "Definition name the schedule applies to.", required = false)
    private String definitionName;

    @McpToolParam(
        description = "Frequency. One of Daily, Hourly, Monthly, MonthlyRelative, None, Weekly.",
        required = false)
    private String frequency;

    @McpToolParam(description = "Time of day for the schedule.", required = false)
    private DataTransformTimeInput time;

    // ---- Daily / Hourly ----

    @McpToolParam(
        description = "Daily, Hourly only: number of days/hours between runs.",
        required = false)
    private Integer interval;

    // ---- Hourly / Weekly ----

    @McpToolParam(
        description = "Hourly, Weekly only: days of the week to run. Each value one of MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.",
        required = false)
    private List<String> daysOfWeek;

    // ---- MonthlyRelative ----

    @McpToolParam(
        description = "MonthlyRelative only: day of the week to run. One of MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.",
        required = false)
    private String dayOfWeek;

    @McpToolParam(
        description = "MonthlyRelative only: relative week of the month. One of FIRST, SECOND, THIRD, FOURTH, LAST.",
        required = false)
    private String weekOfMonth;

    // ---- Monthly (CdpMonthlySpecificScheduleInputRepresentation) ----

    @McpToolParam(
        description = "Monthly only: days of the month to run (numbers 1-31, or -1 for last day).",
        required = false)
    private List<Integer> daysOfMonth;

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

    public DataTransformTimeInput getTime() {
        return time;
    }

    public void setTime(DataTransformTimeInput time) {
        this.time = time;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getWeekOfMonth() {
        return weekOfMonth;
    }

    public void setWeekOfMonth(String weekOfMonth) {
        this.weekOfMonth = weekOfMonth;
    }

    public List<Integer> getDaysOfMonth() {
        return daysOfMonth;
    }

    public void setDaysOfMonth(List<Integer> daysOfMonth) {
        this.daysOfMonth = daysOfMonth;
    }
}
