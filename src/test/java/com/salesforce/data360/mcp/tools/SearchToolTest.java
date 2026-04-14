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

import com.salesforce.data360.mcp.runtime.FamilyCatalog;
import com.salesforce.data360.mcp.runtime.SearchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchToolTest {

    @Mock
    private FamilyCatalog catalog;

    @Mock
    private SearchStrategy searchStrategy;

    private SearchTool searchTool;

    @BeforeEach
    void setUp() {
        searchTool = new SearchTool(catalog, searchStrategy);
    }

    @Test
    void search_returnsRankedFamilies() {
        // Given
        String query = "how to query data";

        FamilyCatalog.ToolInfo queryTool1 = new FamilyCatalog.ToolInfo(
            "d360_query_sql",
            "Query",
            "Execute SQL query",
            null
        );
        FamilyCatalog.ToolInfo queryTool2 = new FamilyCatalog.ToolInfo(
            "d360_query_metadata",
            "Query",
            "Get query metadata",
            null
        );
        FamilyCatalog.FamilyEntry queryFamily = new FamilyCatalog.FamilyEntry(
            "Query",
            "Execute SQL queries against Data 360",
            List.of(queryTool1, queryTool2)
        );

        FamilyCatalog.ToolInfo mappingTool = new FamilyCatalog.ToolInfo(
            "d360_mappings_create",
            "Mappings",
            "Create field mapping",
            null
        );
        FamilyCatalog.FamilyEntry mappingsFamily = new FamilyCatalog.FamilyEntry(
            "Mappings",
            "Map source fields to target DMO fields",
            List.of(mappingTool)
        );

        List<SearchStrategy.ScoredFamily> scoredFamilies = List.of(
            new SearchStrategy.ScoredFamily("Query", 0.95),
            new SearchStrategy.ScoredFamily("Mappings", 0.30)
        );

        when(searchStrategy.search(anyString(), anyList(), anyInt())).thenReturn(scoredFamilies);
        when(catalog.getAllFamilies()).thenReturn(List.of(queryFamily, mappingsFamily));
        when(catalog.getFamily("Query")).thenReturn(queryFamily);
        when(catalog.getFamily("Mappings")).thenReturn(mappingsFamily);

        // When
        Map<String, Object> result = searchTool.invoke(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("query")).isEqualTo(query);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).hasSize(2);

        Map<String, Object> firstResult = results.get(0);
        assertThat(firstResult.get("family")).isEqualTo("Query");
        assertThat(firstResult.get("summary")).isEqualTo("Execute SQL queries against Data 360");

        @SuppressWarnings("unchecked")
        List<String> firstTools = (List<String>) firstResult.get("tools");
        assertThat(firstTools).containsExactly("d360_query_sql", "d360_query_metadata");

        Map<String, Object> secondResult = results.get(1);
        assertThat(secondResult.get("family")).isEqualTo("Mappings");

        @SuppressWarnings("unchecked")
        List<String> secondTools = (List<String>) secondResult.get("tools");
        assertThat(secondTools).containsExactly("d360_mappings_create");
    }

    @Test
    void search_noResults_returnsEmptyList() {
        // Given
        String query = "nonexistent feature";

        when(searchStrategy.search(anyString(), anyList(), anyInt())).thenReturn(List.of());
        when(catalog.getAllFamilies()).thenReturn(List.of());

        // When
        Map<String, Object> result = searchTool.invoke(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("query")).isEqualTo(query);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).isEmpty();
    }

    @Test
    void search_scoredFamilyNotInCatalog_skipped() {
        // Given
        String query = "test query";

        List<SearchStrategy.ScoredFamily> scoredFamilies = List.of(
            new SearchStrategy.ScoredFamily("Query", 0.95),
            new SearchStrategy.ScoredFamily("NonExistent", 0.50)
        );

        FamilyCatalog.ToolInfo queryTool = new FamilyCatalog.ToolInfo(
            "d360_query_sql",
            "Query",
            "Execute SQL query",
            null
        );
        FamilyCatalog.FamilyEntry queryFamily = new FamilyCatalog.FamilyEntry(
            "Query",
            "Execute SQL queries",
            List.of(queryTool)
        );

        when(searchStrategy.search(anyString(), anyList(), anyInt())).thenReturn(scoredFamilies);
        when(catalog.getAllFamilies()).thenReturn(List.of(queryFamily));
        when(catalog.getFamily("Query")).thenReturn(queryFamily);
        when(catalog.getFamily("NonExistent")).thenReturn(null);

        // When
        Map<String, Object> result = searchTool.invoke(query);

        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("family")).isEqualTo("Query");
    }
}
