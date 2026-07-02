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
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Flat union of {@code ConnectApi.DataConnectorInput} and every concrete subtype:
 * {@code S3ConnectorInput}, {@code S3V2ConnectorInput}, {@code GcsConnectorInput},
 * {@code AzureConnectorInput}, {@code SftpConnectorInput},
 * {@code MarketingCloudConnectorInput}, {@code ExternalPlatformConnectorInput}.
 *
 * <p>The discriminator lives on the enclosing activation target as
 * {@code platformType} (S3, S3_V2, GCS, AZURE, SFTP, MARKETING_CLOUD, EXTERNAL_PLATFORM);
 * there is no discriminator field on this object itself. Only fields applicable to
 * the chosen platform type should be populated.
 *
 * <p>{@code @JsonInclude(NON_NULL)} omits unused fields on serialization. Per the
 * UDF, only S3, MarketingCloud, and ExternalPlatform declare subtype-specific
 * properties; Azure, GCS, S3V2, and SFTP carry only {@link #name}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectorInput {

    // ---- Shared by every subtype ----

    @McpToolParam(
        description = "Developer name of the connector. Required by every connector subtype.",
        required = false)
    private String name;

    // ---- S3ConnectorInput (platformType = S3) ----

    @McpToolParam(
        description = "S3 only: AWS access key.",
        required = false)
    private String accessKey;

    @McpToolParam(
        description = "S3 only: AWS secret key.",
        required = false)
    private String secretKey;

    @McpToolParam(
        description = "S3 only: target bucket name.",
        required = false)
    private String bucketName;

    @McpToolParam(
        description = "S3 only: folder name within the bucket.",
        required = false)
    private String folderName;

    @McpToolParam(
        description = "S3 only: output file format.",
        required = false)
    private String outputFormat;

    // ---- MarketingCloudConnectorInput (platformType = MARKETING_CLOUD) ----

    @McpToolParam(
        description = "Marketing Cloud only: business unit configuration.",
        required = false)
    private List<String> businessUnitConfig;

    @McpToolParam(
        description = "Marketing Cloud only: target subtype.",
        required = false)
    private String targetSubType;

    // ---- ExternalPlatformConnectorInput (platformType = EXTERNAL_PLATFORM) ----

    @McpToolParam(
        description = "External Platform only: per-field configuration for the destination schema.",
        required = false)
    private List<ExternalPlatformFieldInput> fieldConfig;

    @McpToolParam(
        description = "External Platform only: key prefix name used by the connector.",
        required = false)
    private String keyPrefixName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public List<String> getBusinessUnitConfig() {
        return businessUnitConfig;
    }

    public void setBusinessUnitConfig(List<String> businessUnitConfig) {
        this.businessUnitConfig = businessUnitConfig;
    }

    public String getTargetSubType() {
        return targetSubType;
    }

    public void setTargetSubType(String targetSubType) {
        this.targetSubType = targetSubType;
    }

    public List<ExternalPlatformFieldInput> getFieldConfig() {
        return fieldConfig;
    }

    public void setFieldConfig(List<ExternalPlatformFieldInput> fieldConfig) {
        this.fieldConfig = fieldConfig;
    }

    public String getKeyPrefixName() {
        return keyPrefixName;
    }

    public void setKeyPrefixName(String keyPrefixName) {
        this.keyPrefixName = keyPrefixName;
    }
}
