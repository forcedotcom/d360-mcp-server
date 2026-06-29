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
package com.salesforce.data360.mcp.model.request.retriever;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Mirror of {@code MlDmoFieldSourceTargetRelationshipInputRepresentation}.
 * Describes a single hop in the relationship path from a related DMO
 * back to the source DMO of the semantic search definition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MlDmoFieldSourceTargetRelationshipInput {

    @McpToolParam(description = "The source DMO field developer name", required = false)
    private String relationSourceDmoFieldName;

    @McpToolParam(description = "The source DMO developer name", required = false)
    private String relationSourceDmoName;

    @McpToolParam(description = "The target DMO field developer name", required = false)
    private String relationTargetDmoFieldName;

    @McpToolParam(description = "The target DMO developer name", required = false)
    private String relationTargetDmoName;

    public String getRelationSourceDmoFieldName() {
        return relationSourceDmoFieldName;
    }

    public void setRelationSourceDmoFieldName(String relationSourceDmoFieldName) {
        this.relationSourceDmoFieldName = relationSourceDmoFieldName;
    }

    public String getRelationSourceDmoName() {
        return relationSourceDmoName;
    }

    public void setRelationSourceDmoName(String relationSourceDmoName) {
        this.relationSourceDmoName = relationSourceDmoName;
    }

    public String getRelationTargetDmoFieldName() {
        return relationTargetDmoFieldName;
    }

    public void setRelationTargetDmoFieldName(String relationTargetDmoFieldName) {
        this.relationTargetDmoFieldName = relationTargetDmoFieldName;
    }

    public String getRelationTargetDmoName() {
        return relationTargetDmoName;
    }

    public void setRelationTargetDmoName(String relationTargetDmoName) {
        this.relationTargetDmoName = relationTargetDmoName;
    }
}
