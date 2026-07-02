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
class TopicClassificationPredictionJobDefToolsTest {

    private static final String DEFAULT_MODEL_NAME = "sfdc_ai__DefaultSalesforceTopicClassification";
    private static final String DATA_OBJECT_NAME = "Article__dlm";
    private static final String MAPPED_FIELD_NAME = "Body__c";
    private static final String SOURCE_DATA_OBJECT_NAME = "Article__dlm";
    private static final String SOURCE_FIELD_NAME = "AuthorId__c";
    private static final String TARGET_DATA_OBJECT_NAME = "Account__dlm";
    private static final String TARGET_FIELD_NAME = "Id__c";
    private static final String OUTPUT_OBJECT_NAME = "ArticleTopics__dlm";
    private static final List<String> TOPIC_LABELS =
            List.of("Technology", "Business", "Healthcare", "Education");

    @Mock
    private Data360Client client;

    private TopicClassificationPredictionJobDefTools tools;

    @BeforeEach
    void setUp() {
        tools = new TopicClassificationPredictionJobDefTools(client);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void create_wrapsTopicLabelsAsStaticBlock() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "abc"));

        tools.createTopicClassificationPredictionJobDef(
                "TopicClassificationJob",
                ref(DATA_OBJECT_NAME),
                List.of(textField()),
                TOPIC_LABELS,
                OUTPUT_OBJECT_NAME,
                null,
                "Topic Classification Job", "Topic classification prediction job",
                null, "Output description",
                "Batch");

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));

        Map<String, Object> b = body.getValue();
        assertThat(b)
                .containsEntry("type", "TopicClassification")
                .containsEntry("apiName", "TopicClassificationJob")
                .containsEntry("label", "Topic Classification Job")
                .containsEntry("scoringMode", "Batch");

        assertThat((Map<String, Object>) b.get("model"))
                .containsEntry("name", DEFAULT_MODEL_NAME);

        Map<String, Object> input = (Map<String, Object>) b.get("inputConfig");
        assertThat((Map<String, Object>) input.get("dataObject"))
                .containsEntry("name", DATA_OBJECT_NAME);

        Map<String, Object> topicLabels = (Map<String, Object>) input.get("topicLabels");
        assertThat(topicLabels).containsEntry("type", "Static");
        assertThat((List) topicLabels.get("values")).containsExactlyElementsOf(TOPIC_LABELS);
    }

    @Test
    void create_emptyTextFields_returnsErrorWithoutHttpCall() {
        String result = tools.createTopicClassificationPredictionJobDef(
                "x", ref(DATA_OBJECT_NAME), List.of(),
                TOPIC_LABELS,
                OUTPUT_OBJECT_NAME,
                null, null, null, null, null, null);

        assertThat(result).contains("error", "textFields");
        verifyNoInteractions(client);
    }

    @Test
    void create_nullTextFields_returnsErrorWithoutHttpCall() {
        String result = tools.createTopicClassificationPredictionJobDef(
                "x", ref(DATA_OBJECT_NAME), null,
                TOPIC_LABELS,
                OUTPUT_OBJECT_NAME,
                null, null, null, null, null, null);

        assertThat(result).contains("error", "textFields");
        verifyNoInteractions(client);
    }

    @Test
    void create_emptyTopicLabels_returnsErrorWithoutHttpCall() {
        String result = tools.createTopicClassificationPredictionJobDef(
                "x", ref(DATA_OBJECT_NAME), List.of(textField()),
                List.of(),
                OUTPUT_OBJECT_NAME,
                null, null, null, null, null, null);

        assertThat(result).contains("error", "topicLabels");
        verifyNoInteractions(client);
    }

    @Test
    void create_nullTopicLabels_returnsErrorWithoutHttpCall() {
        String result = tools.createTopicClassificationPredictionJobDef(
                "x", ref(DATA_OBJECT_NAME), List.of(textField()),
                null,
                OUTPUT_OBJECT_NAME,
                null, null, null, null, null, null);

        assertThat(result).contains("error", "topicLabels");
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
