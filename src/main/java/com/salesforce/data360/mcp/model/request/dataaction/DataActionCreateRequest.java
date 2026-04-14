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
import java.util.Map;

/**
 * Request body for creating a data action.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionCreateRequest {

    @McpToolParam(description = "Name of the data action")
    private String dataActionName;

    @McpToolParam(description = "Developer name")
    private String developerName;

    @McpToolParam(description = "Data space developer name")
    private String dataspace;

    @McpToolParam(description = "Description")
    private String description;

    @McpToolParam(description = "Master label")
    private String masterLabel;

    @McpToolParam(description = "Action condition expression")
    private String actionConditionExpression;

    @McpToolParam(description = "Event trigger condition")
    private String eventTriggerCondition;

    @McpToolParam(description = "External record identifier")
    private String externalRecordIdentifier;

    @McpToolParam(description = "Subscriber key")
    private String subscriberKey;

    @McpToolParam(description = "Whether this is a real-time action")
    private Boolean isRealTimeAction;

    @McpToolParam(description = "Whether to process day zero")
    private Boolean shouldProcessDayZero;

    @McpToolParam(description = "Whether to trigger event only first time")
    private Boolean shouldTriggerEventOnlyFirstTime;

    @McpToolParam(description = "Array of data action source objects")
    private List<Map<String, Object>> dataActionSources;

    @McpToolParam(description = "Array of action condition objects")
    private List<Map<String, Object>> actionConditions;

    @McpToolParam(description = "Array of target names")
    private List<String> dataActionTargetNames;

    @McpToolParam(description = "Array of projected field objects")
    private List<Map<String, Object>> dataActionProjectedFields;

    @McpToolParam(description = "Array of enrichment property objects")
    private List<Map<String, Object>> dataActionEnrichmentProperties;

    public String getDataActionName() {
        return dataActionName;
    }

    public void setDataActionName(String dataActionName) {
        this.dataActionName = dataActionName;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getDataspace() {
        return dataspace;
    }

    public void setDataspace(String dataspace) {
        this.dataspace = dataspace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMasterLabel() {
        return masterLabel;
    }

    public void setMasterLabel(String masterLabel) {
        this.masterLabel = masterLabel;
    }

    public String getActionConditionExpression() {
        return actionConditionExpression;
    }

    public void setActionConditionExpression(String actionConditionExpression) {
        this.actionConditionExpression = actionConditionExpression;
    }

    public String getEventTriggerCondition() {
        return eventTriggerCondition;
    }

    public void setEventTriggerCondition(String eventTriggerCondition) {
        this.eventTriggerCondition = eventTriggerCondition;
    }

    public String getExternalRecordIdentifier() {
        return externalRecordIdentifier;
    }

    public void setExternalRecordIdentifier(String externalRecordIdentifier) {
        this.externalRecordIdentifier = externalRecordIdentifier;
    }

    public String getSubscriberKey() {
        return subscriberKey;
    }

    public void setSubscriberKey(String subscriberKey) {
        this.subscriberKey = subscriberKey;
    }

    public Boolean getIsRealTimeAction() {
        return isRealTimeAction;
    }

    public void setIsRealTimeAction(Boolean isRealTimeAction) {
        this.isRealTimeAction = isRealTimeAction;
    }

    public Boolean getShouldProcessDayZero() {
        return shouldProcessDayZero;
    }

    public void setShouldProcessDayZero(Boolean shouldProcessDayZero) {
        this.shouldProcessDayZero = shouldProcessDayZero;
    }

    public Boolean getShouldTriggerEventOnlyFirstTime() {
        return shouldTriggerEventOnlyFirstTime;
    }

    public void setShouldTriggerEventOnlyFirstTime(Boolean shouldTriggerEventOnlyFirstTime) {
        this.shouldTriggerEventOnlyFirstTime = shouldTriggerEventOnlyFirstTime;
    }

    public List<Map<String, Object>> getDataActionSources() {
        return dataActionSources;
    }

    public void setDataActionSources(List<Map<String, Object>> dataActionSources) {
        this.dataActionSources = dataActionSources;
    }

    public List<Map<String, Object>> getActionConditions() {
        return actionConditions;
    }

    public void setActionConditions(List<Map<String, Object>> actionConditions) {
        this.actionConditions = actionConditions;
    }

    public List<String> getDataActionTargetNames() {
        return dataActionTargetNames;
    }

    public void setDataActionTargetNames(List<String> dataActionTargetNames) {
        this.dataActionTargetNames = dataActionTargetNames;
    }

    public List<Map<String, Object>> getDataActionProjectedFields() {
        return dataActionProjectedFields;
    }

    public void setDataActionProjectedFields(List<Map<String, Object>> dataActionProjectedFields) {
        this.dataActionProjectedFields = dataActionProjectedFields;
    }

    public List<Map<String, Object>> getDataActionEnrichmentProperties() {
        return dataActionEnrichmentProperties;
    }

    public void setDataActionEnrichmentProperties(List<Map<String, Object>> dataActionEnrichmentProperties) {
        this.dataActionEnrichmentProperties = dataActionEnrichmentProperties;
    }
}
