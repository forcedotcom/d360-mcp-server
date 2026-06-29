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

import java.util.Map;

/**
 * Flat union of {@code ConnectApi.CdpDataActionTargetConfig} (abstract) and every
 * concrete subtype: {@code CdpDataActionTargetCRMConfig},
 * {@code CdpDataActionTargetGrpcConfig}, {@code CdpDataActionTargetInternalWebConfig},
 * {@code CdpDataActionTargetMCConfig}, {@code CdpDataActionTargetWebConfig}.
 *
 * <p>The discriminators live on the enclosing
 * {@link DataActionTargetCreateRequest}: {@code type} selects the broad target
 * (Core = CRM, Internal_WebHook = InternalWeb, MarketingCloud = MC, WebHook = Web)
 * and {@code subType} (Grpc / Rest) further selects the WebHook flavour. There
 * is no discriminator field on this object itself. Only fields applicable to
 * the chosen {@code type}/{@code subType} should be populated.
 *
 * <p>{@code @JsonInclude(NON_NULL)} omits unused fields on serialization. Per
 * the UDF, {@code InternalWebConfig} and {@code WebConfig} declare no
 * subtype-specific properties — they carry only the {@link #apiContract} and
 * {@link #targetEndpoint} inherited from the abstract base.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionTargetConfig {

    // ---- CdpDataActionTargetConfig (shared by every subtype) ----

    @McpToolParam(
        description = "Shared by every subtype: API contract for the target.",
        required = false)
    private String apiContract;

    @McpToolParam(
        description = "Shared by every subtype: target endpoint URL.",
        required = false)
    private String targetEndpoint;

    // ---- CdpDataActionTargetCRMConfig (type = Core) ----

    @McpToolParam(
        description = "CRM (type=Core) only: org ID for the target CRM.",
        required = false)
    private String orgId;

    @McpToolParam(
        description = "CRM (type=Core) only: org label for the target CRM.",
        required = false)
    private String orgLabel;

    // ---- CdpDataActionTargetMCConfig (type = MarketingCloud) ----

    @McpToolParam(
        description = "Marketing Cloud (type=MarketingCloud) only: content key.",
        required = false)
    private String contentKey;

    @McpToolParam(
        description = "Marketing Cloud (type=MarketingCloud) only: content template.",
        required = false)
    private String contentTemplate;

    // ---- CdpDataActionTargetGrpcConfig (type = WebHook, subType = Grpc) ----

    @McpToolParam(
        description = "gRPC (type=WebHook, subType=Grpc) only: name of the gRPC service.",
        required = false)
    private String serviceName;

    @McpToolParam(
        description = "gRPC (type=WebHook, subType=Grpc) only: name of the gRPC method to invoke.",
        required = false)
    private String methodName;

    @McpToolParam(
        description = "gRPC (type=WebHook, subType=Grpc) only: contents of the proto buffer definition.",
        required = false)
    private String protoBufferContent;

    @McpToolParam(
        description = "gRPC (type=WebHook, subType=Grpc) only: mapping configuration from event payload to gRPC method message.",
        required = false)
    private DataActionTargetGrpcMappingConfig mappingConfig;

    @McpToolParam(
        description = "gRPC (type=WebHook, subType=Grpc) only: custom metadata sent with the gRPC request.",
        required = false)
    private Map<String, String> customMetadata;

    public String getApiContract() {
        return apiContract;
    }

    public void setApiContract(String apiContract) {
        this.apiContract = apiContract;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgLabel() {
        return orgLabel;
    }

    public void setOrgLabel(String orgLabel) {
        this.orgLabel = orgLabel;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getProtoBufferContent() {
        return protoBufferContent;
    }

    public void setProtoBufferContent(String protoBufferContent) {
        this.protoBufferContent = protoBufferContent;
    }

    public DataActionTargetGrpcMappingConfig getMappingConfig() {
        return mappingConfig;
    }

    public void setMappingConfig(DataActionTargetGrpcMappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    public Map<String, String> getCustomMetadata() {
        return customMetadata;
    }

    public void setCustomMetadata(Map<String, String> customMetadata) {
        this.customMetadata = customMetadata;
    }
}
