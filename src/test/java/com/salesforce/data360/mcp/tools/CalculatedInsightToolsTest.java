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
import com.salesforce.data360.mcp.model.request.calculatedinsight.CalculatedInsightCreateRequest;
import com.salesforce.data360.mcp.model.request.calculatedinsight.CalculatedInsightUpdateRequest;
import com.salesforce.data360.mcp.model.request.calculatedinsight.CalculatedInsightValidateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.salesforce.data360.mcp.tools.TestConstants.DEFAULT_DATASPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculatedInsightToolsTest {

    @Mock
    private Data360Client client;

    private CalculatedInsightTools calculatedInsightTools;

    @BeforeEach
    void setUp() {
        calculatedInsightTools = new CalculatedInsightTools(client);
    }

    @Test
    void testListCalculatedInsights_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(
                Map.of("apiName", "TotalRevenue__cio", "displayName", "Total Revenue"),
                Map.of("apiName", "CustomerCount__cio", "displayName", "Customer Count")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.listCalculatedInsights(null, null, null, null, null, null);

        // Then
        assertThat(result).contains("TotalRevenue__cio", "CustomerCount__cio");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights");
    }

    @Test
    void testGetCalculatedInsight_success() {
        // Given
        String ciName = "TotalRevenue__cio";
        Map<String, Object> mockResponse = Map.of(
            "apiName", "TotalRevenue__cio",
            "displayName", "Total Revenue",
            "definitionType", "CALCULATED_METRIC",
            "expression", "SELECT SUM(amount) FROM Sales__dlm"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.getCalculatedInsight(ciName);

        // Then
        assertThat(result).contains("TotalRevenue__cio", "Total Revenue", "CALCULATED_METRIC");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/" + ciName);
    }

    @Test
    void testCreateCalculatedInsight_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "ci-123",
            "apiName", "NewMetric__cio"
        );

        // First call to check existence returns 404
        when(client.get(eq("/ssot/calculated-insights/NewMetric__cio"), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/calculated-insights/NewMetric__cio"));

        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        CalculatedInsightCreateRequest request = new CalculatedInsightCreateRequest();
        request.setApiName("NewMetric__cio");
        request.setDisplayName("New Metric");
        request.setDefinitionType("CALCULATED_METRIC");
        request.setExpression("SELECT COUNT(*) FROM MyDmo__dlm");
        request.setPublishScheduleInterval("SYSTEM_MANAGED");

        String result = calculatedInsightTools.createCalculatedInsight(request, null);

        // Then
        assertThat(result).contains("ci-123", "NewMetric__cio");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights");
        assertThat(bodyCaptor.getValue())
            .containsEntry("apiName", "NewMetric__cio")
            .containsEntry("displayName", "New Metric")
            .containsEntry("definitionType", "CALCULATED_METRIC");
    }

    @Test
    void testCreateCalculatedInsight_alreadyExists() {
        // Given
        Map<String, Object> existingResponse = Map.of(
            "apiName", "ExistingMetric__cio",
            "displayName", "Existing Metric"
        );

        when(client.get(eq("/ssot/calculated-insights/ExistingMetric__cio"), eq(Map.class)))
            .thenReturn(existingResponse);

        // When
        CalculatedInsightCreateRequest request = new CalculatedInsightCreateRequest();
        request.setApiName("ExistingMetric__cio");
        request.setDisplayName("Existing Metric");
        request.setDefinitionType("CALCULATED_METRIC");
        request.setExpression("SELECT 1");
        request.setPublishScheduleInterval("SYSTEM_MANAGED");

        String result = calculatedInsightTools.createCalculatedInsight(request, null);

        // Then
        assertThat(result).contains("ExistingMetric__cio", "_alreadyExisted", "true");

        // Verify POST was never called
        verify(client, never()).post(anyString(), any(Map.class), eq(Map.class));
    }

    @Test
    void testUpdateCalculatedInsight_success() {
        // Given
        String ciName = "MyMetric__cio";
        Map<String, Object> mockResponse = Map.of(
            "apiName", "MyMetric__cio",
            "displayName", "Updated Metric"
        );

        when(client.patch(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        CalculatedInsightUpdateRequest request = new CalculatedInsightUpdateRequest();
        request.setDisplayName("Updated Metric");

        String result = calculatedInsightTools.updateCalculatedInsight(ciName, request, null);

        // Then
        assertThat(result).contains("Updated Metric");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/" + ciName);
        assertThat(bodyCaptor.getValue()).containsEntry("displayName", "Updated Metric");
    }

    @Test
    void testDeleteCalculatedInsight_success() {
        // Given
        String ciName = "MyMetric__cio";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.deleteCalculatedInsight(ciName, null);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/" + ciName);
    }

    @Test
    void testRunCalculatedInsight_success() {
        // Given
        String ciName = "MyMetric__cio";
        Map<String, Object> mockResponse = Map.of(
            "jobId", "job-789",
            "status", "running"
        );

        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.runCalculatedInsight(ciName);

        // Then
        assertThat(result).contains("job-789", "running");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/" + ciName + "/actions/run");
    }

    @Test
    void testGetCalculatedInsightRunStatus_success() {
        // Given
        String ciName = "MyMetric__cio";
        Map<String, Object> mockResponse = Map.of(
            "jobId", "job-789",
            "status", "completed",
            "progress", 100
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.getCalculatedInsightRunStatus(ciName);

        // Then
        assertThat(result).contains("job-789", "completed", "100");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/" + ciName + "/actions/refresh-status");
    }

    @Test
    void testQueryInsights_success() {
        // Given
        String ciName = "MyMetric__cio";
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(
                Map.of("dimension1", "value1", "measure1", 100),
                Map.of("dimension1", "value2", "measure1", 200)
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.queryInsights(
            ciName, "dimension1", "measure1", null, null, 10, 0, null, null
        );

        // Then
        assertThat(result).contains("dimension1", "measure1");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue())
            .contains("/ssot/insight/calculated-insights/" + ciName)
            .contains("dimensions=dimension1")
            .contains("measures=measure1")
            .contains("batchSize=10")
            .contains("offset=0");
    }

    @Test
    void testQueryInsights_withFilters() {
        // Given
        String ciName = "MyMetric__cio";
        Map<String, Object> mockResponse = Map.of("data", List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.queryInsights(
            ciName, null, null, "field1 DESC", "field1 > 100", null, null, "DAY", DEFAULT_DATASPACE
        );

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue())
            .contains("orderby=field1+DESC")
            .contains("filters=field1+%3E+100")
            .contains("timeGranularity=DAY")
            .contains("dataspace=default");
    }

    @Test
    void testGetInsightsMetadata_allCIs() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "calculatedInsights", List.of(
                Map.of("apiName", "CI1__cio"),
                Map.of("apiName", "CI2__cio")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.getInsightsMetadata(null, null);

        // Then
        assertThat(result).contains("CI1__cio", "CI2__cio");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/insight/metadata");
    }

    @Test
    void testGetInsightsMetadata_specificCI() {
        // Given
        String ciName = "MyMetric__cio";
        Map<String, Object> mockResponse = Map.of(
            "apiName", "MyMetric__cio",
            "dimensions", List.of("dim1", "dim2"),
            "measures", List.of("measure1", "measure2")
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = calculatedInsightTools.getInsightsMetadata(ciName, null);

        // Then
        assertThat(result).contains("MyMetric__cio", "dimensions", "measures");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/insight/metadata/" + ciName);
    }

    @Test
    void testListCalculatedInsights_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal server error", "/ssot/calculated-insights"));

        // When
        String result = calculatedInsightTools.listCalculatedInsights(null, null, null, null, null, null);

        // Then
        assertThat(result).contains("error", "Internal server error", "500");
    }

    @Test
    void testGetCalculatedInsight_notFound() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "CI not found", "/ssot/calculated-insights/NonExistent__cio"));

        // When
        String result = calculatedInsightTools.getCalculatedInsight("NonExistent__cio");

        // Then
        assertThat(result).contains("error", "CI not found", "404");
    }

    @Test
    void testCreateCalculatedInsight_withShouldCommitTransaction() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "ci-456");

        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/calculated-insights/NewMetric__cio"));
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        CalculatedInsightCreateRequest request = new CalculatedInsightCreateRequest();
        request.setApiName("NewMetric__cio");
        request.setDisplayName("New Metric");
        request.setDefinitionType("CALCULATED_METRIC");
        request.setExpression("SELECT 1");
        request.setPublishScheduleInterval("SYSTEM_MANAGED");

        String result = calculatedInsightTools.createCalculatedInsight(request, true);

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("shouldCommitTransaction=true");
    }

    @Test
    void testCreateCalculatedInsight_checkExistenceError() {
        // Given - Error checking existence (not 404)
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Server error", "/ssot/calculated-insights/NewMetric__cio"));

        // When
        CalculatedInsightCreateRequest request = new CalculatedInsightCreateRequest();
        request.setApiName("NewMetric__cio");
        request.setDisplayName("New Metric");
        request.setDefinitionType("CALCULATED_METRIC");
        request.setExpression("SELECT 1");
        request.setPublishScheduleInterval("SYSTEM_MANAGED");

        String result = calculatedInsightTools.createCalculatedInsight(request, null);

        // Then - Should return error, not proceed with creation
        assertThat(result).contains("error", "Server error", "500");
        verify(client, never()).post(anyString(), any(Map.class), eq(Map.class));
    }

    @Test
    void testEnableCalculatedInsight_success() {
        Map<String, Object> mockResponse = Map.of("status", "enabled");
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(mockResponse);

        String result = calculatedInsightTools.enableCalculatedInsight("MyMetric__cio");

        assertThat(result).contains("enabled");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/MyMetric__cio/actions/enable");
    }

    @Test
    void testDisableCalculatedInsight_success() {
        Map<String, Object> mockResponse = Map.of("status", "disabled");
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(mockResponse);

        String result = calculatedInsightTools.disableCalculatedInsight("MyMetric__cio");

        assertThat(result).contains("disabled");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/MyMetric__cio/actions/disable");
    }

    @Test
    void testValidateCalculatedInsight_success() {
        Map<String, Object> mockResponse = Map.of("valid", true);
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(mockResponse);

        CalculatedInsightValidateRequest request = new CalculatedInsightValidateRequest();
        request.setExpression("SELECT SUM(amount) FROM Orders__dlm");
        String result = calculatedInsightTools.validateCalculatedInsight(request);

        assertThat(result).contains("valid");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/calculated-insights/actions/validate");
    }

    @Test
    void testEnableCalculatedInsight_error() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException(404, "CI not found", "/ssot/calculated-insights/NonExistent__cio/actions/enable"));

        String result = calculatedInsightTools.enableCalculatedInsight("NonExistent__cio");

        assertThat(result).contains("error", "404");
    }

    @Test
    void testUpdateCalculatedInsight_connectionErrorIsNotReportedAsInvalidJson() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException("Data 360 API connection error on /calculated-insights/MyMetric__cio", new RuntimeException("timeout")));

        CalculatedInsightUpdateRequest request = new CalculatedInsightUpdateRequest();
        request.setDisplayName("Updated Metric");

        String result = calculatedInsightTools.updateCalculatedInsight("MyMetric__cio", request, null);

        assertThat(result).contains("Data 360 API connection error");
        assertThat(result).doesNotContain("Invalid JSON body");
    }
}
