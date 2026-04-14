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
 * Egress properties for file-based activation targets.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EgressPropertiesInput {

    @McpToolParam(description = "Filename date suffix format")
    private String filenameDateSuffixFormat;

    @McpToolParam(description = "Compression: Bzip2, Gzip, None")
    private String outputCompressionFormat;

    @McpToolParam(description = "Delimiter: BrokenPipe, Caret, Colon, Comma, Hash, Pipe, Semicolon, Slash, Tab, Tilde, Underscore")
    private String outputDelimiter;

    @McpToolParam(description = "Output format")
    private String outputFormat;

    @McpToolParam(description = "Max file size in MB (1-500)")
    private Integer outputMaxFileSizeMegaBytes;

    @McpToolParam(description = "Max records per file")
    private Integer outputMaxRecordsPerFile;

    @McpToolParam(description = "Child folder path", required = false)
    private String childFolder;

    @McpToolParam(description = "Custom filename", required = false)
    private String customFilename;

    @McpToolParam(description = "Whether subfolder creation is enabled", required = false)
    private Boolean isSubfolderCreationEnabled;

    @McpToolParam(description = "Predetermined filename: Activation, Segment, SegmentActivation", required = false)
    private String predeterminedFilename;

    public String getFilenameDateSuffixFormat() {
        return filenameDateSuffixFormat;
    }

    public void setFilenameDateSuffixFormat(String filenameDateSuffixFormat) {
        this.filenameDateSuffixFormat = filenameDateSuffixFormat;
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

    public Integer getOutputMaxFileSizeMegaBytes() {
        return outputMaxFileSizeMegaBytes;
    }

    public void setOutputMaxFileSizeMegaBytes(Integer outputMaxFileSizeMegaBytes) {
        this.outputMaxFileSizeMegaBytes = outputMaxFileSizeMegaBytes;
    }

    public Integer getOutputMaxRecordsPerFile() {
        return outputMaxRecordsPerFile;
    }

    public void setOutputMaxRecordsPerFile(Integer outputMaxRecordsPerFile) {
        this.outputMaxRecordsPerFile = outputMaxRecordsPerFile;
    }

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

    public Boolean getIsSubfolderCreationEnabled() {
        return isSubfolderCreationEnabled;
    }

    public void setIsSubfolderCreationEnabled(Boolean isSubfolderCreationEnabled) {
        this.isSubfolderCreationEnabled = isSubfolderCreationEnabled;
    }

    public String getPredeterminedFilename() {
        return predeterminedFilename;
    }

    public void setPredeterminedFilename(String predeterminedFilename) {
        this.predeterminedFilename = predeterminedFilename;
    }
}
