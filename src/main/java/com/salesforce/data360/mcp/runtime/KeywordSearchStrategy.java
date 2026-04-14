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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Token-overlap keyword search over family summaries + tool names + descriptions.
 * No external dependencies. Scored 89% coverage on 35 real Salesforce use cases.
 */
public class KeywordSearchStrategy implements SearchStrategy {

    @Override
    public List<ScoredFamily> search(String query, List<FamilyCatalog.FamilyEntry> families, int topK) {
        String[] tokens = query.toLowerCase(Locale.ROOT).split("\\s+");
        List<ScoredFamily> results = new ArrayList<>();

        for (FamilyCatalog.FamilyEntry family : families) {
            double score = scoreFamily(family, tokens);
            if (score > 0) {
                results.add(new ScoredFamily(family.family(), score));
            }
        }

        results.sort(Comparator.comparingDouble(ScoredFamily::score).reversed());
        return results.stream().limit(topK).toList();
    }

    private static double scoreFamily(FamilyCatalog.FamilyEntry family, String[] tokens) {
        String hay = buildSearchText(family).toLowerCase(Locale.ROOT);
        int matched = 0;
        int total = 0;
        for (String token : tokens) {
            if (token.length() <= 2) continue;
            total++;
            if (hay.contains(token)) matched++;
        }
        return total > 0 ? (double) matched / total : 0;
    }

    static String buildSearchText(FamilyCatalog.FamilyEntry family) {
        StringBuilder sb = new StringBuilder();
        sb.append(family.family()).append(" ");
        sb.append(family.summary()).append(" ");
        for (FamilyCatalog.ToolInfo tool : family.tools()) {
            sb.append(tool.name().replace('_', ' ')).append(" ");
            if (tool.description() != null) sb.append(tool.description()).append(" ");
        }
        return sb.toString();
    }
}
