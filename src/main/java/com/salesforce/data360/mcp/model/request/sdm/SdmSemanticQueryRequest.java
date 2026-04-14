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
package com.salesforce.data360.mcp.model.request.sdm;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/** Request body for executing a semantic query. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SdmSemanticQueryRequest {

    @McpToolParam(description = "ID of the semantic model to query")
    private String semanticModelId;

    @McpToolParam(description = "Structured semantic query as JSON object")
    private String structuredSemanticQuery;

    public String getSemanticModelId() {
        return semanticModelId;
    }

    public void setSemanticModelId(String semanticModelId) {
        this.semanticModelId = semanticModelId;
    }

    public String getStructuredSemanticQuery() {
        return structuredSemanticQuery;
    }

    public void setStructuredSemanticQuery(String structuredSemanticQuery) {
        this.structuredSemanticQuery = structuredSemanticQuery;
    }
}
