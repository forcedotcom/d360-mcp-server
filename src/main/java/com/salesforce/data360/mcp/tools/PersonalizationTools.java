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
package com.salesforce.data360.mcp.tools;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.personalization.ExtAppPreviewInputRepresentation;
import com.salesforce.data360.mcp.model.request.personalization.PersonalizationExperienceConfigInputRepresentation;
import com.salesforce.data360.mcp.model.request.personalization.TransformerInputRepresentation;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Data 360 Personalization Tools - P13N configuration queries.
 * Maps to /personalization/* Connect API endpoints.
 */
@Component
public class PersonalizationTools {

    private final Data360Client client;

    public PersonalizationTools(Data360Client client) {
        this.client = client;
    }

    // ── Engagement Signals ─────────────────────────────────────────────────

    @ApiEndpoint(path = "/personalization/engagement-signals", verb = "GET")
    @McpTool(
        name = "d360_p13n_engagement_signals_list",
        description = "List engagement signals configured for a personalization data graph. Returns signal types (click, view, purchase, add-to-cart, etc.) that feed into recommender attribution and objective optimization. Use this to discover which user interactions are tracked before configuring a recommender."
    )
    public String listEngagementSignals(
        @McpToolParam(description = "Data Space ID — the isolated workspace the recommender operates in") String dataSpaceId,
        @McpToolParam(description = "Data Graph ID — the profile or item data graph to query signals for") String dataGraphId
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("dataSpaceId", dataSpaceId);
            params.put("dataGraphId", dataGraphId);

            String path = ToolUtils.buildPath("/personalization/engagement-signals", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ── Org Info ───────────────────────────────────────────────────────────

    @ApiEndpoint(path = "/personalization/external-apps/org", verb = "GET")
    @McpTool(
        name = "d360_p13n_org_info_get",
        description = "Get current organization information for personalization. Returns Data Cloud Tenant Specific Endpoint (dcTse) used for connecting to Data Cloud services. Use this to discover the correct endpoint for your organization's Data Cloud instance."
    )
    public String getOrgInfo() {
        try {
            String path = "/personalization/external-apps/org";
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ── Mobile Preview ─────────────────────────────────────────────────────

    @ApiEndpoint(path = "/personalization/external-apps/mobile/live-preview-link", verb = "POST")
    @McpTool(
        name = "d360_p13n_mobile_preview_create",
        description = "Generate a mobile live preview link for testing experience configs and transformers. Returns a preview URL and expiration timestamp. PREREQUISITE: Data connector (Streaming App) must exist. Use this to test personalization configurations before deploying to production."
    )
    public String createMobileLivePreviewLink(
        @McpToolParam(description = "Mobile preview configuration including dataConnectorId, experienceConfig or transformer, and decision request body")
        ExtAppPreviewInputRepresentation request
    ) {
        try {
            String path = "/personalization/external-apps/mobile/live-preview-link";
            Map result = client.post(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ── Experience Configs ─────────────────────────────────────────────────

    @ApiEndpoint(path = "/personalization/external-apps/{id}/personalization-experience-configs", verb = "GET")
    @McpTool(
        name = "d360_p13n_experience_config_list",
        description = "List personalization experience configs for a data connector. Filter by personalization point, transformer, or dataspace. Use isWpmUrlRequired=true to get Web Personalization Manager edit URLs. PREREQUISITE: Data connector (Streaming App) must exist. Returns paginated results (limit 1-299, default 50). Experience configs define what personalized content to show and where to show it."
    )
    public String listExperienceConfigs(
        @McpToolParam(description = "Unique id, appSourceId or name of Streaming App Data Connector") String idOrAppSourceIdOrName,
        @McpToolParam(description = "Limits the number returned (1-299, default 50)", required = false) Integer limit,
        @McpToolParam(description = "Number to skip (default 0)", required = false) Integer offset,
        @McpToolParam(description = "Filter by personalization point name or ID", required = false) String personalizationPointNameOrId,
        @McpToolParam(description = "Filter by transformer name or ID", required = false) String transformerNameOrId,
        @McpToolParam(description = "Filter by data space ID or name", required = false) String dataSpaceIdOrName,
        @McpToolParam(description = "Include Web Personalization Manager URLs in response (default false)", required = false) Boolean isWpmUrlRequired
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            if (personalizationPointNameOrId != null) params.put("personalizationPointNameOrId", personalizationPointNameOrId);
            if (transformerNameOrId != null) params.put("transformerNameOrId", transformerNameOrId);
            if (dataSpaceIdOrName != null) params.put("dataSpaceIdOrName", dataSpaceIdOrName);
            if (isWpmUrlRequired != null) params.put("isWpmUrlRequired", isWpmUrlRequired);

            String basePath = "/personalization/external-apps/" + ToolUtils.encodePath(idOrAppSourceIdOrName) +
                            "/personalization-experience-configs";
            String path = ToolUtils.buildPath(basePath, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/{id}/personalization-experience-configs/{name}", verb = "GET")
    @McpTool(
        name = "d360_p13n_experience_config_get",
        description = "Get a specific personalization experience config by name. Use isWpmUrlRequired=true to get Web Personalization Manager edit URL. Returns the complete experience config including data provider, source matchers, and transformation configuration."
    )
    public String getExperienceConfig(
        @McpToolParam(description = "Unique id, appSourceId or name of Streaming App Data Connector") String idOrAppSourceIdOrName,
        @McpToolParam(description = "Unique name of Personalization Experience Config to retrieve") String nameParam,
        @McpToolParam(description = "Include Web Personalization Manager URL in response (default false)", required = false) Boolean isWpmUrlRequired
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (isWpmUrlRequired != null) params.put("isWpmUrlRequired", isWpmUrlRequired);

            String basePath = "/personalization/external-apps/" + ToolUtils.encodePath(idOrAppSourceIdOrName) +
                            "/personalization-experience-configs/" + ToolUtils.encodePath(nameParam);
            String path = ToolUtils.buildPath(basePath, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/{id}/personalization-experience-configs", verb = "POST")
    @McpTool(
        name = "d360_p13n_experience_config_create",
        description = "Create a personalization experience config. Defines what content to show (dataProvider), where to show it (sourceMatchers), and how to render it (transformationConfig). Use disableRelatedExperiences=true to disable other experiences in the same content zone. PREREQUISITES: Data connector, transformer, and personalization point must exist. See payload_examples for the complex nested structure."
    )
    public String createExperienceConfig(
        @McpToolParam(description = "Unique id, appSourceId or name of Streaming App Data Connector") String idOrAppSourceIdOrName,
        @McpToolParam(description = "Experience config with name, label, dataProvider, sourceMatchers, and transformationConfig") PersonalizationExperienceConfigInputRepresentation request,
        @McpToolParam(description = "Disable other experiences in the same content zone (default false)", required = false) Boolean disableRelatedExperiences
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (disableRelatedExperiences != null) params.put("disableRelatedExperiences", disableRelatedExperiences);

            String basePath = "/personalization/external-apps/" + ToolUtils.encodePath(idOrAppSourceIdOrName) +
                            "/personalization-experience-configs";
            String path = ToolUtils.buildPath(basePath, params);
            Map result = client.post(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/{id}/personalization-experience-configs/{name}", verb = "PUT")
    @McpTool(
        name = "d360_p13n_experience_config_update",
        description = "Update a personalization experience config. Use disableRelatedExperiences=true to disable other experiences in the same content zone. All fields are optional for partial updates. See payload_examples for structure."
    )
    public String updateExperienceConfig(
        @McpToolParam(description = "Unique id, appSourceId or name of Streaming App Data Connector") String idOrAppSourceIdOrName,
        @McpToolParam(description = "Unique name of Personalization Experience Config to update") String nameParam,
        @McpToolParam(description = "Experience config with fields to update (all optional)") PersonalizationExperienceConfigInputRepresentation request,
        @McpToolParam(description = "Disable other experiences in the same content zone (default false)", required = false) Boolean disableRelatedExperiences
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (disableRelatedExperiences != null) params.put("disableRelatedExperiences", disableRelatedExperiences);

            String basePath = "/personalization/external-apps/" + ToolUtils.encodePath(idOrAppSourceIdOrName) +
                            "/personalization-experience-configs/" + ToolUtils.encodePath(nameParam);
            String path = ToolUtils.buildPath(basePath, params);
            Map result = client.put(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/{id}/personalization-experience-configs/{name}", verb = "DELETE")
    @McpTool(
        name = "d360_p13n_experience_config_delete",
        description = "Delete a specific personalization experience config. This removes the personalization configuration and stops it from being delivered to the application. Cannot be undone."
    )
    public String deleteExperienceConfig(
        @McpToolParam(description = "Unique id, appSourceId or name of Streaming App Data Connector") String idOrAppSourceIdOrName,
        @McpToolParam(description = "Unique name of Personalization Experience Config to delete") String nameParam
    ) {
        try {
            String path = "/personalization/external-apps/" + ToolUtils.encodePath(idOrAppSourceIdOrName) +
                        "/personalization-experience-configs/" + ToolUtils.encodePath(nameParam);
            client.delete(path);
            return JsonUtil.toJson(Map.of("status", "deleted", "name", nameParam));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ── Transformers ───────────────────────────────────────────────────────

    @ApiEndpoint(path = "/personalization/external-apps/transformers", verb = "GET")
    @McpTool(
        name = "d360_p13n_transformer_list",
        description = "List personalization transformers with extensive filtering. Transformers define how to render content (HTML templates, scripts, mobile components). Filter by source (Global/Organization), schemaReference, category (EmbeddedContent/Agent), channel (MobileApp/WebApp), type (Handlebars/HtmlElementModifier/AgentScript/Component), enabled status, connector, dataspace, or attachment status. Returns paginated results (limit 1-299, default 50)."
    )
    public String listTransformers(
        @McpToolParam(description = "Limits the number returned (1-299, default 50)", required = false) Integer limit,
        @McpToolParam(description = "Number to skip (default 0)", required = false) Integer offset,
        @McpToolParam(description = "Source: Global or Organization (default Organization)", required = false) String source,
        @McpToolParam(description = "Filter by personalization schema reference", required = false) String schemaReference,
        @McpToolParam(description = "Filter by category: EmbeddedContent or Agent", required = false) String transformerCategory,
        @McpToolParam(description = "Filter by channel: MobileApp or WebApp", required = false) String channelType,
        @McpToolParam(description = "Filter by type: Handlebars, HtmlElementModifier, AgentScript, or Component", required = false) String transformerType,
        @McpToolParam(description = "Filter by enabled status", required = false) Boolean isEnabled,
        @McpToolParam(description = "Filter by data connector name or ID", required = false) String connectorNameOrId,
        @McpToolParam(description = "Filter by data space ID or name", required = false) String dataSpaceIdOrName,
        @McpToolParam(description = "Filter by attachment status. When false, returns unattached transformers. Combine with connectorNameOrId to get transformers matching that connector OR unattached", required = false) Boolean isDataConnectorAttached
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            if (source != null) params.put("source", source);
            if (schemaReference != null) params.put("schemaReference", schemaReference);
            if (transformerCategory != null) params.put("transformerCategory", transformerCategory);
            if (channelType != null) params.put("channelType", channelType);
            if (transformerType != null) params.put("transformerType", transformerType);
            if (isEnabled != null) params.put("isEnabled", isEnabled);
            if (connectorNameOrId != null) params.put("connectorNameOrId", connectorNameOrId);
            if (dataSpaceIdOrName != null) params.put("dataSpaceIdOrName", dataSpaceIdOrName);
            if (isDataConnectorAttached != null) params.put("isDataConnectorAttached", isDataConnectorAttached);

            String path = ToolUtils.buildPath("/personalization/external-apps/transformers", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/transformer", verb = "GET")
    @McpTool(
        name = "d360_p13n_transformer_get",
        description = "Get a specific personalization transformer by ID or name. Returns the complete transformer including substitution definitions, type details, and associated data connectors."
    )
    public String getTransformer(
        @McpToolParam(description = "Unique id or name of Personalization Transformer to retrieve") String idOrName
    ) {
        try {
            Map<String, Object> params = Map.of("idOrName", idOrName);
            String path = ToolUtils.buildPath("/personalization/external-apps/transformer", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/transformers", verb = "POST")
    @McpTool(
        name = "d360_p13n_transformer_create",
        description = "Create a personalization transformer. Transformers define how to render personalized content using templates (Handlebars), scripts, HTML modifiers, or mobile components. Includes substitution definitions (variables) that can be customized per experience. See payload_examples for the structure including name, label, channel, transformerType, substitutionDefinitions, and transformerTypeDetails."
    )
    public String createTransformer(
        @McpToolParam(description = "Transformer with name, label, description, channel, transformerType, substitutionDefinitions, and transformerTypeDetails") TransformerInputRepresentation request
    ) {
        try {
            String path = "/personalization/external-apps/transformers";
            Map result = client.post(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/transformer", verb = "PUT")
    @McpTool(
        name = "d360_p13n_transformer_update",
        description = "Update a personalization transformer. All fields are optional for partial updates. Note: Changing transformerType may require updating transformerTypeDetails to match the new type. See payload_examples for structure."
    )
    public String updateTransformer(
        @McpToolParam(description = "Unique id or name of Personalization Transformer to update") String idOrName,
        @McpToolParam(description = "Transformer with fields to update (all optional)") TransformerInputRepresentation request
    ) {
        try {
            Map<String, Object> params = Map.of("idOrName", idOrName);
            String path = ToolUtils.buildPath("/personalization/external-apps/transformer", params);
            Map result = client.put(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/external-apps/transformer", verb = "DELETE")
    @McpTool(
        name = "d360_p13n_transformer_delete",
        description = "Delete a specific personalization transformer. WARNING: This will break any experience configs that reference this transformer. Ensure no active experiences are using this transformer before deleting. Cannot be undone."
    )
    public String deleteTransformer(
        @McpToolParam(description = "Unique id or name of Personalization Transformer to delete") String idOrName
    ) {
        try {
            Map<String, Object> params = Map.of("idOrName", idOrName);
            String path = ToolUtils.buildPath("/personalization/external-apps/transformer", params);
            client.delete(path);
            return JsonUtil.toJson(Map.of("status", "deleted", "idOrName", idOrName));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ── Personalization Schemas ────────────────────────────────────────────

    @ApiEndpoint(path = "/personalization/personalization-schemas", verb = "POST")
    @McpTool(
        name = "d360_p13n_schema_create",
        description = "Create a personalization schema. Schemas define the structure of personalized content including attributes (variables), content objects (DMO), and related objects. Use for ExperienceVariation, FlowPath, ManualContent, or Recommendations types. PREREQUISITES: Data space and content DMO must exist. See payload_examples for the complex nested structure including attributes and contentObject."
    )
    public String createPersonalizationSchema(
        @McpToolParam(description = "Schema JSON with name, label, description, dataSpaceName, personalizationType, attributes array, and contentObject details") String requestJson
    ) {
        try {
            Map<String, Object> request = JsonUtil.fromJson(requestJson, Map.class);
            String path = "/personalization/personalization-schemas";
            Map result = client.post(path, request, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/personalization-schemas/{idOrName}", verb = "GET")
    @McpTool(
        name = "d360_p13n_schema_get",
        description = "Get a specific personalization schema by ID or name. Returns the complete schema including all attributes, content object configuration, and related content objects. Use this to inspect schema structure before creating personalization points."
    )
    public String getPersonalizationSchema(
        @McpToolParam(description = "Unique id or name of Personalization Schema to retrieve") String idOrName
    ) {
        try {
            String path = "/personalization/personalization-schemas/" + ToolUtils.encodePath(idOrName);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/personalization-schemas/{idOrName}", verb = "PATCH")
    @McpTool(
        name = "d360_p13n_schema_update",
        description = "Update a personalization schema. All fields are optional for partial updates. You can modify attributes, content object configuration, or related objects. Note that changes to the schema may affect existing personalization points using this schema. See payload_examples for structure."
    )
    public String updatePersonalizationSchema(
        @McpToolParam(description = "Unique id or name of Personalization Schema to update") String idOrName,
        @McpToolParam(description = "Schema JSON with fields to update (all optional)") String requestJson
    ) {
        try {
            Map<String, Object> request = JsonUtil.fromJson(requestJson, Map.class);
            String path = "/personalization/personalization-schemas/" + ToolUtils.encodePath(idOrName);
            Map result = client.patch(path, request, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/personalization-schemas/{idOrName}", verb = "DELETE")
    @McpTool(
        name = "d360_p13n_schema_delete",
        description = "Delete a specific personalization schema. WARNING: This will break any personalization points that reference this schema. Ensure no active personalization points are using this schema before deleting. Cannot be undone."
    )
    public String deletePersonalizationSchema(
        @McpToolParam(description = "Unique id or name of Personalization Schema to delete") String idOrName
    ) {
        try {
            String path = "/personalization/personalization-schemas/" + ToolUtils.encodePath(idOrName);
            client.delete(path);
            return JsonUtil.toJson(Map.of("status", "deleted", "idOrName", idOrName));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ── Personalization Points ─────────────────────────────────────────────

    @ApiEndpoint(path = "/personalization/personalization-points", verb = "POST")
    @McpTool(
        name = "d360_p13n_point_create",
        description = "Create a personalization point. Points define decision rules with attribute values that determine personalized content or experiences. Includes source tracking (BlockBuilder, ExperienceBuilder, FlowBuilder, PersonalizationApp), authentication requirements, and decision criteria. PREREQUISITES: Data space, profile data graph, and schema must exist. See payload_examples for the complex nested structure including decisions array."
    )
    public String createPersonalizationPoint(
        @McpToolParam(description = "Point JSON with name, label, dataSpaceName, profileDataGraphName, source, isAuthenticationRequired, decisions array, and optional schemaName/schemaEnum") String requestJson
    ) {
        try {
            Map<String, Object> request = JsonUtil.fromJson(requestJson, Map.class);
            String path = "/personalization/personalization-points";
            Map result = client.post(path, request, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/personalization-points/{idOrName}", verb = "GET")
    @McpTool(
        name = "d360_p13n_point_get",
        description = "Get a specific personalization point by ID or name. Returns the complete point including all decisions with attribute values and criteria, schema configuration, authentication settings, and current sync status. Use this to inspect point configuration before runtime execution."
    )
    public String getPersonalizationPoint(
        @McpToolParam(description = "Unique id or name of Personalization Point to retrieve") String idOrName
    ) {
        try {
            String path = "/personalization/personalization-points/" + ToolUtils.encodePath(idOrName);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/personalization-points/{idOrName}", verb = "PUT")
    @McpTool(
        name = "d360_p13n_point_update",
        description = "Update a personalization point. Full replacement update (PUT) - must provide all required fields. You can modify decisions, attribute values, criteria, schema associations, or authentication requirements. Changes take effect immediately for runtime execution. Note: This is PUT not PATCH - provide complete object. See payload_examples for structure."
    )
    public String updatePersonalizationPoint(
        @McpToolParam(description = "Unique id or name of Personalization Point to update") String idOrName,
        @McpToolParam(description = "Complete Point JSON with all required fields (PUT semantics)") String requestJson
    ) {
        try {
            Map<String, Object> request = JsonUtil.fromJson(requestJson, Map.class);
            String path = "/personalization/personalization-points/" + ToolUtils.encodePath(idOrName);
            Map result = client.put(path, request, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/personalization/personalization-points/{idOrName}", verb = "DELETE")
    @McpTool(
        name = "d360_p13n_point_delete",
        description = "Delete a specific personalization point. WARNING: This will immediately stop personalization at this point in experience/flow execution. Any references from experiments or batch decisions will break. Cannot be undone. Verify no active usage before deleting."
    )
    public String deletePersonalizationPoint(
        @McpToolParam(description = "Unique id or name of Personalization Point to delete") String idOrName
    ) {
        try {
            String path = "/personalization/personalization-points/" + ToolUtils.encodePath(idOrName);
            client.delete(path);
            return JsonUtil.toJson(Map.of("status", "deleted", "idOrName", idOrName));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
