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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keyword-first, vector-boost hybrid search.
 * Keyword results are primary. Vector only promotes families that keyword missed
 * but vector scored highly on. Never lets vector demote a strong keyword match.
 */
public class HybridSearchStrategy implements SearchStrategy {

    private final KeywordSearchStrategy keyword;
    private final VectorSearchStrategy vector;

    public HybridSearchStrategy(KeywordSearchStrategy keyword, VectorSearchStrategy vector) {
        this.keyword = keyword;
        this.vector = vector;
    }

    @Override
    public List<ScoredFamily> search(String query, List<FamilyCatalog.FamilyEntry> families, int topK) {
        List<ScoredFamily> kwResults = keyword.search(query, families, topK * 2);
        List<ScoredFamily> vecResults = vector.search(query, families, topK * 2);

        // Start with keyword scores as primary
        Map<String, Double> scores = new LinkedHashMap<>();
        for (ScoredFamily sf : kwResults) {
            scores.put(sf.family(), sf.score());
        }

        // Boost: add vector-only families or boost low-scoring keyword families
        for (ScoredFamily sf : vecResults) {
            double kwScore = scores.getOrDefault(sf.family(), 0.0);
            if (kwScore < 0.1 && sf.score() > 0.3) {
                // Keyword missed this but vector found it — add with vector score
                scores.put(sf.family(), sf.score());
            } else if (kwScore > 0) {
                // Both found it — keep the higher score
                scores.put(sf.family(), Math.max(kwScore, sf.score()));
            }
        }

        return scores.entrySet().stream()
            .map(e -> new ScoredFamily(e.getKey(), e.getValue()))
            .sorted(Comparator.comparingDouble(ScoredFamily::score).reversed())
            .limit(topK)
            .toList();
    }
}
