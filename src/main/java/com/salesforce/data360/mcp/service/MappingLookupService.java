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
package com.salesforce.data360.mcp.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Loads standard SObject-to-DMO mapping definitions from XML files on the classpath
 * (mappings/*.xml) at startup and provides fast lookup by source object name.
 */
@Service
public class MappingLookupService {

    private static final Logger log = LoggerFactory.getLogger(MappingLookupService.class);

    private static final Pattern DLO_SUFFIX_PATTERN =
        Pattern.compile("^(.+)_[^_].*__(dll)$");
    private static final Pattern BARE_DLO_SUFFIX =
        Pattern.compile("__(dll)$");

    // Keyed by lowercase sourceObjectName for case-insensitive lookup
    private final Map<String, SObjectDmoMappings> mappingsBySource = new HashMap<>();

    @PostConstruct
    void loadMappings() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:mappings/*.xml");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Disable external entities to prevent XXE
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = db.parse(is);
                    SObjectDmoMappings mapping = parseDocument(doc);
                    if (mapping != null) {
                        mappingsBySource.put(mapping.sourceObjectName().toLowerCase(), mapping);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse mapping file {}: {}", resource.getFilename(), e.getMessage());
                }
            }

            log.info("Loaded {} standard mapping definitions from classpath:mappings/", mappingsBySource.size());
        } catch (Exception e) {
            log.warn("Failed to load mapping files from classpath:mappings/: {}", e.getMessage());
        }
    }

    /**
     * Look up standard mappings for a source object name.
     * Matching is case-insensitive and also tries stripping common suffixes
     * (e.g., "Account_00D000000000000__dll" will match "Account").
     *
     * @return the mapping if found, or null
     */
    public SObjectDmoMappings lookup(String sourceObjectName) {
        if (sourceObjectName == null) return null;

        // Exact (case-insensitive) match
        SObjectDmoMappings result = mappingsBySource.get(sourceObjectName.toLowerCase());
        if (result != null) return result;

        // Strip common DLO suffixes: "Account_00D000000000000__dll" -> "Account"
        String stripped = stripDloSuffix(sourceObjectName);
        if (!stripped.equalsIgnoreCase(sourceObjectName)) {
            result = mappingsBySource.get(stripped.toLowerCase());
            if (result != null) return result;
        }

        return null;
    }

    /**
     * Return the number of loaded mapping definitions.
     */
    public int size() {
        return mappingsBySource.size();
    }

    // -- Parsing helpers --

    private static SObjectDmoMappings parseDocument(Document doc) {
        Element root = doc.getDocumentElement();
        String sourceObjectName = root.getAttribute("sourceObjectName");
        if (sourceObjectName == null || sourceObjectName.isEmpty()) return null;

        List<DmoMapping> dmoMappings = new ArrayList<>();
        NodeList dmoNodes = root.getElementsByTagName("Dmo_Mapping");
        for (int i = 0; i < dmoNodes.getLength(); i++) {
            Element dmoElem = (Element) dmoNodes.item(i);
            String dmoName = dmoElem.getAttribute("dmoName");

            List<FieldMapping> fieldMappings = new ArrayList<>();
            NodeList fieldNodes = dmoElem.getElementsByTagName("fieldMapping");
            for (int j = 0; j < fieldNodes.getLength(); j++) {
                Element fieldElem = (Element) fieldNodes.item(j);
                fieldMappings.add(new FieldMapping(
                    fieldElem.getAttribute("sourceField"),
                    fieldElem.getAttribute("targetField"),
                    "true".equalsIgnoreCase(fieldElem.getAttribute("isFilterApplied")),
                    emptyToNull(fieldElem.getAttribute("filterOperationType"))
                ));
            }

            dmoMappings.add(new DmoMapping(dmoName, fieldMappings));
        }

        return new SObjectDmoMappings(sourceObjectName, dmoMappings);
    }

    /**
     * Strip DLO-style suffixes to get the base SObject name.
     * E.g., "Account_00D000000000000__dll" -> "Account",
     *       "Account_Home__dll" -> "Account"
     */
    static String stripDloSuffix(String name) {
        var m = DLO_SUFFIX_PATTERN.matcher(name);
        if (m.matches()) {
            return m.group(1);
        }
        // Fallback for bare names without a middle segment (e.g., "MyObject__dll")
        return BARE_DLO_SUFFIX.matcher(name).replaceAll("");
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    // -- Data records --

    public record SObjectDmoMappings(String sourceObjectName, List<DmoMapping> dmoMappings) {}

    public record DmoMapping(String dmoName, List<FieldMapping> fieldMappings) {}

    public record FieldMapping(String sourceField, String targetField,
                               boolean isFilterApplied, String filterOperationType) {}
}
