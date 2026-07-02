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
package com.salesforce.data360.mcp.model.request.personalization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Decision Request Body Input Representation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionRequestBodyInputRepresentation {

    @JsonProperty("p")
    @Valid
    @McpToolParam(description = "Decision request items (decisionId, cohortId, experimentId)")
    private DecisionRequestItemsInputRepresentation p;

    @JsonProperty("c")
    @Valid
    @McpToolParam(description = "Contextual attributes (individualId, anchorId)")
    private ContextInputRepresentation c;

    public DecisionRequestItemsInputRepresentation getP() {
        return p;
    }

    public void setP(DecisionRequestItemsInputRepresentation p) {
        this.p = p;
    }

    public ContextInputRepresentation getC() {
        return c;
    }

    public void setC(ContextInputRepresentation c) {
        this.c = c;
    }
}
