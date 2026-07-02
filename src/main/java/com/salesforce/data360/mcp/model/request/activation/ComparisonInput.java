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
package com.salesforce.data360.mcp.model.request.activation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Flat union of {@code ConnectApi.BaseComparisonInput} and every concrete subtype:
 * {@code LogicalComparisonInput}, {@code EntityScopedGroupInput}, and the seven
 * primitive comparisons (Boolean, Number, Text, Date, DateOnly,
 * ExactlyRelativeDate, RelativeToNowDate).
 *
 * <p>The wire-level discriminator is {@code operator}; only fields applicable to
 * the chosen operator should be populated. {@code @JsonInclude(NON_NULL)} omits
 * unused fields on the way out.
 *
 * <p>The primitive {@code value} property collides across subtypes (Boolean /
 * Double / Integer). It is exposed to the LLM as three distinct typed fields —
 * {@link #valueBoolean}, {@link #valueNumber}, {@link #valueInt} — so the JSON
 * Schema gives the model a precise type per operator group. All three are
 * {@code WRITE_ONLY}; on serialization to Connect API they collapse to a single
 * {@code "value"} key via {@link #getValue()}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComparisonInput {

    // ---- BaseComparisonInput (discriminator) ----

    @McpToolParam(
        description = "Operator discriminator. Logical group: AND, OR. Entity-scoped: ANY, ALL, NONE. "
            + "Primitive comparisons (Boolean/Number/Text/Date): EQ, NEQ, GT, LT, GTE, LTE, BETWEEN, IN, etc.",
        required = false)
    private String operator;

    // ---- LogicalComparisonInput (operator = AND / OR) ----

    @McpToolParam(
        description = "Logical operators (AND, OR) only: nested filter expressions joined by the operator.",
        required = false)
    private List<TypeAndFilterInput> filtersConfig;

    // ---- EntityScopedGroupInput (operator = ANY / ALL / NONE) ----

    @McpToolParam(
        description = "Entity-scoped operators (ANY, ALL, NONE) only: type of the scoped group.",
        required = false)
    private String type;

    @McpToolParam(
        description = "Entity-scoped operators (ANY, ALL, NONE) only: object/DMO API name being scoped.",
        required = false)
    private String objectName;

    @McpToolParam(
        description = "Entity-scoped operators (ANY, ALL, NONE) only: source of the attribute being scoped.",
        required = false)
    private String attributeSource;

    @McpToolParam(
        description = "Entity-scoped operators (ANY, ALL, NONE) only: query path from the activate-on DMO to the scoped entity.",
        required = false)
    private List<QueryPathInputConfig> queryPathConfigFromActivateOnToEntity;

    @McpToolParam(
        description = "Entity-scoped operators (ANY, ALL, NONE) only: nested condition evaluated within the scope.",
        required = false)
    private ComparisonInput condition;

    // ---- PrimitiveComparisonInput (shared by Boolean/Number/Text/Date/RelativeDate) ----

    @McpToolParam(
        description = "Primitive operators only: subject (object + field) of the comparison.",
        required = false)
    private SubjectInput subject;

    @McpToolParam(
        description = "Primitive operators only: path used to reach the subject field.",
        required = false)
    private List<LabeledSubjectsConfigInput> path;

    @McpToolParam(
        description = "Primitive operators only: join path used to reach the subject field.",
        required = false)
    private List<LabeledSubjectsConfigInput> joinPath;

    @McpToolParam(
        description = "Primitive operators only: whether the comparison references the same record.",
        required = false)
    private Boolean selfReference;

    // ---- Primitive `value` field, typed per subtype.
    // The LLM populates exactly one of these. They serialize as a single `"value"` key
    // via getValue(); WRITE_ONLY suppresses their own emission. ----

    @McpToolParam(
        description = "BooleanComparison only: boolean value to compare against.",
        required = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean valueBoolean;

    @McpToolParam(
        description = "NumberComparison only: numeric value to compare against. For BETWEEN, set firstBoundValue and secondBoundValue instead.",
        required = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Double valueNumber;

    @McpToolParam(
        description = "ExactlyRelativeDateComparison and RelativeToNowDateComparison only: integer offset (e.g. number of days).",
        required = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer valueInt;

    // ---- NumberComparisonInput BETWEEN bounds ----

    @McpToolParam(
        description = "NumberComparison BETWEEN only: lower bound.",
        required = false)
    private Double firstBoundValue;

    @McpToolParam(
        description = "NumberComparison BETWEEN only: upper bound.",
        required = false)
    private Double secondBoundValue;

    // ---- TextComparisonInput / DateComparisonInput / DateOnlyComparisonInput ----

    @McpToolParam(
        description = "TextComparison, DateComparison, DateOnlyComparison only: list of values for IN-style operators.",
        required = false)
    private List<String> filterConfig;

    // ---- ExactlyRelativeDateComparisonInput ----

    @McpToolParam(
        description = "ExactlyRelativeDateComparison only: date unit. One of Days, Months, Years.",
        required = false)
    private String dateUnits;

    /**
     * Wire-side getter that emits whichever typed value field is populated as a
     * single {@code "value"} key. Connect API expects one untyped {@code value};
     * the LLM-facing schema uses three typed fields for clearer hints.
     */
    @JsonProperty("value")
    public Object getValue() {
        if (valueBoolean != null) {
            return valueBoolean;
        }
        if (valueNumber != null) {
            return valueNumber;
        }
        return valueInt;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<TypeAndFilterInput> getFiltersConfig() {
        return filtersConfig;
    }

    public void setFiltersConfig(List<TypeAndFilterInput> filtersConfig) {
        this.filtersConfig = filtersConfig;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getAttributeSource() {
        return attributeSource;
    }

    public void setAttributeSource(String attributeSource) {
        this.attributeSource = attributeSource;
    }

    public List<QueryPathInputConfig> getQueryPathConfigFromActivateOnToEntity() {
        return queryPathConfigFromActivateOnToEntity;
    }

    public void setQueryPathConfigFromActivateOnToEntity(List<QueryPathInputConfig> queryPathConfigFromActivateOnToEntity) {
        this.queryPathConfigFromActivateOnToEntity = queryPathConfigFromActivateOnToEntity;
    }

    public ComparisonInput getCondition() {
        return condition;
    }

    public void setCondition(ComparisonInput condition) {
        this.condition = condition;
    }

    public SubjectInput getSubject() {
        return subject;
    }

    public void setSubject(SubjectInput subject) {
        this.subject = subject;
    }

    public List<LabeledSubjectsConfigInput> getPath() {
        return path;
    }

    public void setPath(List<LabeledSubjectsConfigInput> path) {
        this.path = path;
    }

    public List<LabeledSubjectsConfigInput> getJoinPath() {
        return joinPath;
    }

    public void setJoinPath(List<LabeledSubjectsConfigInput> joinPath) {
        this.joinPath = joinPath;
    }

    public Boolean getSelfReference() {
        return selfReference;
    }

    public void setSelfReference(Boolean selfReference) {
        this.selfReference = selfReference;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public Double getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
    }

    public Integer getValueInt() {
        return valueInt;
    }

    public void setValueInt(Integer valueInt) {
        this.valueInt = valueInt;
    }

    public Double getFirstBoundValue() {
        return firstBoundValue;
    }

    public void setFirstBoundValue(Double firstBoundValue) {
        this.firstBoundValue = firstBoundValue;
    }

    public Double getSecondBoundValue() {
        return secondBoundValue;
    }

    public void setSecondBoundValue(Double secondBoundValue) {
        this.secondBoundValue = secondBoundValue;
    }

    public List<String> getFilterConfig() {
        return filterConfig;
    }

    public void setFilterConfig(List<String> filterConfig) {
        this.filterConfig = filterConfig;
    }

    public String getDateUnits() {
        return dateUnits;
    }

    public void setDateUnits(String dateUnits) {
        this.dateUnits = dateUnits;
    }
}
