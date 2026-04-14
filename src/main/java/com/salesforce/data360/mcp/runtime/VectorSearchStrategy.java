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

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Semantic vector search using OpenAI embeddings.
 * Embeds all family texts at startup, queries via cosine similarity.
 * Requires EmbeddingModel bean (spring-ai-starter-model-openai + OPENAI_API_KEY).
 */
public class VectorSearchStrategy implements SearchStrategy {

    private final EmbeddingModel embeddingModel;
    private final List<FamilyVector> index;

    private record FamilyVector(String family, float[] vector) {}

    public VectorSearchStrategy(EmbeddingModel embeddingModel, List<FamilyCatalog.FamilyEntry> families) {
        this.embeddingModel = embeddingModel;
        this.index = buildIndex(families);
    }

    @Override
    public List<ScoredFamily> search(String query, List<FamilyCatalog.FamilyEntry> families, int topK) {
        float[] queryVector = embed(query);
        List<ScoredFamily> results = new ArrayList<>();
        for (FamilyVector fv : index) {
            double score = cosine(queryVector, fv.vector());
            results.add(new ScoredFamily(fv.family(), score));
        }
        results.sort(Comparator.comparingDouble(ScoredFamily::score).reversed());
        return results.stream().limit(topK).toList();
    }

    private List<FamilyVector> buildIndex(List<FamilyCatalog.FamilyEntry> families) {
        List<String> texts = families.stream()
            .map(KeywordSearchStrategy::buildSearchText)
            .toList();
        float[][] vectors = batchEmbed(texts);
        List<FamilyVector> idx = new ArrayList<>();
        for (int i = 0; i < families.size(); i++) {
            idx.add(new FamilyVector(families.get(i).family(), vectors[i]));
        }
        return idx;
    }

    private float[] embed(String text) {
        return batchEmbed(List.of(text))[0];
    }

    private float[][] batchEmbed(List<String> texts) {
        EmbeddingResponse response = embeddingModel.call(new EmbeddingRequest(texts, null));
        float[][] vectors = new float[texts.size()][];
        for (int i = 0; i < texts.size(); i++) {
            vectors[i] = response.getResults().get(i).getOutput();
        }
        return vectors;
    }

    private static double cosine(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0 : dot / denom;
    }
}
