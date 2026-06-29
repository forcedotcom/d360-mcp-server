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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalizationToolsTest {

    @Mock
    private Data360Client client;

    private PersonalizationTools personalizationTools;

    @BeforeEach
    void setUp() {
        personalizationTools = new PersonalizationTools(client);
    }

    // ============================================================
    // List Engagement Signals
    // ============================================================

    @Test
    void testListEngagementSignals_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "engagementSignals", List.of(
                Map.of("id", "sig-001", "name", "Click", "type", "CLICK"),
                Map.of("id", "sig-002", "name", "Purchase", "type", "PURCHASE")
            ),
            "totalSize", 2
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.listEngagementSignals("ds-123", "dg-456");

        // Then
        assertThat(result).contains("sig-001", "Click", "CLICK");
        assertThat(result).contains("sig-002", "Purchase", "PURCHASE");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).startsWith("/personalization/engagement-signals");
        assertThat(capturedPath).contains("dataSpaceId=ds-123");
        assertThat(capturedPath).contains("dataGraphId=dg-456");
    }

    @Test
    void testListEngagementSignals_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(403, "Insufficient permissions", "/personalization/engagement-signals"));

        // When
        String result = personalizationTools.listEngagementSignals("ds-123", "dg-456");

        // Then
        assertThat(result).contains("error", "403");
    }

    @Test
    void testListEngagementSignals_connectionError() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException("Connection timeout", new RuntimeException("timeout")));

        // When
        String result = personalizationTools.listEngagementSignals("ds-123", "dg-456");

        // Then
        assertThat(result).contains("error", "Connection timeout");
    }

    @Test
    void testListEngagementSignals_emptyResult() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "engagementSignals", List.of(),
            "totalSize", 0
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.listEngagementSignals("ds-empty", "dg-empty");

        // Then
        assertThat(result).contains("engagementSignals");
        assertThat(result).contains("\"totalSize\":0");
    }

    @Test
    void testListEngagementSignals_notFound() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Data graph not found", "/personalization/engagement-signals"));

        // When
        String result = personalizationTools.listEngagementSignals("ds-123", "dg-nonexistent");

        // Then
        assertThat(result).contains("error", "404");
    }

    // ============================================================
    // Get Org Info
    // ============================================================

    @Test
    void testGetOrgInfo_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "dcTse", "https://dc-tse.my.salesforce.com"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.getOrgInfo();

        // Then
        assertThat(result).contains("dcTse", "https://dc-tse.my.salesforce.com");

        verify(client).get(eq("/personalization/external-apps/org"), eq(Map.class));
    }

    @Test
    void testGetOrgInfo_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal server error", "/personalization/external-apps/org"));

        // When
        String result = personalizationTools.getOrgInfo();

        // Then
        assertThat(result).contains("error", "500");
    }

    // ============================================================
    // Create Mobile Preview
    // ============================================================

    @Test
    void testCreateMobileLivePreviewLink_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "previewUrl", "https://preview.mobile.app/abc123",
            "expiresAt", "2026-06-12T10:00:00Z"
        );

        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        ExtAppPreviewInputRepresentation request = new ExtAppPreviewInputRepresentation();
        request.setDataConnectorId("conn-123");

        // When
        String result = personalizationTools.createMobileLivePreviewLink(request);

        // Then
        assertThat(result).contains("previewUrl", "https://preview.mobile.app/abc123");
        assertThat(result).contains("expiresAt");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(eq("/personalization/external-apps/mobile/live-preview-link"), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).containsKey("dataConnectorId");
    }

    @Test
    void testCreateMobileLivePreviewLink_errorHandling() {
        // Given
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid request", "/personalization/external-apps/mobile/live-preview-link"));

        ExtAppPreviewInputRepresentation request = new ExtAppPreviewInputRepresentation();
        request.setDataConnectorId("invalid");

        // When
        String result = personalizationTools.createMobileLivePreviewLink(request);

        // Then
        assertThat(result).contains("error", "400");
    }

    // ============================================================
    // List Experience Configs
    // ============================================================

    @Test
    void testListExperienceConfigs_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "personalizationExperienceConfigs", List.of(
                Map.of("id", "exp-001", "name", "homepage_banner"),
                Map.of("id", "exp-002", "name", "product_recommendations")
            ),
            "totalSize", 2
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.listExperienceConfigs("my-connector", 10, 0, null, null, null, null);

        // Then
        assertThat(result).contains("exp-001", "homepage_banner");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/my-connector/personalization-experience-configs");
        assertThat(capturedPath).contains("limit=10");
        assertThat(capturedPath).contains("offset=0");
    }

    @Test
    void testListExperienceConfigs_withFilters() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(Map.of("personalizationExperienceConfigs", List.of()));

        // When
        String result = personalizationTools.listExperienceConfigs(
            "my-connector", 50, 10, "pp-001", "trans-001", "ds-001", true
        );

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("personalizationPointNameOrId=pp-001");
        assertThat(capturedPath).contains("transformerNameOrId=trans-001");
        assertThat(capturedPath).contains("dataSpaceIdOrName=ds-001");
        assertThat(capturedPath).contains("isWpmUrlRequired=true");
    }

    // ============================================================
    // Get Experience Config
    // ============================================================

    @Test
    void testGetExperienceConfig_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "exp-001",
            "name", "homepage_banner",
            "label", "Homepage Banner",
            "isEnabled", true
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.getExperienceConfig("my-connector", "homepage_banner", false);

        // Then
        assertThat(result).contains("exp-001", "homepage_banner");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/my-connector/personalization-experience-configs/homepage_banner");
    }

    // ============================================================
    // Create Experience Config
    // ============================================================

    @Test
    void testCreateExperienceConfig_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "exp-new",
            "name", "new_experience",
            "isEnabled", true
        );

        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        PersonalizationExperienceConfigInputRepresentation request = new PersonalizationExperienceConfigInputRepresentation();
        request.setName("new_experience");
        request.setLabel("New Experience");

        // When
        String result = personalizationTools.createExperienceConfig("my-connector", request, false);

        // Then
        assertThat(result).contains("exp-new", "new_experience");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/my-connector/personalization-experience-configs");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "new_experience");
    }

    @Test
    void testCreateExperienceConfig_withDisableRelated() {
        // Given
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(Map.of("id", "exp-new"));

        PersonalizationExperienceConfigInputRepresentation request = new PersonalizationExperienceConfigInputRepresentation();
        request.setName("new_experience");

        // When
        personalizationTools.createExperienceConfig("my-connector", request, true);

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), anyMap(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("disableRelatedExperiences=true");
    }

    // ============================================================
    // Update Experience Config
    // ============================================================

    @Test
    void testUpdateExperienceConfig_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "exp-001",
            "name", "homepage_banner",
            "isEnabled", false
        );

        when(client.put(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        PersonalizationExperienceConfigInputRepresentation request = new PersonalizationExperienceConfigInputRepresentation();
        request.setIsEnabled(false);

        // When
        String result = personalizationTools.updateExperienceConfig("my-connector", "homepage_banner", request, null);

        // Then
        assertThat(result).contains("exp-001", "\"isEnabled\":false");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).put(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/my-connector/personalization-experience-configs/homepage_banner");
        assertThat(bodyCaptor.getValue()).containsEntry("isEnabled", false);
    }

    // ============================================================
    // Delete Experience Config
    // ============================================================

    @Test
    void testDeleteExperienceConfig_success() {
        // Given
        doNothing().when(client).delete(anyString());

        // When
        String result = personalizationTools.deleteExperienceConfig("my-connector", "homepage_banner");

        // Then
        assertThat(result).contains("deleted", "homepage_banner");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture());

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/my-connector/personalization-experience-configs/homepage_banner");
    }

    // ============================================================
    // List Transformers
    // ============================================================

    @Test
    void testListTransformers_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "transformers", List.of(
                Map.of("id", "trans-001", "name", "banner_transformer"),
                Map.of("id", "trans-002", "name", "card_transformer")
            ),
            "totalSize", 2
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.listTransformers(
            50, 0, null, null, null, null, null, null, null, null, null
        );

        // Then
        assertThat(result).contains("trans-001", "banner_transformer");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/transformers");
        assertThat(capturedPath).contains("limit=50");
    }

    @Test
    void testListTransformers_withAllFilters() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(Map.of("transformers", List.of()));

        // When
        personalizationTools.listTransformers(
            100, 20, "Organization", "schema-001", "EmbeddedContent",
            "WebApp", "Handlebars", true, "conn-001", "ds-001", false
        );

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("source=Organization");
        assertThat(capturedPath).contains("schemaReference=schema-001");
        assertThat(capturedPath).contains("transformerCategory=EmbeddedContent");
        assertThat(capturedPath).contains("channelType=WebApp");
        assertThat(capturedPath).contains("transformerType=Handlebars");
        assertThat(capturedPath).contains("isEnabled=true");
        assertThat(capturedPath).contains("connectorNameOrId=conn-001");
        assertThat(capturedPath).contains("dataSpaceIdOrName=ds-001");
        assertThat(capturedPath).contains("isDataConnectorAttached=false");
    }

    // ============================================================
    // Get Transformer
    // ============================================================

    @Test
    void testGetTransformer_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "trans-001",
            "name", "banner_transformer",
            "transformerType", "Handlebars",
            "isEnabled", true
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.getTransformer("banner_transformer");

        // Then
        assertThat(result).contains("trans-001", "banner_transformer");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/transformer");
        assertThat(capturedPath).contains("idOrName=banner_transformer");
    }

    // ============================================================
    // Create Transformer
    // ============================================================

    @Test
    void testCreateTransformer_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "trans-new",
            "name", "new_transformer",
            "transformerType", "Handlebars"
        );

        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        TransformerInputRepresentation request = new TransformerInputRepresentation();
        request.setName("new_transformer");
        request.setTransformerType("Handlebars");

        // When
        String result = personalizationTools.createTransformer(request);

        // Then
        assertThat(result).contains("trans-new", "new_transformer");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(eq("/personalization/external-apps/transformers"), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).containsEntry("name", "new_transformer");
        assertThat(bodyCaptor.getValue()).containsEntry("transformerType", "Handlebars");
    }

    // ============================================================
    // Update Transformer
    // ============================================================

    @Test
    void testUpdateTransformer_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "trans-001",
            "name", "banner_transformer",
            "isEnabled", false
        );

        when(client.put(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        TransformerInputRepresentation request = new TransformerInputRepresentation();
        request.setIsEnabled(false);

        // When
        String result = personalizationTools.updateTransformer("banner_transformer", request);

        // Then
        assertThat(result).contains("trans-001", "\"isEnabled\":false");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).put(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/transformer");
        assertThat(capturedPath).contains("idOrName=banner_transformer");
        assertThat(bodyCaptor.getValue()).containsEntry("isEnabled", false);
    }

    // ============================================================
    // Delete Transformer
    // ============================================================

    @Test
    void testDeleteTransformer_success() {
        // Given
        doNothing().when(client).delete(anyString());

        // When
        String result = personalizationTools.deleteTransformer("banner_transformer");

        // Then
        assertThat(result).contains("deleted", "banner_transformer");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture());

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/external-apps/transformer");
        assertThat(capturedPath).contains("idOrName=banner_transformer");
    }

    // ============================================================
    // Create Personalization Schema
    // ============================================================

    @Test
    void testCreatePersonalizationSchema_success() {
        // Given
        String requestJson = """
            {
              "name": "product_recommendations",
              "label": "Product Recommendations",
              "description": "Schema for product recommendation content",
              "dataSpaceName": "default",
              "personalizationType": "Recommendations",
              "attributes": [
                {
                  "name": "recommendation_type",
                  "label": "Recommendation Type",
                  "defaultValue": "similar_items"
                }
              ],
              "contentObject": {
                "name": "Product__dll",
                "fieldNames": ["name", "description", "price"]
              }
            }
            """;

        Map<String, Object> mockResponse = Map.of(
            "id", "schema-001",
            "name", "product_recommendations",
            "label", "Product Recommendations",
            "personalizationType", "Recommendations"
        );

        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.createPersonalizationSchema(requestJson);

        // Then
        assertThat(result).contains("schema-001", "product_recommendations");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), anyMap(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).isEqualTo("/personalization/personalization-schemas");
    }

    @Test
    void testCreatePersonalizationSchema_errorHandling() {
        // Given
        String requestJson = """
            {
              "name": "test_schema",
              "label": "Test Schema"
            }
            """;

        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid request", "/personalization/personalization-schemas"));

        // When
        String result = personalizationTools.createPersonalizationSchema(requestJson);

        // Then
        assertThat(result).contains("error", "400");
    }

    // ============================================================
    // Get Personalization Schema
    // ============================================================

    @Test
    void testGetPersonalizationSchema_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "schema-001",
            "name", "product_recommendations",
            "label", "Product Recommendations",
            "personalizationType", "Recommendations",
            "attributes", List.of(
                Map.of("name", "recommendation_type", "label", "Recommendation Type")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.getPersonalizationSchema("product_recommendations");

        // Then
        assertThat(result).contains("schema-001", "product_recommendations");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-schemas/product_recommendations");
    }

    @Test
    void testGetPersonalizationSchema_byId() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "schema-001");

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        personalizationTools.getPersonalizationSchema("schema-001");

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-schemas/schema-001");
    }

    // ============================================================
    // Update Personalization Schema
    // ============================================================

    @Test
    void testUpdatePersonalizationSchema_success() {
        // Given
        String requestJson = """
            {
              "label": "Updated Product Recommendations",
              "description": "Updated description"
            }
            """;

        Map<String, Object> mockResponse = Map.of(
            "id", "schema-001",
            "name", "product_recommendations",
            "label", "Updated Product Recommendations"
        );

        when(client.patch(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.updatePersonalizationSchema("product_recommendations", requestJson);

        // Then
        assertThat(result).contains("schema-001", "Updated Product Recommendations");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).patch(pathCaptor.capture(), anyMap(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-schemas/product_recommendations");
    }

    @Test
    void testUpdatePersonalizationSchema_errorHandling() {
        // Given
        String requestJson = "{}";

        when(client.patch(anyString(), anyMap(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Schema not found", "/personalization/personalization-schemas/unknown"));

        // When
        String result = personalizationTools.updatePersonalizationSchema("unknown", requestJson);

        // Then
        assertThat(result).contains("error", "404");
    }

    // ============================================================
    // Delete Personalization Schema
    // ============================================================

    @Test
    void testDeletePersonalizationSchema_success() {
        // Given
        doNothing().when(client).delete(anyString());

        // When
        String result = personalizationTools.deletePersonalizationSchema("product_recommendations");

        // Then
        assertThat(result).contains("deleted", "product_recommendations");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture());

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-schemas/product_recommendations");
    }

    @Test
    void testDeletePersonalizationSchema_errorHandling() {
        // Given
        doThrow(new ApiException(403, "Permission denied", "/personalization/personalization-schemas/product_recommendations"))
            .when(client).delete(anyString());

        // When
        String result = personalizationTools.deletePersonalizationSchema("product_recommendations");

        // Then
        assertThat(result).contains("error", "403");
    }

    // ============================================================
    // Create Personalization Point
    // ============================================================

    @Test
    void testCreatePersonalizationPoint_success() {
        // Given
        String requestJson = """
            {
              "name": "home_page_banner",
              "label": "Home Page Banner",
              "description": "Personalization point for homepage banner",
              "dataSpaceName": "default",
              "profileDataGraphName": "profile_graph",
              "source": "PersonalizationApp",
              "isAuthenticationRequired": true,
              "schemaName": "product_recommendations_schema",
              "maxItemsCount": 5,
              "decisions": [
                {
                  "name": "premium_users",
                  "label": "Premium Users",
                  "criteria": "{\\"type\\":\\"user_segment\\"}",
                  "attributeValues": [
                    {
                      "attributeName": "recommendation_type",
                      "value": "premium"
                    }
                  ],
                  "personalizerName": "premium_recommender"
                }
              ]
            }
            """;

        Map<String, Object> mockResponse = Map.of(
            "id", "point-001",
            "name", "home_page_banner",
            "label", "Home Page Banner",
            "status", "Active"
        );

        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.createPersonalizationPoint(requestJson);

        // Then
        assertThat(result).contains("point-001", "home_page_banner");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), anyMap(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).isEqualTo("/personalization/personalization-points");
    }

    @Test
    void testCreatePersonalizationPoint_errorHandling() {
        // Given
        String requestJson = """
            {
              "name": "test_point",
              "label": "Test Point"
            }
            """;

        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Missing required field: dataSpaceName", "/personalization/personalization-points"));

        // When
        String result = personalizationTools.createPersonalizationPoint(requestJson);

        // Then
        assertThat(result).contains("error", "400");
    }

    // ============================================================
    // Get Personalization Point
    // ============================================================

    @Test
    void testGetPersonalizationPoint_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "point-001",
            "name", "home_page_banner",
            "label", "Home Page Banner",
            "status", "Active",
            "dataSpaceName", "default",
            "decisions", List.of(
                Map.of("name", "premium_users", "label", "Premium Users")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.getPersonalizationPoint("home_page_banner");

        // Then
        assertThat(result).contains("point-001", "home_page_banner", "Active");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-points/home_page_banner");
    }

    @Test
    void testGetPersonalizationPoint_byId() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "point-001");

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        personalizationTools.getPersonalizationPoint("point-001");

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-points/point-001");
    }

    @Test
    void testGetPersonalizationPoint_notFound() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Personalization Point not found", "/personalization/personalization-points/unknown"));

        // When
        String result = personalizationTools.getPersonalizationPoint("unknown");

        // Then
        assertThat(result).contains("error", "404");
    }

    // ============================================================
    // Update Personalization Point
    // ============================================================

    @Test
    void testUpdatePersonalizationPoint_success() {
        // Given
        String requestJson = """
            {
              "name": "home_page_banner",
              "label": "Updated Home Page Banner",
              "description": "Updated description",
              "dataSpaceName": "default",
              "profileDataGraphName": "profile_graph",
              "source": "PersonalizationApp",
              "isAuthenticationRequired": false,
              "decisions": [
                {
                  "name": "all_users",
                  "label": "All Users",
                  "criteria": "{\\"type\\":\\"default\\"}",
                  "attributeValues": [],
                  "personalizerName": "default_recommender"
                }
              ]
            }
            """;

        Map<String, Object> mockResponse = Map.of(
            "id", "point-001",
            "name", "home_page_banner",
            "label", "Updated Home Page Banner",
            "isAuthenticationRequired", false
        );

        when(client.put(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = personalizationTools.updatePersonalizationPoint("home_page_banner", requestJson);

        // Then
        assertThat(result).contains("point-001", "Updated Home Page Banner");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).put(pathCaptor.capture(), anyMap(), eq(Map.class));

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-points/home_page_banner");
    }

    @Test
    void testUpdatePersonalizationPoint_errorHandling() {
        // Given
        String requestJson = "{}";

        when(client.put(anyString(), anyMap(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid request", "/personalization/personalization-points/unknown"));

        // When
        String result = personalizationTools.updatePersonalizationPoint("unknown", requestJson);

        // Then
        assertThat(result).contains("error", "400");
    }

    // ============================================================
    // Delete Personalization Point
    // ============================================================

    @Test
    void testDeletePersonalizationPoint_success() {
        // Given
        doNothing().when(client).delete(anyString());

        // When
        String result = personalizationTools.deletePersonalizationPoint("home_page_banner");

        // Then
        assertThat(result).contains("deleted", "home_page_banner");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture());

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("/personalization/personalization-points/home_page_banner");
    }

    @Test
    void testDeletePersonalizationPoint_errorHandling() {
        // Given
        doThrow(new ApiException(404, "Personalization Point not found", "/personalization/personalization-points/unknown"))
            .when(client).delete(anyString());

        // When
        String result = personalizationTools.deletePersonalizationPoint("unknown");

        // Then
        assertThat(result).contains("error", "404");
    }
}
