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
class ClusteringPredictionJobDefToolsTest {

    private static final String MODEL_NAME = "SegmentationModel";
    private static final String DATA_OBJECT_NAME = "Customer__dlm";
    private static final String MODEL_FIELD_NAME = "SegmentFeature";
    private static final String MAPPED_FIELD_NAME = "AnnualRevenue__c";
    private static final String SOURCE_DATA_OBJECT_NAME = "Customer__dlm";
    private static final String SOURCE_FIELD_NAME = "AccountId__c";
    private static final String TARGET_DATA_OBJECT_NAME = "Account__dlm";
    private static final String TARGET_FIELD_NAME = "Id__c";
    private static final String OUTPUT_OBJECT_NAME = "CustomerClusters__dlm";

    @Mock
    private Data360Client client;

    private ClusteringPredictionJobDefTools tools;

    @BeforeEach
    void setUp() {
        tools = new ClusteringPredictionJobDefTools(client);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void create_buildsClusteringPayload() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "abc"));

        PredictionJobDefSettingsInput settings = new PredictionJobDefSettingsInput();
        settings.setClusterIdFieldLabel("Cluster ID");
        settings.setClusterLabelFieldLabel("Cluster Label");
        settings.setMaxTopContributors(3);

        tools.createClusteringPredictionJobDef(
                "customer_segments",
                ref(MODEL_NAME),
                ref(DATA_OBJECT_NAME),
                List.of(featureMapping()),
                OUTPUT_OBJECT_NAME,
                settings,
                "Clustering Prediction Job", "Clustering prediction job for segmentation",
                null, "Output description",
                "Batch");

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));

        Map<String, Object> b = body.getValue();
        assertThat(b)
                .containsEntry("type", "Clustering")
                .containsEntry("apiName", "customer_segments")
                .containsEntry("label", "Clustering Prediction Job")
                .containsEntry("scoringMode", "Batch");

        assertThat((Map<String, Object>) b.get("model"))
                .containsEntry("name", MODEL_NAME)
                .doesNotContainKey("id");

        Map<String, Object> input = (Map<String, Object>) b.get("inputConfig");
        assertThat((Map<String, Object>) input.get("dataObject"))
                .containsEntry("name", DATA_OBJECT_NAME);
        assertThat((List) input.get("fields")).hasSize(1);

        Map<String, Object> cs = (Map<String, Object>) b.get("clusteringSettings");
        assertThat(cs)
                .containsEntry("clusterIdFieldLabel", "Cluster ID")
                .containsEntry("clusterLabelFieldLabel", "Cluster Label")
                .containsEntry("maxTopContributors", 3);
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
