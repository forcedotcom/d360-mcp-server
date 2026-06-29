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
package com.salesforce.data360.mcp.tools.machinelearning.predictionjob;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.DataFieldInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.FieldRelationshipInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.InputFieldInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.MappedFieldInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SentimentDetectionPredictionJobDefToolsTest {

    private static final String DEFAULT_MODEL_NAME = "sfdc_ai__DefaultSalesforceSentimentAnalysis";
    private static final String DATA_OBJECT_NAME = "Review__dlm";
    private static final String MAPPED_FIELD_NAME = "ReviewText__c";
    private static final String SOURCE_DATA_OBJECT_NAME = "Review__dlm";
    private static final String SOURCE_FIELD_NAME = "AuthorId__c";
    private static final String TARGET_DATA_OBJECT_NAME = "Account__dlm";
    private static final String TARGET_FIELD_NAME = "Id__c";
    private static final String OUTPUT_OBJECT_NAME = "ReviewSentiment__dlm";

    @Mock
    private Data360Client client;

    private SentimentDetectionPredictionJobDefTools tools;

    @BeforeEach
    void setUp() {
        tools = new SentimentDetectionPredictionJobDefTools(client);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void create_defaultsModelToSalesforceSentiment() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "abc"));

        tools.createSentimentDetectionPredictionJobDef(
                "SentimentDetectionJob",
                ref(DATA_OBJECT_NAME),
                List.of(textField()),
                OUTPUT_OBJECT_NAME,
                null,
                "Sentiment Detection Job", "Sentiment detection prediction job",
                null, "Output description",
                "Batch");

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));

        Map<String, Object> b = body.getValue();
        assertThat(b)
                .containsEntry("type", "SentimentDetection")
                .containsEntry("apiName", "SentimentDetectionJob")
                .containsEntry("label", "Sentiment Detection Job")
                .containsEntry("scoringMode", "Batch");

        assertThat((Map<String, Object>) b.get("model"))
                .containsEntry("name", DEFAULT_MODEL_NAME);

        Map<String, Object> input = (Map<String, Object>) b.get("inputConfig");
        assertThat((Map<String, Object>) input.get("dataObject"))
                .containsEntry("name", DATA_OBJECT_NAME);

        List<Map<String, Object>> textFields = (List<Map<String, Object>>) input.get("textFields");
        assertThat(textFields).hasSize(1);
        Map<String, Object> mapped = (Map<String, Object>) textFields.get(0).get("mappedField");
        assertThat(mapped).containsEntry("name", MAPPED_FIELD_NAME);
        assertThat((List) mapped.get("relationshipPath")).hasSize(1);

        assertThat(input).doesNotContainKey("fields");
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_explicitModelOverridesDefault() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "abc"));

        tools.createSentimentDetectionPredictionJobDef(
                "x",
                ref(DATA_OBJECT_NAME),
                List.of(textField()),
                OUTPUT_OBJECT_NAME,
                ref("CustomSentimentModel"),
                null, null, null, null, null);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        assertThat((Map<String, Object>) body.getValue().get("model"))
                .containsEntry("name", "CustomSentimentModel");
    }

    @Test
    void create_emptyTextFields_returnsErrorWithoutHttpCall() {
        String result = tools.createSentimentDetectionPredictionJobDef(
                "x", ref(DATA_OBJECT_NAME), List.of(), OUTPUT_OBJECT_NAME,
                null, null, null, null, null, null);

        assertThat(result).contains("error", "textFields");
        verifyNoInteractions(client);
    }

    @Test
    void create_nullTextFields_returnsErrorWithoutHttpCall() {
        String result = tools.createSentimentDetectionPredictionJobDef(
                "x", ref(DATA_OBJECT_NAME), null, OUTPUT_OBJECT_NAME,
                null, null, null, null, null, null);

        assertThat(result).contains("error", "textFields");
        verifyNoInteractions(client);
    }

    private static AssetReferenceInput ref(String name) {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setName(name);
        return ref;
    }

    private static InputFieldInput textField() {
        InputFieldInput f = new InputFieldInput();
        MappedFieldInput mapped = new MappedFieldInput();
        mapped.setName(MAPPED_FIELD_NAME);
        mapped.setDataObject(ref(DATA_OBJECT_NAME));
        mapped.setRelationshipPath(List.of(relationship()));
        f.setMappedField(mapped);
        f.setUpdateScore(true);
        return f;
    }

    private static FieldRelationshipInput relationship() {
        FieldRelationshipInput rel = new FieldRelationshipInput();
        rel.setSourceField(dataField(SOURCE_FIELD_NAME, SOURCE_DATA_OBJECT_NAME));
        rel.setTargetField(dataField(TARGET_FIELD_NAME, TARGET_DATA_OBJECT_NAME));
        return rel;
    }

    private static DataFieldInput dataField(String fieldName, String dataObjectName) {
        DataFieldInput df = new DataFieldInput();
        df.setName(fieldName);
        df.setDataObject(ref(dataObjectName));
        return df;
    }
}
