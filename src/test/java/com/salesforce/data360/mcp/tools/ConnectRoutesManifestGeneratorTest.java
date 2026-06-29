/*
 * Copyright (c) 2026, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.salesforce.data360.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Regenerates {@code src/test/resources/connect-routes.json} from connect-api goldfile XMLs.
 *
 * <p>This test is the in-tree replacement for the old {@code scripts/extract_connect_routes.py}.
 * It is disabled by default and runs only when {@code -Dgoldfiles=...} is set, so normal
 * {@code mvn test} runs are unaffected. The committed manifest remains the gating source of
 * truth used by {@link ToolEndpointPathTest}.
 *
 * <h3>Usage</h3>
 * <pre>
 * mvn test -Dtest=ConnectRoutesManifestGeneratorTest \
 *   -Dgoldfiles=/path/to/cdp-connect-udf-66.0.xml,/path/to/semantic-authoring-connect-udf-66.0.xml \
 *   -DcoreBranch=p4/260-patch+v66 \
 *   -DcoreRoot=/path/to/core
 * </pre>
 *
 * <p>Goldfiles are the post-build canonical XML representation of every shipped
 * {@code @ConnectResource} (paths, verbs, parameters). They live in core under
 * {@code <module>/test/unit/{,java/}resources/goldfiles/<module>-udf-<api>.xml} and
 * ship inside {@code <module>/lib_trim.jar} under {@code generated-udf/}.
 */
class ConnectRoutesManifestGeneratorTest {

    private static final Pattern GOLDFILE_NAME =
            Pattern.compile("^(?<module>.+)-udf-(?<api>\\d+\\.\\d+)\\.xml$");

    @Test
    @EnabledIfSystemProperty(named = "goldfiles", matches = ".+")
    void regenerateManifestFromGoldfiles() throws Exception {
        String goldfilesProp = System.getProperty("goldfiles");
        String coreBranch = System.getProperty("coreBranch", "unknown");
        String coreRootProp = System.getProperty("coreRoot");
        String outputProp = System.getProperty(
                "manifestOutput", "src/test/resources/connect-routes.json");

        List<Path> goldfiles = new ArrayList<>();
        for (String s : goldfilesProp.split(",")) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) continue;
            Path p = Paths.get(trimmed);
            if (!Files.isRegularFile(p)) {
                fail("--goldfiles entry is not a file: " + p);
            }
            goldfiles.add(p);
        }
        if (goldfiles.isEmpty()) {
            fail("Set -Dgoldfiles=path1,path2,...");
        }

        Path coreRoot = coreRootProp == null ? null : Paths.get(coreRootProp);

        List<Goldfile> parsed = new ArrayList<>();
        for (Path gf : goldfiles) {
            parsed.add(parseGoldfile(gf, coreRoot));
        }

        List<Map<String, Object>> mergedRoutes = mergeRoutes(parsed);
        if (mergedRoutes.isEmpty()) {
            fail("Extracted 0 routes — check the goldfile inputs.");
        }

        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("coreBranch", coreBranch);
        manifest.put("generatedAt",
                DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)
                        .format(Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)));
        List<Map<String, Object>> goldfilesSummary = new ArrayList<>();
        for (Goldfile g : parsed) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("module", g.module);
            entry.put("apiVersion", g.apiVersion);
            entry.put("routeCount", g.routes.size());
            goldfilesSummary.add(entry);
        }
        manifest.put("goldfiles", goldfilesSummary);
        manifest.put("routeCount", mergedRoutes.size());
        manifest.put("routes", mergedRoutes);

        Path output = Paths.get(outputProp);
        Files.createDirectories(output.getParent());
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.INDENT_OUTPUT);
        Files.writeString(output, m.writeValueAsString(manifest) + "\n");

        System.out.printf(
                "[connect-routes] wrote %d routes from %d goldfile(s) to %s%n",
                mergedRoutes.size(), parsed.size(), output);
        assertFalse(false);
    }

    private static Goldfile parseGoldfile(Path file, Path coreRoot) throws Exception {
        Matcher nameMatch = GOLDFILE_NAME.matcher(file.getFileName().toString());
        if (!nameMatch.matches()) {
            throw new IllegalArgumentException(
                    "Goldfile name does not match expected pattern: " + file.getFileName());
        }
        String module = nameMatch.group("module");
        String apiVersion = nameMatch.group("api");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new File(file.toString()));

        String sourceFile = file.toString();
        if (coreRoot != null) {
            try {
                sourceFile = coreRoot.toAbsolutePath().relativize(file.toAbsolutePath()).toString();
            } catch (IllegalArgumentException ignored) {
                // Different roots: fall back to the absolute path.
            }
        }

        List<Map<String, Object>> routes = new ArrayList<>();
        NodeList resources = doc.getElementsByTagName("resource");
        for (int i = 0; i < resources.getLength(); i++) {
            Element resource = (Element) resources.item(i);

            List<String> urlPaths = collectText(resource, "url-path");
            List<String> verbs = new ArrayList<>();
            NodeList verbNodes = resource.getElementsByTagName("verb");
            for (int j = 0; j < verbNodes.getLength(); j++) {
                String name = ((Element) verbNodes.item(j)).getAttribute("name");
                if (!name.isEmpty() && !verbs.contains(name)) verbs.add(name);
            }
            if (urlPaths.isEmpty() || verbs.isEmpty()) continue;

            for (String url : urlPaths) {
                if (url.isEmpty()) continue;
                if (!url.startsWith("/ssot/") && !url.startsWith("/cdp/")) continue;
                Map<String, Object> route = new LinkedHashMap<>();
                route.put("path", url);
                route.put("methods", new ArrayList<>(verbs));
                route.put("sourceModule", module);
                route.put("sourceFile", sourceFile);
                routes.add(route);
            }
        }
        return new Goldfile(module, apiVersion, routes);
    }

    private static List<String> collectText(Element parent, String tag) {
        List<String> out = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            String t = n.getTextContent() == null ? "" : n.getTextContent().trim();
            out.add(t);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mergeRoutes(List<Goldfile> goldfiles) {
        // Key on (path, methods) so identical routes from multiple goldfiles dedupe.
        TreeMap<String, Map<String, Object>> grouped = new TreeMap<>();
        for (Goldfile g : goldfiles) {
            for (Map<String, Object> r : g.routes) {
                String path = (String) r.get("path");
                List<String> methods = (List<String>) r.get("methods");
                String key = path + "\0" + String.join(",", methods);
                grouped.putIfAbsent(key, r);
            }
        }
        return new ArrayList<>(grouped.values());
    }

    private record Goldfile(String module, String apiVersion, List<Map<String, Object>> routes) {}
}
