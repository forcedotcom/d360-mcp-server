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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for creating a Connect API data action
 * (POST /ssot/data-actions).
 *
 * <p>Mirrors ConnectApi.CdpDataActionInputRepresentation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionCreateRequest {

    @McpToolParam(description = "Action condition expression.")
    private String actionConditionExpression;

    @McpToolParam(description = "Action conditions.")
    private List<DataActionConditionInput> actionConditions;

    @McpToolParam(description = "Data action enrichment properties.")
    private List<DataActionEnrichmentInput> dataActionEnrichmentProperties;

    @NotBlank
    @McpToolParam(description = "Name of the data action.")
    private String dataActionName;

    @McpToolParam(description = "Data action projected fields.")
    private List<DataActionProjectedFieldInput> dataActionProjectedFields;

    @McpToolParam(description = "Data action sources.")
    private List<DataActionSourceInput> dataActionSources;

    @McpToolParam(description = "Data action status. One of Active, Error, Inactive, Processing", required = false)
    private String dataActionStatus;

    @McpToolParam(description = "Names of data action targets to invoke.")
    private List<String> dataActionTargetNames;

    @McpToolParam(description = "Dataspace name.")
    private String dataspace;

    @McpToolParam(description = "Description of the data action.", required = false)
    private String description;

    @NotBlank
    @McpToolParam(description = "Developer name (API name).")
    private String developerName;

    @McpToolParam(description = "Event trigger condition.", required = false)
    private String eventTriggerCondition;

    @McpToolParam(description = "External record identifier.", required = false)
    private String externalRecordIdentifier;

    @McpToolParam(description = "Whether this is a real-time action.", required = false)
    private Boolean isRealTimeAction;

    @McpToolParam(description = "Master label (display name).", required = false)
    private String masterLabel;

    @McpToolParam(description = "Search configuration for the data action.", required = false)
    private DataActionSearchConfigInput searchConfig;

    @McpToolParam(description = "Whether to process day zero records.", required = false)
    private Boolean shouldProcessDayZero;

    @McpToolParam(description = "If true, the related event is triggered only the first time that the data action is used. If false, the event is triggered each time the data action is used.", required = false)
    private Boolean shouldTriggerEventOnlyFirstTime;

    @McpToolParam(description = "Subscriber key.", required = false)
    private String subscriberKey;

    public String getActionConditionExpression() { return actionConditionExpression; }
    public void setActionConditionExpression(String actionConditionExpression) { this.actionConditionExpression = actionConditionExpression; }
    public List<DataActionConditionInput> getActionConditions() { return actionConditions; }
    public void setActionConditions(List<DataActionConditionInput> actionConditions) { this.actionConditions = actionConditions; }
    public List<DataActionEnrichmentInput> getDataActionEnrichmentProperties() { return dataActionEnrichmentProperties; }
    public void setDataActionEnrichmentProperties(List<DataActionEnrichmentInput> dataActionEnrichmentProperties) { this.dataActionEnrichmentProperties = dataActionEnrichmentProperties; }
    public String getDataActionName() { return dataActionName; }
    public void setDataActionName(String dataActionName) { this.dataActionName = dataActionName; }
    public List<DataActionProjectedFieldInput> getDataActionProjectedFields() { return dataActionProjectedFields; }
    public void setDataActionProjectedFields(List<DataActionProjectedFieldInput> dataActionProjectedFields) { this.dataActionProjectedFields = dataActionProjectedFields; }
    public List<DataActionSourceInput> getDataActionSources() { return dataActionSources; }
    public void setDataActionSources(List<DataActionSourceInput> dataActionSources) { this.dataActionSources = dataActionSources; }
    public String getDataActionStatus() { return dataActionStatus; }
    public void setDataActionStatus(String dataActionStatus) { this.dataActionStatus = dataActionStatus; }
    public List<String> getDataActionTargetNames() { return dataActionTargetNames; }
    public void setDataActionTargetNames(List<String> dataActionTargetNames) { this.dataActionTargetNames = dataActionTargetNames; }
    public String getDataspace() { return dataspace; }
    public void setDataspace(String dataspace) { this.dataspace = dataspace; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDeveloperName() { return developerName; }
    public void setDeveloperName(String developerName) { this.developerName = developerName; }
    public String getEventTriggerCondition() { return eventTriggerCondition; }
    public void setEventTriggerCondition(String eventTriggerCondition) { this.eventTriggerCondition = eventTriggerCondition; }
    public String getExternalRecordIdentifier() { return externalRecordIdentifier; }
    public void setExternalRecordIdentifier(String externalRecordIdentifier) { this.externalRecordIdentifier = externalRecordIdentifier; }
    public Boolean getIsRealTimeAction() { return isRealTimeAction; }
    public void setIsRealTimeAction(Boolean isRealTimeAction) { this.isRealTimeAction = isRealTimeAction; }
    public String getMasterLabel() { return masterLabel; }
    public void setMasterLabel(String masterLabel) { this.masterLabel = masterLabel; }
    public DataActionSearchConfigInput getSearchConfig() { return searchConfig; }
    public void setSearchConfig(DataActionSearchConfigInput searchConfig) { this.searchConfig = searchConfig; }
    public Boolean getShouldProcessDayZero() { return shouldProcessDayZero; }
    public void setShouldProcessDayZero(Boolean shouldProcessDayZero) { this.shouldProcessDayZero = shouldProcessDayZero; }
    public Boolean getShouldTriggerEventOnlyFirstTime() { return shouldTriggerEventOnlyFirstTime; }
    public void setShouldTriggerEventOnlyFirstTime(Boolean shouldTriggerEventOnlyFirstTime) { this.shouldTriggerEventOnlyFirstTime = shouldTriggerEventOnlyFirstTime; }
    public String getSubscriberKey() { return subscriberKey; }
    public void setSubscriberKey(String subscriberKey) { this.subscriberKey = subscriberKey; }
}
