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
package com.salesforce.data360.mcp.model.request.datatransform;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;
import java.util.Map;

/**
 * Transform definition — polymorphic structure for STL or SQL transforms.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTransformDefinitionInput {

    @McpToolParam(description = "Definition type: STL or SQL")
    private String type;

    @McpToolParam(description = "Definition version")
    private String version;

    @McpToolParam(description = "Node graph (for STL/batch transforms)", required = false)
    private Map<String, Object> nodes;

    @McpToolParam(description = "UI rendering metadata", required = false)
    private Map<String, Object> ui;

    @McpToolParam(description = "SQL expression (for SQL/streaming transforms)", required = false)
    private String expression;

    @McpToolParam(description = "Target DLO name", required = false)
    private String targetDlo;

    @McpToolParam(description = "Target DMO name", required = false)
    private String targetDmo;

    @McpToolParam(description = "DCSQL manifest with nodes structure (for DCSQL transforms)", required = false)
    private Map<String, Object> manifest;

    @McpToolParam(description = "Output data objects for DLO auto-creation (enriched after validation)", required = false)
    private List<Map<String, Object>> outputDataObjects;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Object> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, Object> nodes) {
        this.nodes = nodes;
    }

    public Map<String, Object> getUi() {
        return ui;
    }

    public void setUi(Map<String, Object> ui) {
        this.ui = ui;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getTargetDlo() {
        return targetDlo;
    }

    public void setTargetDlo(String targetDlo) {
        this.targetDlo = targetDlo;
    }

    public String getTargetDmo() {
        return targetDmo;
    }

    public void setTargetDmo(String targetDmo) {
        this.targetDmo = targetDmo;
    }

    public Map<String, Object> getManifest() {
        return manifest;
    }

    public void setManifest(Map<String, Object> manifest) {
        this.manifest = manifest;
    }

    public List<Map<String, Object>> getOutputDataObjects() {
        return outputDataObjects;
    }

    public void setOutputDataObjects(List<Map<String, Object>> outputDataObjects) {
        this.outputDataObjects = outputDataObjects;
    }
}
