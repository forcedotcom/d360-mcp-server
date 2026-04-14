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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests that exercise read-only Data 360 tools against a running MCP server.
 *
 * <p>These tests are tagged {@code "integration"} and skipped in the normal test
 * suite. Run them with:</p>
 * <pre>
 *   mvn test -Dgroups=integration -DMCP_SERVER_URL=http://localhost:8090/mcp
 * </pre>
 */
@Tag("integration")
public class ReadOnlyIntegrationTest extends IntegrationTestBase {

    @BeforeAll
    static void checkServer() {
        assumeTrue(mcpServerUrl != null && !mcpServerUrl.isBlank(),
                "MCP_SERVER_URL not configured — skipping integration tests");
        assumeTrue(isServerAvailable(),
                "MCP server not reachable at " + mcpServerUrl + " — skipping integration tests");
    }

    // ── Metadata ─────────────────────────────────────────────────────────────

    @Test
    void metadata_returnsResponse() throws Exception {
        String response = callTool("d360_metadata", "{}");

        assertNotNull(response);
        assertFalse(response.isBlank(), "Metadata response should not be blank");
    }

    @Test
    void metadataEntities_returnsResponse() throws Exception {
        String response = callTool("d360_metadata_entities", "{}");

        assertNotNull(response);
        assertFalse(response.isBlank(), "Metadata entities response should not be blank");
    }

    // ── Safety guard ─────────────────────────────────────────────────────────

    @Test
    void assertSafeTool_rejectsWriteTools() {
        assertThrows(IllegalArgumentException.class,
                () -> assertSafeTool("d360_dmo_create"));
        assertThrows(IllegalArgumentException.class,
                () -> assertSafeTool("d360_segment_delete"));
        assertThrows(IllegalArgumentException.class,
                () -> assertSafeTool("d360_segment_publish"));
        assertThrows(IllegalArgumentException.class,
                () -> assertSafeTool("d360_ci_enable"));
        assertThrows(IllegalArgumentException.class,
                () -> assertSafeTool("d360_ci_run"));
        assertThrows(IllegalArgumentException.class,
                () -> assertSafeTool("d360_transform_run"));
    }

    @Test
    void assertSafeTool_allowsReadTools() {
        assertDoesNotThrow(() -> assertSafeTool("d360_dmo_list"));
        assertDoesNotThrow(() -> assertSafeTool("d360_segment_get"));
        assertDoesNotThrow(() -> assertSafeTool("d360_metadata"));
        assertDoesNotThrow(() -> assertSafeTool("d360_query_sql"));
    }
}
