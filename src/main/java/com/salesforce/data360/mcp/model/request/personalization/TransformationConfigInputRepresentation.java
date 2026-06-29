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
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Personalization Experience Config Transformation Config Input Representation.
 * Configures how and when transformations are applied.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformationConfigInputRepresentation {

    @McpToolParam(description = "Should we render the content from the transformation if it couldn't be transformed? Default if missing is not to render")
    private Boolean renderIfEmpty;

    @McpToolParam(description = "When should this transformation occur in relationship to its target being available? Values: Immediately, ExitIntent, ElementClick, ScrollPercent")
    private String when;

    @Valid
    @McpToolParam(description = "Additional data needed for specific TransformationConfigWhenType (e.g., CSS selector for ElementClick, percentage for ScrollPercent)")
    private WhenValuesInputRepresentation whenValues;

    @McpToolParam(description = "How will this transformed content interact with the config's target. Values: ReplaceContentZoneContent, ReplaceElementContent, AddAfterElement, AddBeforeElement, AddOverlay, ContentZoneHandler")
    private String method;

    @McpToolParam(description = "If provided this will need to map to a specific destination entry in the Engagement config which would determine the Event Attributes needed to land the data in Data Cloud with the necessary attributes to correctly map this into the target Engagement Destination Data Model Object (DMO)")
    private String engagementDestination;

    @Valid
    @McpToolParam(description = "List of transformations defining how the Data Provider Response will be transformed into acceptable content")
    private List<TransformationInputRepresentation> transformations;

    public Boolean getRenderIfEmpty() {
        return renderIfEmpty;
    }

    public void setRenderIfEmpty(Boolean renderIfEmpty) {
        this.renderIfEmpty = renderIfEmpty;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public WhenValuesInputRepresentation getWhenValues() {
        return whenValues;
    }

    public void setWhenValues(WhenValuesInputRepresentation whenValues) {
        this.whenValues = whenValues;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEngagementDestination() {
        return engagementDestination;
    }

    public void setEngagementDestination(String engagementDestination) {
        this.engagementDestination = engagementDestination;
    }

    public List<TransformationInputRepresentation> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<TransformationInputRepresentation> transformations) {
        this.transformations = transformations;
    }
}
