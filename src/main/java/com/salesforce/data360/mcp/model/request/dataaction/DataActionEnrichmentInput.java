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
package com.salesforce.data360.mcp.model.request.dataaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/** Mirrors ConnectApi.DataActionEnrichmentInputRepresentation. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionEnrichmentInput {

    @McpToolParam(description = "Data action projected fields.", required = false)
    private List<DataActionProjectedFieldInput> dataActionProjectedFields;

    @McpToolParam(description = "Data action relationship edges.", required = false)
    private List<DataActionRelationshipEdgeInput> dataActionRelationshipEdges;

    @McpToolParam(description = "Data graph API name.", required = false)
    private String dataGraphApiName;

    @McpToolParam(description = "Enrichment name.", required = false)
    private String enrichmentName;

    public List<DataActionProjectedFieldInput> getDataActionProjectedFields() { return dataActionProjectedFields; }
    public void setDataActionProjectedFields(List<DataActionProjectedFieldInput> dataActionProjectedFields) { this.dataActionProjectedFields = dataActionProjectedFields; }
    public List<DataActionRelationshipEdgeInput> getDataActionRelationshipEdges() { return dataActionRelationshipEdges; }
    public void setDataActionRelationshipEdges(List<DataActionRelationshipEdgeInput> dataActionRelationshipEdges) { this.dataActionRelationshipEdges = dataActionRelationshipEdges; }
    public String getDataGraphApiName() { return dataGraphApiName; }
    public void setDataGraphApiName(String dataGraphApiName) { this.dataGraphApiName = dataGraphApiName; }
    public String getEnrichmentName() { return enrichmentName; }
    public void setEnrichmentName(String enrichmentName) { this.enrichmentName = enrichmentName; }
}
