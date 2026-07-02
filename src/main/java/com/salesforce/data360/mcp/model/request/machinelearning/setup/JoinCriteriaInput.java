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
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/** One join clause between the primary dataset and a related dataset. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JoinCriteriaInput {

    @Valid
    @McpToolParam(description = "Field-pair conditions that make up the join key.", required = false)
    private List<JoinFieldsInput> fields;

    @McpToolParam(description = "How field-pair conditions combine within this join. One of: And, Or.", required = false)
    private String operator;

    @McpToolParam(description = "Join type. One of: Inner, Left, Right, Outer.", required = false)
    private String type;

    @McpToolParam(description = "API name of the primary (left) dataset.", required = false)
    private String left;

    @McpToolParam(description = "API name of the related (right) dataset.", required = false)
    private String right;

    public List<JoinFieldsInput> getFields() {
        return fields;
    }

    public void setFields(List<JoinFieldsInput> fields) {
        this.fields = fields;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }
}
