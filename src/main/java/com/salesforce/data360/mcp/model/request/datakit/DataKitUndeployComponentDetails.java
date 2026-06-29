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
package com.salesforce.data360.mcp.model.request.datakit;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Component details for undeploy operation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataKitUndeployComponentDetails {

    @McpToolParam(description = "Component name")
    private String name;

    @McpToolParam(description = "Component type. One of ActivationTarget, AnalyticsDashboard, AnalyticsVisualization, AnalyticsWorkspace, CalculatedInsight, CopyFieldEnrichment, CurrencyConfigObject, DataAction, DataActionTarget, DataCleanRoomDataSpecDef, DataCleanRoomProvider, DataConnection, DataCustomCode, DataGraph, DataLakeObject, DataSemanticSearch, DataShare, DataStreamBundle, DataTransform, EngagementSignal, FiscalCalendarConfigObject, IdentityResolution, IdpConfiguration, InternalDataConnector, IrRelatedListEnrichment, MarketSegment, MarketSegmentActivation, MlConfiguredModel, MlPredictionJob, MlRetriever, PersnlBatchDecision, PersonalizationObjective, PersonalizationPoint, PersonalizationRecommender, PersonalizationSchema, SecondaryIndex, SemanticModel, TuaTemplatedObject")
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
