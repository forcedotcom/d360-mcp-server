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
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.FeatureMappingInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.FieldRelationshipInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.MappedFieldInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefSettingsInput;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MulticlassClassificationPredictionJobDefToolsTest {

    private static final String MODEL_NAME = "TierClassifierModel";
    private static final String DATA_OBJECT_NAME = "Customer__dlm";
    private static final String MODEL_FIELD_NAME = "TierFeature";
    private static final String MAPPED_FIELD_NAME = "AnnualRevenue__c";
    private static final String SOURCE_DATA_OBJECT_NAME = "Customer__dlm";
    private static final String SOURCE_FIELD_NAME = "AccountId__c";
    private static final String TARGET_DATA_OBJECT_NAME = "Account__dlm";
    private static final String TARGET_FIELD_NAME = "Id__c";
    private static final String OUTPUT_OBJECT_NAME = "TierPredictions__dlm";

    @Mock
    private Data360Client client;

    private MulticlassClassificationPredictionJobDefTools tools;

    @BeforeEach
    void setUp() {
        tools = new MulticlassClassificationPredictionJobDefTools(client);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void create_buildsMulticlassPayload() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "abc"));

        PredictionJobDefSettingsInput settings = new PredictionJobDefSettingsInput();
        settings.setMaxTopContributors(3);
        settings.setNumberOfClasses(4);

        tools.createMulticlassClassificationPredictionJobDef(
                "tier_pred",
                ref(MODEL_NAME),
                ref(DATA_OBJECT_NAME),
                List.of(featureMapping()),
                OUTPUT_OBJECT_NAME,
                "Multiclass Classification Job", "Multiclass classification prediction job",
                null, "Output description",
                "Batch",
                settings);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));

        Map<String, Object> b = body.getValue();
        assertThat(b)
                .containsEntry("type", "MulticlassClassification")
                .containsEntry("apiName", "tier_pred")
                .containsEntry("label", "Multiclass Classification Job")
                .containsEntry("scoringMode", "Batch");

        assertThat((Map<String, Object>) b.get("model"))
                .containsEntry("name", MODEL_NAME)
                .doesNotContainKey("id");

        Map<String, Object> input = (Map<String, Object>) b.get("inputConfig");
        assertThat((Map<String, Object>) input.get("dataObject"))
                .containsEntry("name", DATA_OBJECT_NAME);
        assertThat((List) input.get("fields")).hasSize(1);

        Map<String, Object> mcs = (Map<String, Object>) b.get("multiclassClassificationSettings");
        assertThat(mcs)
                .containsEntry("maxTopContributors", 3)
                .containsEntry("numberOfClasses", 4);
    }

    private static AssetReferenceInput ref(String name) {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setName(name);
        return ref;
    }

    private static FeatureMappingInput featureMapping() {
        FeatureMappingInput f = new FeatureMappingInput();
        f.setModelField(ref(MODEL_FIELD_NAME));

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
