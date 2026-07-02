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
package com.salesforce.data360.mcp.model.request.personalization;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Data specific to the type of transformer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformerInputDetails {

    @McpToolParam(description = "The Handlebars Html Template to be used to transform the content")
    private String html;

    @McpToolParam(description = "If true then this transformer will not auto-inject engagement attributes and instead rely on the HTML providing the attributes using substitution values to provide the data. Default is auto-inject")
    private Boolean disableAutoInjectionOfEngagementAttributes;

    @McpToolParam(description = "The Handlebars Script Template to be used to transform the content")
    private String script;

    @McpToolParam(description = "A name the Component (which for now is Mobile) developer provides to indicate which Component Class in the Application will do the rendering. Web version of this if desired could be the name of a React Component class")
    private String componentName;

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Boolean getDisableAutoInjectionOfEngagementAttributes() {
        return disableAutoInjectionOfEngagementAttributes;
    }

    public void setDisableAutoInjectionOfEngagementAttributes(Boolean disableAutoInjectionOfEngagementAttributes) {
        this.disableAutoInjectionOfEngagementAttributes = disableAutoInjectionOfEngagementAttributes;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
