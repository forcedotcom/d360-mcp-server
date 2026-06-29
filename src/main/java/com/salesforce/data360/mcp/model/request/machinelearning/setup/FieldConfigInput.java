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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Polymorphic field configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldConfigInput {

    @NotBlank
    @McpToolParam(description = "Field type. One of: Text, Date, Number. Per-type fields: Text uses includeOther/balanced/ordering/dataType/values; Date uses min/max (ISO date strings); Number uses min/max (doubles) plus optional bucketingStrategy.")
    private String type;

    @NotBlank
    @McpToolParam(description = "Field API name (must exist on the input source).")
    private String name;

    @McpToolParam(description = "Display label.", required = false)
    private String label;

    @McpToolParam(description = "Owning data source name (multi-source only).", required = false)
    private String source;

    @McpToolParam(description = "Mark field as ignored (excluded from training).", required = false)
    private Boolean ignored;

    @McpToolParam(description = "Text fields only — group rare values into an \"Other\" bucket.", required = false)
    private Boolean includeOther;

    @McpToolParam(description = "Text fields only — balance class frequencies during training.", required = false)
    private Boolean balanced;

    @McpToolParam(description = "Text fields only — value ordering. One of: Occurrence, Alphabetical, Numeric.", required = false)
    private String ordering;

    @McpToolParam(description = "Text fields only — data type. One of: Categorical, Boolean.", required = false)
    private String dataType;

    @Valid
    @McpToolParam(description = "Text fields only — distinct value configurations.", required = false)
    private List<TextFieldValueInput> values;

    @McpToolParam(description = "Date fields: ISO-8601 minimum (e.g. 2024-01-01). Number fields: numeric minimum.", required = false)
    private Object min;

    @McpToolParam(description = "Date fields: ISO-8601 maximum. Number fields: numeric maximum.", required = false)
    private Object max;

    @Valid
    @McpToolParam(description = "Number fields only — bucketing strategy.", required = false)
    private BucketingStrategyInput bucketingStrategy;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getIgnored() {
        return ignored;
    }

    public void setIgnored(Boolean ignored) {
        this.ignored = ignored;
    }

    public Boolean getIncludeOther() {
        return includeOther;
    }

    public void setIncludeOther(Boolean includeOther) {
        this.includeOther = includeOther;
    }

    public Boolean getBalanced() {
        return balanced;
    }

    public void setBalanced(Boolean balanced) {
        this.balanced = balanced;
    }

    public String getOrdering() {
        return ordering;
    }

    public void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<TextFieldValueInput> getValues() {
        return values;
    }

    public void setValues(List<TextFieldValueInput> values) {
        this.values = values;
    }

    public Object getMin() {
        return min;
    }

    public void setMin(Object min) {
        this.min = min;
    }

    public Object getMax() {
        return max;
    }

    public void setMax(Object max) {
        this.max = max;
    }

    public BucketingStrategyInput getBucketingStrategy() {
        return bucketingStrategy;
    }

    public void setBucketingStrategy(BucketingStrategyInput bucketingStrategy) {
        this.bucketingStrategy = bucketingStrategy;
    }
}
