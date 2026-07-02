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

/**
 * Mirrors ConnectApi.EgressPropertiesInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EgressPropertiesInput {

    @McpToolParam(description = "Child folder path for the output", required = false)
    private String childFolder;

    @McpToolParam(description = "Custom file name prefix for the output file", required = false)
    private String customFilename;

    @McpToolParam(description = "File name type for the output file. One of Custom, Predetermined.", required = false)
    private String fileNameType;

    @McpToolParam(description = "Date suffix format for the output file name", required = false)
    private String filenameDateSuffixFormat;

    @McpToolParam(description = "Indicates permission to create a subfolder", required = false)
    private Boolean isSubfolderCreationEnabled;

    @McpToolParam(description = "Compression format for the output file. One of Bzip2, Gzip, None.", required = false)
    private String outputCompressionFormat;

    @McpToolParam(description = "Delimiter for the output file data. One of BrokenPipe, Caret, Colon, Comma, Hash, Pipe, Semicolon, Slash, Tab, Tilde, Underscore.", required = false)
    private String outputDelimiter;

    @McpToolParam(description = "Output format for the output file", required = false)
    private String outputFormat;

    @McpToolParam(description = "Maximum file size in Mega bytes", required = false)
    private Long outputMaxFileSizeMegaBytes;

    @McpToolParam(description = "Maximum records per file", required = false)
    private Long outputMaxRecordsPerFile;

    @McpToolParam(description = "Predetermined file name. One of Activation, Segment, SegmentActivation.", required = false)
    private String predeterminedFilename;

    public String getChildFolder() {
        return childFolder;
    }

    public void setChildFolder(String childFolder) {
        this.childFolder = childFolder;
    }

    public String getCustomFilename() {
        return customFilename;
    }

    public void setCustomFilename(String customFilename) {
        this.customFilename = customFilename;
    }

    public String getFileNameType() {
        return fileNameType;
    }

    public void setFileNameType(String fileNameType) {
        this.fileNameType = fileNameType;
    }

    public String getFilenameDateSuffixFormat() {
        return filenameDateSuffixFormat;
    }

    public void setFilenameDateSuffixFormat(String filenameDateSuffixFormat) {
        this.filenameDateSuffixFormat = filenameDateSuffixFormat;
    }

    public Boolean getIsSubfolderCreationEnabled() {
        return isSubfolderCreationEnabled;
    }

    public void setIsSubfolderCreationEnabled(Boolean isSubfolderCreationEnabled) {
        this.isSubfolderCreationEnabled = isSubfolderCreationEnabled;
    }

    public String getOutputCompressionFormat() {
        return outputCompressionFormat;
    }

    public void setOutputCompressionFormat(String outputCompressionFormat) {
        this.outputCompressionFormat = outputCompressionFormat;
    }

    public String getOutputDelimiter() {
        return outputDelimiter;
    }

    public void setOutputDelimiter(String outputDelimiter) {
        this.outputDelimiter = outputDelimiter;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Long getOutputMaxFileSizeMegaBytes() {
        return outputMaxFileSizeMegaBytes;
    }

    public void setOutputMaxFileSizeMegaBytes(Long outputMaxFileSizeMegaBytes) {
        this.outputMaxFileSizeMegaBytes = outputMaxFileSizeMegaBytes;
    }

    public Long getOutputMaxRecordsPerFile() {
        return outputMaxRecordsPerFile;
    }

    public void setOutputMaxRecordsPerFile(Long outputMaxRecordsPerFile) {
        this.outputMaxRecordsPerFile = outputMaxRecordsPerFile;
    }

    public String getPredeterminedFilename() {
        return predeterminedFilename;
    }

    public void setPredeterminedFilename(String predeterminedFilename) {
        this.predeterminedFilename = predeterminedFilename;
    }
}
