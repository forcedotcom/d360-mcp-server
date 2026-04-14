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

import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for integration tests against a running MCP server.
 *
 * <p>Provides a SAFE_TOOLS allowlist of read-only tool names, a shared HTTP client,
 * and configuration loading from environment variables or a {@code .env.test} file.</p>
 *
 * <p>Subclasses should be annotated with {@code @Tag("integration")} so they are
 * excluded from the normal unit-test suite and only run via
 * {@code mvn test -Dgroups=integration}.</p>
 */
public abstract class IntegrationTestBase {

    // ── Read-only tool allowlist ──────────────────────────────────────────────

    /**
     * Tools that are safe to invoke in integration tests because they perform
     * only read / query / list / get / metadata operations — no side-effects.
     */
    protected static final Set<String> SAFE_TOOLS = Set.of(
            // DMO
            "d360_dmo_list", "d360_dmo_get",
            // DMO Mapping
            "d360_dmo_mapping_list", "d360_dmo_mapping_get",
            // Segment
            "d360_segment_list", "d360_segment_get",
            // Calculated Insight
            "d360_ci_list", "d360_ci_get", "d360_ci_run_status",
            // Activation
            "d360_activation_list", "d360_activation_get",
            // Activation Target
            "d360_activation_target_list", "d360_activation_target_get",
            // Data Action
            "d360_dataaction_list", "d360_dataaction_get",
            // Data Action Target
            "d360_dataaction_target_list", "d360_dataaction_target_get",
            // Identity Resolution
            "d360_ir_list", "d360_ir_get",
            // Data Stream
            "d360_datastream_list", "d360_datastream_get",
            // Data Space
            "d360_dataspace_list", "d360_dataspace_get", "d360_dataspace_member_list",
            // Connection
            "d360_connection_list", "d360_connection_get", "d360_connection_endpoints",
            // Data Transform
            "d360_transform_list", "d360_transform_get", "d360_transform_schedule_get",
            // DataKit
            "d360_datakit_list", "d360_datakit_get",
            "d360_datakit_component_status", "d360_datakit_component_deps",
            "d360_datakit_deploy_status", "d360_datakit_manifest",
            // SDM
            "d360_sdm_list", "d360_sdm_get",
            "d360_sdm_data_objects_list", "d360_sdm_data_object_get",
            "d360_sdm_dimensions_list", "d360_sdm_measurements_list",
            "d360_sdm_relationships_list", "d360_sdm_relationship_get",
            "d360_sdm_metrics_list", "d360_sdm_metric_get",
            "d360_sdm_calc_dims_list", "d360_sdm_calc_dim_get",
            "d360_sdm_calc_measures_list", "d360_sdm_calc_measure_get",
            "d360_sdm_formula_metadata", "d360_sdm_permissions", "d360_sdm_dependencies",
            // Connector
            "d360_connector_list", "d360_connector_metadata",
            // Metadata
            "d360_metadata", "d360_metadata_entities", "d360_metadata_search",
            // Query
            "d360_query_sql", "d360_query_sql_status", "d360_query_sql_rows",
            "d360_query", "d360_query_v2",
            // Profile
            "d360_profile_query", "d360_profile_metadata",
            // Insights
            "d360_insights_query", "d360_insights_metadata",
            // Data Graph
            "d360_datagraph_query", "d360_datagraph_lookup", "d360_datagraph_metadata",
            // GDPR
            "d360_gdpr_read", "d360_gdpr_bulk_read",
            // Misc read-only helpers
            "d360_analyze_event_date"
    );

    // ── Dangerous tools (explicitly excluded) ────────────────────────────────
    // All *_create, *_update, *_delete, *_run, *_publish, *_enable, *_disable
    // tools are NOT in SAFE_TOOLS and will be rejected by assertSafeTool().

    // ── Shared HTTP client & config ──────────────────────────────────────────

    protected static HttpClient httpClient;
    protected static final Map<String, String> testConfig = new HashMap<>();

    /** MCP server base URL, e.g. {@code http://localhost:8090/mcp}. */
    protected static String mcpServerUrl;

    @BeforeAll
    static void initIntegrationBase() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        loadConfig();
        mcpServerUrl = testConfig.get("MCP_SERVER_URL");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Assert that the given tool name is in the SAFE_TOOLS allowlist.
     *
     * @throws IllegalArgumentException if the tool is not safe (write/mutate operation)
     */
    protected static void assertSafeTool(String toolName) {
        if (!SAFE_TOOLS.contains(toolName)) {
            throw new IllegalArgumentException(
                    "Tool '" + toolName + "' is NOT in the SAFE_TOOLS allowlist. "
                            + "Integration tests may only invoke read-only operations.");
        }
    }

    /**
     * Send a JSON-RPC style request to the MCP server to call a tool.
     *
     * @param toolName   the Data 360 tool name (must be in SAFE_TOOLS)
     * @param arguments  JSON object string of tool arguments
     * @return the HTTP response body as a string
     */
    protected String callTool(String toolName, String arguments)
            throws IOException, InterruptedException {
        assertSafeTool(toolName);

        String body = """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "tools/call",
                  "params": {
                    "name": "%s",
                    "arguments": %s
                  }
                }
                """.formatted(toolName, arguments);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mcpServerUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Check whether the MCP server is reachable at the configured URL.
     */
    protected static boolean isServerAvailable() {
        if (mcpServerUrl == null || mcpServerUrl.isBlank()) {
            return false;
        }
        try {
            // Derive a health URL from the MCP URL (replace /mcp with /health)
            String healthUrl = mcpServerUrl.replace("/mcp", "/health");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> resp = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Config loading ───────────────────────────────────────────────────────

    /**
     * Load test configuration from environment variables first, then fall back
     * to a {@code .env.test} file in the project root.
     */
    private static void loadConfig() {
        // Environment variables take precedence
        String[] keys = {
                "MCP_SERVER_URL",
                "DATA360_INSTANCE_URL",
                "DATA360_ACCESS_TOKEN",
                "DATA360_CLIENT_ID",
                "DATA360_CLIENT_SECRET"
        };
        for (String key : keys) {
            String val = System.getenv(key);
            if (val == null) {
                val = System.getProperty(key);
            }
            if (val != null) {
                testConfig.put(key, val);
            }
        }

        // Fall back to .env.test file if it exists
        Path envFile = Path.of(".env.test");
        if (Files.exists(envFile)) {
            try (BufferedReader reader = Files.newBufferedReader(envFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String k = line.substring(0, eq).strip();
                        String v = line.substring(eq + 1).strip();
                        // Don't override env vars
                        testConfig.putIfAbsent(k, v);
                    }
                }
            } catch (IOException e) {
                // Silently ignore — .env.test is optional
            }
        }
    }
}
