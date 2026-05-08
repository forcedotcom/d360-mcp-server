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
package com.salesforce.data360.mcp.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Comprehensive catalog of all Data 360 tool families and their tools.
 * This is the single source of truth for the 3-tool code-mode MCP server.
 *
 * <p>Data sourced from the live {@code @McpTool} annotations and curated family metadata.
 * Tool names use the {@code d360_} prefix.
 */
public class FamilyCatalog {

    /** What the agent sees via search results. */
    public record FamilyEntry(String family, String summary, List<ToolInfo> tools) {}

    /** Individual tool info exposed to the agent via payload_examples. */
    public record ToolInfo(String name, String family, String description, String tips) {}

    private final List<FamilyEntry> families;
    private final Map<String, ToolInfo> toolIndex;

    public FamilyCatalog() {
        Map<String, String> summaries = buildFamilySummaries();
        List<ToolDef> allTools = buildAllTools();

        this.toolIndex = new LinkedHashMap<>();
        for (ToolDef def : allTools) {
            toolIndex.put(def.name, new ToolInfo(def.name, def.family, def.description, def.tips));
        }

        // Group into families
        Map<String, List<ToolInfo>> grouped = allTools.stream()
            .collect(Collectors.groupingBy(
                d -> d.family,
                LinkedHashMap::new,
                Collectors.mapping(d -> toolIndex.get(d.name), Collectors.toList())));

        this.families = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            String summary = summaries.getOrDefault(entry.getKey(), entry.getKey() + " operations.");
            families.add(new FamilyEntry(entry.getKey(), summary, entry.getValue()));
        }
    }

    public List<FamilyEntry> getAllFamilies() { return families; }

    public FamilyEntry getFamily(String familyName) {
        return families.stream().filter(f -> f.family().equals(familyName)).findFirst().orElse(null);
    }

    public ToolInfo getToolInfo(String toolName) { return toolIndex.get(toolName); }

    public List<String> getAllToolNames() { return List.copyOf(toolIndex.keySet()); }

    // ── Family Summaries (tuned for 89% search coverage) ───────────────────

    private static Map<String, String> buildFamilySummaries() {
        Map<String, String> s = new LinkedHashMap<>();
        s.put("Query", "Execute SQL queries against Data 360, inspect metadata, query profiles and calculated insights. Verify data landed, check record counts, explore schema, test expressions. Start here to explore what data exists.");
        s.put("DMO", "Data Model Objects define the target schema for customer, product, order, patient, vehicle, policy, billing, asset, and any entity data. Every data pipeline needs DMOs. Create DMOs before setting up mappings. Names end with __dlm. Also called data model, schema, entity, or table.");
        s.put("DLO", "Data Lake Objects are raw ingested data containers. Usually auto-created by data streams.");
        s.put("Mappings", "Map and link source fields to target DMO fields. Required for every data ingestion pipeline after creating DMOs. Map Snowflake columns, CRM fields, event properties, IoT telemetry attributes to their target data model objects. Define field-to-field correspondence between source and destination. Use smart mapping to auto-generate. Also called field mapping, schema mapping, column mapping, source-to-target mapping.");
        s.put("DataStreams", "Ingestion pipelines that bring external data into Data 360 from Snowflake, S3, databases, CRM, and other sources. Ingest customer, product, order, transaction, billing, and any structured data. Requires a Connection first.");
        s.put("Connection", "Connect to external data sources — Snowflake, S3, databases, CRM, ERP, POS, and any external system. Required first step before ingesting any external data. Every data pipeline starts with a connection.");
        s.put("Segment", "Create audience segments and target groups for marketing campaigns, personalized engagement, notifications, and outreach. Segment customers by lifetime value, behaviour, purchase history, risk score, churn probability, or any computed metric. Requires active Calculated Insights. Publish to calculate membership.");
        s.put("CalculatedInsights", "Compute metrics, scores, and insights over unified data using SQL. Build lifetime value, health scores, engagement scores, churn risk, cart abandonment, revenue aggregates, performance metrics, and any computed insight. Must reach ACTIVE status before segments can use them.");
        s.put("IdentityResolution", "Unify and match customer profiles across multiple data sources using match and reconciliation rules. Create a single unified view, 360-degree profile. Merge CRM contacts with Snowflake customers, web visitors, IoT device owners. Run after data is ingested and mapped.");
        s.put("Activation", "Push segments and audiences to external marketing platforms, email, SMS, push notifications, advertising channels, and engagement systems. Send personalized campaigns, offers, recommendations, and outreach. Requires active segment and an activation target.");
        s.put("Dataspace", "Isolated workspaces for data segregation. Most operations require a dataspace name.");
        s.put("DataTransform", "SQL-based data transformations run on a schedule or manually. Validate SQL before creating.");
        s.put("DataKit", "Pre-packaged data model templates. Deploy to quickly set up common schemas.");
        s.put("DataAction", "Trigger automated real-time actions, alerts, notifications, and workflows when data conditions are met. Send proactive notifications, service alerts, cart abandonment triggers, threshold-based actions. Requires a configured data action target.");
        s.put("SDM", "Semantic Data Models for BI queries and reporting. Create models, add data objects, define relationships, calculated dimensions, and metrics. Build business intelligence views over unified data.");
        s.put("Smart", "AI-assisted tools: auto-generate field mappings, recommend event date fields, preview field matches. Use instead of manual configuration.");
        s.put("StandardMappings", "Create standard DLO-to-DMO mappings from 500+ predefined definitions. Creates all mappings for a source object in one call. Preview before creating.");
        s.put("SearchIndex", "Manage search indexes for Data 360 entities. Create, update, delete, and configure search indexes.");
        s.put("Retriever", "RAG and machine learning retrievers for AI-powered search. Create, configure, and manage retriever endpoints for semantic search over Data 360 data.");
        return s;
    }

    // ── Tool Definitions ───────────────────────────────────────────────────

    private record ToolDef(String name, String family, String description, String httpMethod, String apiPath, String tips) {
        ToolDef(String name, String family, String description, String httpMethod, String apiPath) {
            this(name, family, description, httpMethod, apiPath, null);
        }
    }

    private static List<ToolDef> buildAllTools() {
        List<ToolDef> t = new ArrayList<>();

        // ── Query ──────────────────────────────────────────────────────────
        t.add(new ToolDef("d360_query_sql", "Query", "Execute SQL query. Supports parameterized queries.", "POST", "/ssot/query-sql"));
        t.add(new ToolDef("d360_query_sql_status", "Query", "Poll for long-running query status.", "GET", "/ssot/query-sql/{queryId}"));
        t.add(new ToolDef("d360_query_sql_rows", "Query", "Fetch paginated rows from completed query.", "GET", "/ssot/query-sql/{queryId}/rows"));
        t.add(new ToolDef("d360_query_sql_cancel", "Query", "Cancel a running query.", "DELETE", "/ssot/query-sql/{queryId}"));
        t.add(new ToolDef("d360_metadata", "Query", "Get metadata for entity. ALWAYS use entityName filter.", "GET", "/ssot/metadata"));
        t.add(new ToolDef("d360_metadata_search", "Query", "Search metadata using natural language. Preferred for discovery.", "POST", "/connect/search/metadata/results"));
        t.add(new ToolDef("d360_metadata_entities", "Query", "List paginated metadata entities. entityType required.", "GET", "/ssot/metadata-entities"));
        t.add(new ToolDef("d360_profile_query", "Query", "Query Individual, Account, or custom profile models.", "GET", "/ssot/profile/{dataModelName}"));
        t.add(new ToolDef("d360_profile_metadata", "Query", "Discover profile schema.", "GET", "/ssot/profile/metadata"));
        t.add(new ToolDef("d360_insights_query", "Query", "Query calculated insights with dimensions and measures.", "GET", "/ssot/insight/calculated-insights/{ciName}"));
        t.add(new ToolDef("d360_insights_metadata", "Query", "Discover CI names and available dimensions/measures.", "GET", "/ssot/insight/metadata"));
        t.add(new ToolDef("d360_datagraph_query", "Query", "Query data graphs. Set live=true for real-time.", "GET", "/ssot/data-graphs/data/{entity}/{id}"));
        t.add(new ToolDef("d360_datagraph_lookup", "Query", "Lookup by natural key.", "GET", "/ssot/data-graphs/data/{entity}"));
        t.add(new ToolDef("d360_datagraph_metadata", "Query", "List data graph entities or get schema.", "GET", "/ssot/data-graphs/metadata"));

        // ── DMO ────────────────────────────────────────────────────────────
        t.add(new ToolDef("d360_dmo_list", "DMO", "List all Data Model Objects. Filter by category.", "GET", "/ssot/data-model-objects"));
        t.add(new ToolDef("d360_dmo_get", "DMO", "Get full DMO schema including all fields.", "GET", "/ssot/data-model-objects/{dmoName}"));
        t.add(new ToolDef("d360_dmo_create", "DMO", "Create DMO. API name must end with __dlm.", "POST", "/ssot/data-model-objects"));
        t.add(new ToolDef("d360_dmo_update", "DMO", "Update DMO. Partial updates supported.", "PATCH", "/ssot/data-model-objects/{dmoName}"));
        t.add(new ToolDef("d360_dmo_delete", "DMO", "Delete DMO. Cascades to dependent objects.", "DELETE", "/ssot/data-model-objects/{dmoName}"));

        // ── DLO ────────────────────────────────────────────────────────────
        t.add(new ToolDef("d360_dlo_list", "DLO", "List all Data Lake Objects.", "GET", "/ssot/data-lake-objects"));
        t.add(new ToolDef("d360_dlo_get", "DLO", "Get DLO fields and metadata.", "GET", "/ssot/data-lake-objects/{dloName}"));
        t.add(new ToolDef("d360_dlo_create", "DLO", "Create a new DLO.", "POST", "/ssot/data-lake-objects"));
        t.add(new ToolDef("d360_dlo_update", "DLO", "Update DLO metadata.", "PATCH", "/ssot/data-lake-objects/{dloName}"));
        t.add(new ToolDef("d360_dlo_delete", "DLO", "Remove DLO.", "DELETE", "/ssot/data-lake-objects/{dloName}"));

        // ── Mappings ───────────────────────────────────────────────────────
        t.add(new ToolDef("d360_dmo_mapping_list", "Mappings", "List mappings by DMO name or CRM source.", "GET", "/ssot/data-model-object-mappings"));
        t.add(new ToolDef("d360_dmo_mapping_get", "Mappings", "Get mapping configuration.", "GET", "/ssot/data-model-object-mappings/{mappingName}"));
        t.add(new ToolDef("d360_dmo_mapping_create", "Mappings", "Create a single DLO-to-DMO mapping.", "POST", "/ssot/data-model-object-mappings"));
        t.add(new ToolDef("d360_dmo_mapping_update", "Mappings", "Update mapping. Changes affect future ingestion.", "PATCH", "/ssot/data-model-object-mappings/{mappingName}"));
        t.add(new ToolDef("d360_dmo_mapping_delete", "Mappings", "Delete mapping.", "DELETE", "/ssot/data-model-object-mappings/{mappingName}"));
        t.add(new ToolDef("d360_dmo_field_mapping_add", "Mappings", "Add field mappings to an existing object mapping.", "PATCH", "/ssot/data-model-object-mappings/{mappingName}/field-mappings"));
        t.add(new ToolDef("d360_dmo_field_mapping_delete", "Mappings", "Delete a single field mapping.", "DELETE", "/ssot/data-model-object-mappings/{mappingName}/field-mappings/{fieldMappingName}"));

        // ── DataStreams ────────────────────────────────────────────────────
        t.add(new ToolDef("d360_datastream_list", "DataStreams", "List data streams. Filter by category.", "GET", "/ssot/data-streams"));
        t.add(new ToolDef("d360_datastream_get", "DataStreams", "Get full stream configuration.", "GET", "/ssot/data-streams/{id}"));
        t.add(new ToolDef("d360_datastream_create", "DataStreams", "Create data stream. Requires Connection and DMO Mapping.", "POST", "/ssot/data-streams"));
        t.add(new ToolDef("d360_datastream_update", "DataStreams", "Update stream. Changes apply to future runs.", "PATCH", "/ssot/data-streams/{id}"));
        t.add(new ToolDef("d360_datastream_delete", "DataStreams", "Delete stream. Does not delete existing data.", "DELETE", "/ssot/data-streams/{id}"));
        t.add(new ToolDef("d360_datastream_run", "DataStreams", "Manually trigger ingestion.", "POST", "/ssot/data-streams/{id}/run"));
        t.add(new ToolDef("d360_datastream_create_sfdc", "DataStreams", "Create Salesforce CRM data stream. Auto-populates source fields.", "POST", "/ssot/data-streams"));
        t.add(new ToolDef("d360_datastream_create_aws_s3", "DataStreams", "Create AWS S3 data stream.", "POST", "/ssot/data-streams"));
        t.add(new ToolDef("d360_datastream_create_snowflake", "DataStreams", "Create Snowflake-backed data stream. Builds sourceFields, mappings, and advancedAttributes (database/schema/object) from field definitions.", "POST", "/ssot/data-streams"));

        // ── Connection ─────────────────────────────────────────────────────
        t.add(new ToolDef("d360_connection_list", "Connection", "List connections. connectorType REQUIRED.", "GET", "/ssot/connections"));
        t.add(new ToolDef("d360_connection_get", "Connection", "Get connection details. connectorType REQUIRED.", "GET", "/ssot/connections/{id}"));
        t.add(new ToolDef("d360_connection_create", "Connection", "Create connection. connectorType REQUIRED. Test first.", "POST", "/ssot/connections"));
        t.add(new ToolDef("d360_connection_update", "Connection", "Update connection. Can break dependent streams.", "PATCH", "/ssot/connections/{id}"));
        t.add(new ToolDef("d360_connection_delete", "Connection", "Delete connection. Breaks dependent streams.", "DELETE", "/ssot/connections/{id}"));
        t.add(new ToolDef("d360_connection_test", "Connection", "Validate connection before saving. Does NOT create.", "POST", "/ssot/connections/actions/test"));
        t.add(new ToolDef("d360_connector_list", "Connection", "Discover supported connector types.", "GET", "/ssot/connectors"));
        t.add(new ToolDef("d360_connector_metadata", "Connection", "Get required/optional fields for connector type.", "GET", "/ssot/connectors/{type}"));
        t.add(new ToolDef("d360_connection_endpoints", "Connection", "List pre-configured endpoints.", "GET", "/ssot/connection-endpoints"));
        t.add(new ToolDef("d360_snowflake_connection_list", "Connection", "List Data 360 connections for a connector type. Use connectorType=SNOWFLAKE for Snowflake.", "GET", "/connections"));
        t.add(new ToolDef("d360_connection_create_snowflake", "Connection", "Create a Snowflake connection with KeyPair auth. Provide private key content directly.", "POST", "/connections"));

        // ── Segment ────────────────────────────────────────────────────────
        t.add(new ToolDef("d360_segment_list", "Segment", "List all segments.", "GET", "/ssot/segments"));
        t.add(new ToolDef("d360_segment_get", "Segment", "Get segment by ID. Check segmentStatus for ACTIVE.", "GET", "/ssot/segments/{id}"));
        t.add(new ToolDef("d360_segment_create", "Segment", "Create segment. Requires ACTIVE CIs first.", "POST", "/ssot/segments"));
        t.add(new ToolDef("d360_segment_update", "Segment", "Update segment metadata. Cannot change SQL via PATCH.", "PATCH", "/ssot/segments/{id}"));
        t.add(new ToolDef("d360_segment_delete", "Segment", "Delete segment. Irreversible.", "DELETE", "/ssot/segments/{id}"));
        t.add(new ToolDef("d360_segment_publish", "Segment", "Trigger segment calculation.", "POST", "/ssot/segments/{id}/actions/publish"));

        // ── CalculatedInsights ─────────────────────────────────────────────
        t.add(new ToolDef("d360_ci_list", "CalculatedInsights", "List all CIs. Check status for ACTIVE.", "GET", "/ssot/calculated-insights"));
        t.add(new ToolDef("d360_ci_get", "CalculatedInsights", "Get CI details.", "GET", "/ssot/calculated-insights/{ciName}"));
        t.add(new ToolDef("d360_ci_create", "CalculatedInsights", "Create CI. apiName must end with __cio. No COUNT(DISTINCT).", "POST", "/ssot/calculated-insights"));
        t.add(new ToolDef("d360_ci_update", "CalculatedInsights", "Update CI. Triggers recompilation.", "PATCH", "/ssot/calculated-insights/{ciName}"));
        t.add(new ToolDef("d360_ci_delete", "CalculatedInsights", "Delete CI. Breaks dependent segments.", "DELETE", "/ssot/calculated-insights/{ciName}"));
        t.add(new ToolDef("d360_ci_enable", "CalculatedInsights", "Enable CI.", "POST", "/ssot/calculated-insights/{ciName}/actions/enable"));
        t.add(new ToolDef("d360_ci_disable", "CalculatedInsights", "Disable CI.", "POST", "/ssot/calculated-insights/{ciName}/actions/disable"));
        t.add(new ToolDef("d360_ci_run", "CalculatedInsights", "Execute CI calculation.", "POST", "/ssot/calculated-insights/{ciName}/actions/run"));
        t.add(new ToolDef("d360_ci_run_status", "CalculatedInsights", "Get CI run status.", "GET", "/ssot/calculated-insights/{ciName}/actions/run"));
        t.add(new ToolDef("d360_ci_validate", "CalculatedInsights", "Validate CI before creation.", "POST", "/ssot/calculated-insights/actions/validate"));

        // ── IdentityResolution ─────────────────────────────────────────────
        t.add(new ToolDef("d360_ir_list", "IdentityResolution", "List identity resolution rulesets.", "GET", "/ssot/identity-resolutions"));
        t.add(new ToolDef("d360_ir_get", "IdentityResolution", "Get ruleset details.", "GET", "/ssot/identity-resolutions/{id}"));
        t.add(new ToolDef("d360_ir_create", "IdentityResolution", "Create ruleset. Rules execute in priority order.", "POST", "/ssot/identity-resolutions"));
        t.add(new ToolDef("d360_ir_update", "IdentityResolution", "Update ruleset. Changes apply after republish.", "PATCH", "/ssot/identity-resolutions/{id}"));
        t.add(new ToolDef("d360_ir_full_update", "IdentityResolution", "Full replacement of ruleset.", "PUT", "/ssot/identity-resolutions/{id}"));
        t.add(new ToolDef("d360_ir_delete", "IdentityResolution", "Delete ruleset.", "DELETE", "/ssot/identity-resolutions/{id}"));
        t.add(new ToolDef("d360_ir_publish", "IdentityResolution", "Publish ruleset.", "POST", "/ssot/identity-resolutions/{id}/actions/publish"));
        t.add(new ToolDef("d360_ir_run", "IdentityResolution", "Execute ruleset.", "POST", "/ssot/identity-resolutions/{id}/actions/run-now"));

        // ── Activation ─────────────────────────────────────────────────────
        t.add(new ToolDef("d360_activation_list", "Activation", "List activations.", "GET", "/ssot/activations"));
        t.add(new ToolDef("d360_activation_get", "Activation", "Get activation details.", "GET", "/ssot/activations/{id}"));
        t.add(new ToolDef("d360_activation_create", "Activation", "Create activation. Requires active segment + target.", "POST", "/ssot/activations"));
        t.add(new ToolDef("d360_activation_update", "Activation", "Update activation.", "PATCH", "/ssot/activations/{id}"));
        t.add(new ToolDef("d360_activation_delete", "Activation", "Delete activation.", "DELETE", "/ssot/activations/{id}"));
        t.add(new ToolDef("d360_activation_target_list", "Activation", "List activation targets.", "GET", "/ssot/activation-targets"));
        t.add(new ToolDef("d360_activation_target_get", "Activation", "Get target details.", "GET", "/ssot/activation-targets/{id}"));
        t.add(new ToolDef("d360_activation_target_create", "Activation", "Create target. Must reference existing Connection.", "POST", "/ssot/activation-targets"));
        t.add(new ToolDef("d360_activation_target_update", "Activation", "Update target.", "PUT", "/ssot/activation-targets/{id}"));
        t.add(new ToolDef("d360_activation_target_delete", "Activation", "Delete target.", "DELETE", "/ssot/activation-targets/{id}"));

        // ── Dataspace ──────────────────────────────────────────────────────
        t.add(new ToolDef("d360_dataspace_list", "Dataspace", "List data spaces.", "GET", "/ssot/data-spaces"));
        t.add(new ToolDef("d360_dataspace_get", "Dataspace", "Get data space details.", "GET", "/ssot/data-spaces/{name}"));
        t.add(new ToolDef("d360_dataspace_create", "Dataspace", "Create isolated workspace.", "POST", "/ssot/data-spaces"));
        t.add(new ToolDef("d360_dataspace_update", "Dataspace", "Update data space.", "PATCH", "/ssot/data-spaces/{name}"));
        t.add(new ToolDef("d360_dataspace_delete", "Dataspace", "Delete data space and all contents.", "DELETE", "/ssot/data-spaces/{name}"));
        t.add(new ToolDef("d360_dataspace_member_list", "Dataspace", "List data space members.", "GET", "/ssot/data-spaces/{name}/members"));
        t.add(new ToolDef("d360_dataspace_member_add", "Dataspace", "Add member to data space.", "POST", "/ssot/data-spaces/{name}/members"));
        t.add(new ToolDef("d360_dataspace_member_remove", "Dataspace", "Remove member from data space.", "DELETE", "/ssot/data-spaces/{name}/members/{memberId}"));

        // ── DataTransform ──────────────────────────────────────────────────
        t.add(new ToolDef("d360_transform_list", "DataTransform", "List data transforms.", "GET", "/ssot/data-transforms"));
        t.add(new ToolDef("d360_transform_get", "DataTransform", "Get transform details.", "GET", "/ssot/data-transforms/{id}"));
        t.add(new ToolDef("d360_transform_create", "DataTransform", "Create transform. Schedule runs separately.", "POST", "/ssot/data-transforms"));
        t.add(new ToolDef("d360_transform_update", "DataTransform", "Update transform.", "PATCH", "/ssot/data-transforms/{id}"));
        t.add(new ToolDef("d360_transform_delete", "DataTransform", "Delete transform.", "DELETE", "/ssot/data-transforms/{id}"));
        t.add(new ToolDef("d360_transform_run", "DataTransform", "Execute transform manually.", "POST", "/ssot/data-transforms/{id}/actions/run"));
        t.add(new ToolDef("d360_transform_validate", "DataTransform", "Validate SQL syntax/semantics.", "POST", "/ssot/data-transforms-validation"));
        t.add(new ToolDef("d360_transform_schedule_get", "DataTransform", "Get transform schedule.", "GET", "/ssot/data-transforms/{id}/schedule"));
        t.add(new ToolDef("d360_transform_schedule_set", "DataTransform", "Set transform schedule.", "PUT", "/ssot/data-transforms/{id}/schedule"));

        // ── DataKit ────────────────────────────────────────────────────────
        t.add(new ToolDef("d360_datakit_list", "DataKit", "List available DataKits.", "GET", "/ssot/data-kits"));
        t.add(new ToolDef("d360_datakit_get", "DataKit", "Get DataKit details.", "GET", "/ssot/data-kits/{id}"));
        t.add(new ToolDef("d360_datakit_manifest", "DataKit", "Get DataKit manifest.", "GET", "/ssot/data-kits/{id}/manifest"));
        t.add(new ToolDef("d360_datakit_deploy", "DataKit", "Deploy/update components.", "POST", "/ssot/data-kits/update-components"));
        t.add(new ToolDef("d360_datakit_undeploy", "DataKit", "Undeploy components.", "POST", "/ssot/data-kits/{id}/undeploy"));
        t.add(new ToolDef("d360_datakit_deploy_status", "DataKit", "Get deployment job status.", "GET", "/ssot/data-kits/deployment-jobs/{jobId}"));
        t.add(new ToolDef("d360_datakit_component_status", "DataKit", "Get component deployment status.", "GET", "/ssot/data-kits/{id}/components/{cid}/deployment-status"));
        t.add(new ToolDef("d360_datakit_component_deps", "DataKit", "Get component dependencies.", "GET", "/ssot/data-kits/{id}/components/{cid}/dependencies"));
        t.add(new ToolDef("d360_datakit_components", "DataKit", "List DataKit components.", "GET", "/ssot/data-kits/{id}/components"));

        // ── DataAction ─────────────────────────────────────────────────────
        t.add(new ToolDef("d360_dataaction_list", "DataAction", "List data actions.", "GET", "/ssot/data-actions"));
        t.add(new ToolDef("d360_dataaction_get", "DataAction", "Get action details.", "GET", "/ssot/data-actions/{id}"));
        t.add(new ToolDef("d360_dataaction_create", "DataAction", "Create action. Must have target configured.", "POST", "/ssot/data-actions"));
        t.add(new ToolDef("d360_dataaction_target_list", "DataAction", "List action targets.", "GET", "/ssot/data-action-targets"));
        t.add(new ToolDef("d360_dataaction_target_get", "DataAction", "Get target details.", "GET", "/ssot/data-action-targets/{id}"));
        t.add(new ToolDef("d360_dataaction_target_create", "DataAction", "Create target.", "POST", "/ssot/data-action-targets"));
        t.add(new ToolDef("d360_dataaction_target_update", "DataAction", "Update target.", "PATCH", "/ssot/data-action-targets/{id}"));
        t.add(new ToolDef("d360_dataaction_target_delete", "DataAction", "Delete target.", "DELETE", "/ssot/data-action-targets/{id}"));

        // ── SDM ────────────────────────────────────────────────────────────
        t.add(new ToolDef("d360_sdm_list", "SDM", "List semantic models.", "GET", "/ssot/semantic/models"));
        t.add(new ToolDef("d360_sdm_get", "SDM", "Get semantic model.", "GET", "/ssot/semantic/models/{id}"));
        t.add(new ToolDef("d360_sdm_create", "SDM", "Create empty model. Add objects separately. dataspace REQUIRED.", "POST", "/ssot/semantic/models"));
        t.add(new ToolDef("d360_sdm_update", "SDM", "Update model metadata only.", "PATCH", "/ssot/semantic/models/{id}"));
        t.add(new ToolDef("d360_sdm_delete", "SDM", "Delete model and all sub-components.", "DELETE", "/ssot/semantic/models/{id}"));
        t.add(new ToolDef("d360_sdm_clone", "SDM", "Clone semantic model.", "POST", "/ssot/semantic/models/{id}/clone"));
        t.add(new ToolDef("d360_sdm_validate", "SDM", "Validate model before querying.", "POST", "/ssot/semantic/models/{id}/validate"));
        t.add(new ToolDef("d360_sdm_dependencies", "SDM", "Get model dependencies.", "GET", "/ssot/semantic/models/{id}/dependencies"));
        t.add(new ToolDef("d360_sdm_data_object_create", "SDM", "Add data object. Type: Dmo, Dlo, or Cio.", "POST", "/ssot/semantic/models/{id}/data-objects"));
        t.add(new ToolDef("d360_sdm_data_objects_list", "SDM", "List data objects in model.", "GET", "/ssot/semantic/models/{id}/data-objects"));
        t.add(new ToolDef("d360_sdm_data_object_get", "SDM", "Get data object.", "GET", "/ssot/semantic/models/{id}/data-objects/{oid}"));
        t.add(new ToolDef("d360_sdm_data_object_update", "SDM", "Update data object.", "PATCH", "/ssot/semantic/models/{id}/data-objects/{oid}"));
        t.add(new ToolDef("d360_sdm_data_object_delete", "SDM", "Delete data object. Breaks relationships.", "DELETE", "/ssot/semantic/models/{id}/data-objects/{oid}"));
        t.add(new ToolDef("d360_sdm_dimensions_list", "SDM", "List dimensions.", "GET", "/ssot/semantic/models/{id}/data-objects/{oid}/dimensions"));
        t.add(new ToolDef("d360_sdm_measurements_list", "SDM", "List measurements.", "GET", "/ssot/semantic/models/{id}/data-objects/{oid}/measurements"));
        t.add(new ToolDef("d360_sdm_calc_dims_list", "SDM", "List calculated dimensions.", "GET", "/ssot/semantic/models/{id}/calculated-dimensions"));
        t.add(new ToolDef("d360_sdm_calc_dim_create", "SDM", "Create calculated dimension. Use [DataObject].[Field] syntax.", "POST", "/ssot/semantic/models/{id}/calculated-dimensions"));
        t.add(new ToolDef("d360_sdm_calc_dim_get", "SDM", "Get calculated dimension.", "GET", "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}"));
        t.add(new ToolDef("d360_sdm_calc_dim_update", "SDM", "Update calculated dimension.", "PATCH", "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}"));
        t.add(new ToolDef("d360_sdm_calc_dim_delete", "SDM", "Delete calculated dimension.", "DELETE", "/ssot/semantic/models/{id}/calculated-dimensions/{dimId}"));
        t.add(new ToolDef("d360_sdm_calc_measures_list", "SDM", "List calculated measurements.", "GET", "/ssot/semantic/models/{id}/calculated-measurements"));
        t.add(new ToolDef("d360_sdm_calc_measure_create", "SDM", "Create calculated measurement. aggregationType UserAgg required.", "POST", "/ssot/semantic/models/{id}/calculated-measurements"));
        t.add(new ToolDef("d360_sdm_calc_measure_get", "SDM", "Get calculated measurement.", "GET", "/ssot/semantic/models/{id}/calculated-measurements/{mid}"));
        t.add(new ToolDef("d360_sdm_calc_measure_update", "SDM", "Update calculated measurement.", "PATCH", "/ssot/semantic/models/{id}/calculated-measurements/{mid}"));
        t.add(new ToolDef("d360_sdm_calc_measure_delete", "SDM", "Delete calculated measurement.", "DELETE", "/ssot/semantic/models/{id}/calculated-measurements/{mid}"));
        t.add(new ToolDef("d360_sdm_metrics_list", "SDM", "List metrics.", "GET", "/ssot/semantic/models/{id}/metrics"));
        t.add(new ToolDef("d360_sdm_metric_create", "SDM", "Create named metric for BI tools.", "POST", "/ssot/semantic/models/{id}/metrics"));
        t.add(new ToolDef("d360_sdm_metric_get", "SDM", "Get metric.", "GET", "/ssot/semantic/models/{id}/metrics/{mid}"));
        t.add(new ToolDef("d360_sdm_metric_update", "SDM", "Update metric.", "PATCH", "/ssot/semantic/models/{id}/metrics/{mid}"));
        t.add(new ToolDef("d360_sdm_metric_delete", "SDM", "Delete metric.", "DELETE", "/ssot/semantic/models/{id}/metrics/{mid}"));
        t.add(new ToolDef("d360_sdm_relationships_list", "SDM", "List relationships.", "GET", "/ssot/semantic/models/{id}/relationships"));
        t.add(new ToolDef("d360_sdm_relationship_create", "SDM", "Create relationship. Use semantic field names, not DMO names.", "POST", "/ssot/semantic/models/{id}/relationships"));
        t.add(new ToolDef("d360_sdm_relationship_get", "SDM", "Get relationship.", "GET", "/ssot/semantic/models/{id}/relationships/{rid}"));
        t.add(new ToolDef("d360_sdm_relationship_update", "SDM", "Update relationship.", "PATCH", "/ssot/semantic/models/{id}/relationships/{rid}"));
        t.add(new ToolDef("d360_sdm_relationship_delete", "SDM", "Delete relationship. Breaks queries.", "DELETE", "/ssot/semantic/models/{id}/relationships/{rid}"));
        t.add(new ToolDef("d360_sdm_formula_metadata", "SDM", "Reference available functions for calc dims/measures.", "GET", "/ssot/semantic/models/formula-metadata"));
        t.add(new ToolDef("d360_sdm_permissions", "SDM", "Get SDM permissions.", "GET", "/ssot/semantic/permissions"));
        t.add(new ToolDef("d360_sdm_query", "SDM", "Query semantic model. Use semanticModelId not apiName.", "POST", "/semantic-engine/gateway"));

        // ── Smart ──────────────────────────────────────────────────────────
        t.add(new ToolDef("d360_smart_mapping_suggest", "Smart", "Auto-generate field mappings using similarity.", null, null));
        t.add(new ToolDef("d360_preview_field_matches", "Smart", "Dry-run field matching with confidence scores.", null, null));
        t.add(new ToolDef("d360_smart_datastream_create", "Smart", "Auto-select immutable event date for Engagement streams.", null, null));
        t.add(new ToolDef("d360_event_date_recommend", "Smart", "Show mutable vs immutable date field scores.", null, null));

        // ── StandardMappings ───────────────────────────────────────────────
        t.add(new ToolDef("d360_standard_mapping_preview", "StandardMappings", "Preview standard DLO-to-DMO mappings from 500+ definitions.", "GET", null));
        t.add(new ToolDef("d360_standard_mapping_create", "StandardMappings", "Create ALL standard mappings for a source object in one call.", "POST", "/ssot/data-model-object-mappings"));

        // ── SearchIndex ────────────────────────────────────────────────────
        t.add(new ToolDef("d360_search_index_list", "SearchIndex", "List search indexes.", "GET", "/ssot/search-indexes"));
        t.add(new ToolDef("d360_search_index_get", "SearchIndex", "Get search index details.", "GET", "/ssot/search-indexes/{id}"));
        t.add(new ToolDef("d360_search_index_create", "SearchIndex", "Create search index.", "POST", "/ssot/search-indexes"));
        t.add(new ToolDef("d360_search_index_update", "SearchIndex", "Update search index.", "PATCH", "/ssot/search-indexes/{id}"));
        t.add(new ToolDef("d360_search_index_delete", "SearchIndex", "Delete search index.", "DELETE", "/ssot/search-indexes/{id}"));
        t.add(new ToolDef("d360_search_index_config", "SearchIndex", "Get search index configuration.", "GET", "/ssot/search-indexes/{id}/config"));
        t.add(new ToolDef("d360_search_index_process_history", "SearchIndex", "Get search index processing history.", "GET", "/ssot/search-indexes/{id}/process-history"));

        // ── Retriever ──────────────────────────────────────────────────────
        t.add(new ToolDef("d360_retriever_list", "Retriever", "List retrievers.", "GET", "/machine-learning/retrievers"));
        t.add(new ToolDef("d360_retriever_get", "Retriever", "Get retriever.", "GET", "/machine-learning/retrievers/{id}"));
        t.add(new ToolDef("d360_retriever_create", "Retriever", "Create retriever.", "POST", "/machine-learning/retrievers"));
        t.add(new ToolDef("d360_retriever_update", "Retriever", "Update retriever.", "PATCH", "/machine-learning/retrievers/{id}"));
        t.add(new ToolDef("d360_retriever_delete", "Retriever", "Delete retriever.", "DELETE", "/machine-learning/retrievers/{id}"));
        t.add(new ToolDef("d360_retriever_config_list", "Retriever", "List retriever configurations.", "GET", "/machine-learning/retrievers/{id}/configs"));
        t.add(new ToolDef("d360_retriever_config_get", "Retriever", "Get retriever configuration.", "GET", "/machine-learning/retrievers/{id}/configs/{configId}"));
        t.add(new ToolDef("d360_retriever_config_create", "Retriever", "Create retriever configuration.", "POST", "/machine-learning/retrievers/{id}/configs"));
        t.add(new ToolDef("d360_retriever_config_update", "Retriever", "Update retriever configuration.", "PATCH", "/machine-learning/retrievers/{id}/configs/{configId}"));
        t.add(new ToolDef("d360_retriever_config_delete", "Retriever", "Delete retriever configuration.", "DELETE", "/machine-learning/retrievers/{id}/configs/{configId}"));

        return t;
    }
}
