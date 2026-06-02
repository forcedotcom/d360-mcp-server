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

import com.salesforce.data360.mcp.config.AppProperties;
import com.salesforce.data360.mcp.tools.PayloadExamples;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.McpTool;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolMetadataDriftTest {

    private static final Set<String> FACADE_TOOLS = Set.of("search", "payload_examples", "execute");

    /**
     * Tool names that intentionally have no {@code @ApiEndpoint} because the
     * runtime method does not call a single deterministic Connect API path —
     * either it composes multiple calls (Smart family) or runs purely
     * locally (preview-only helpers).
     */
    private static final Set<String> NO_ENDPOINT_TOOLS = Set.of(
        "d360_smart_mapping_suggest",
        "d360_preview_field_matches",
        "d360_smart_datastream_create",
        "d360_event_date_recommend",
        "d360_standard_mapping_preview"
    );

    /**
     * Tools whose {@code @ApiEndpoint} path is not part of the CDP Connect API
     * gold file ({@code cdp-connect-udf-{version}.xml}) and therefore cannot be
     * validated against it. Add a tool here only if its endpoint is genuinely
     * not part of the CDP API surface.
     *
     * <p>{@code d360_metadata_search} calls {@code /connect/search/metadata/results}
     * (search-connect-api) and {@code d360_sdm_query} calls
     * {@code /semantic-engine/gateway} (semantic-engine-connect-api).
     *
     * <p>The SDM family lives at {@code /ssot/semantic/...} and is served by
     * {@code semantic-authoring-connect-api}, whose owners migrated to an
     * OpenAPI specification ({@code semantic-onestore-openapi-spec.yaml}) as
     * their schema source of truth — no UDF gold file beyond 61.0 is published
     * for that module, so its endpoints are not part of any UDF gold file we
     * could validate against here.
     */
    private static final Set<String> NO_GOLDEN_FILE_TOOLS = Set.of(
        "d360_metadata_search",  // /connect/search/metadata/results — search-connect-api
        "d360_sdm_query",        // /semantic-engine/gateway — semantic-engine-connect-api
        // SDM family — /ssot/semantic/... lives in semantic-engine-connect-api gold file
        "d360_sdm_calc_dim_create",
        "d360_sdm_calc_dim_delete",
        "d360_sdm_calc_dim_get",
        "d360_sdm_calc_dim_update",
        "d360_sdm_calc_dims_list",
        "d360_sdm_calc_measure_create",
        "d360_sdm_calc_measure_delete",
        "d360_sdm_calc_measure_get",
        "d360_sdm_calc_measure_update",
        "d360_sdm_calc_measures_list",
        "d360_sdm_clone",
        "d360_sdm_create",
        "d360_sdm_data_object_create",
        "d360_sdm_data_object_delete",
        "d360_sdm_data_object_get",
        "d360_sdm_data_object_update",
        "d360_sdm_data_objects_list",
        "d360_sdm_delete",
        "d360_sdm_dependencies",
        "d360_sdm_dimensions_list",
        "d360_sdm_formula_metadata",
        "d360_sdm_get",
        "d360_sdm_list",
        "d360_sdm_measurements_list",
        "d360_sdm_metric_create",
        "d360_sdm_metric_delete",
        "d360_sdm_metric_get",
        "d360_sdm_metric_update",
        "d360_sdm_metrics_list",
        "d360_sdm_permissions",
        "d360_sdm_relationship_create",
        "d360_sdm_relationship_delete",
        "d360_sdm_relationship_get",
        "d360_sdm_relationship_update",
        "d360_sdm_relationships_list",
        "d360_sdm_update",
        "d360_sdm_validate"
    );

    /**
     * Snapshot of tools whose {@code @ApiEndpoint} declaration disagrees with the
     * Connect API gold file at the time this gate was introduced. Same shape as
     * {@link #KNOWN_MISSING_PAYLOAD_EXAMPLES}: the set may only shrink. Each entry
     * is either a path that the gold file does not declare (likely a bug, to be
     * addressed as part of another PR) or a (path, verb) pair where the gold
     * file declares the path but not that verb.
     *
     * <p>Adding a fix for one of these requires removing the tool name from this
     * set in the same PR (enforced by {@link #goldenFileFailureSnapshotShouldNotHaveStaleEntries()}).
     * New tools are NOT allowed to be added here; they must agree with the gold
     * file or join {@link #NO_GOLDEN_FILE_TOOLS}.
     */
    private static final Set<String> KNOWN_GOLDEN_FILE_FAILURES = Set.of(
        "d360_activation_target_delete",
        "d360_activation_target_update",
        "d360_activation_update",
        "d360_ci_run_status",
        "d360_connection_endpoints",
        "d360_datakit_components",
        "d360_datakit_deploy",
        "d360_datakit_manifest",
        "d360_dataspace_delete",
        "d360_dataspace_member_add",
        "d360_dataspace_member_remove",
        "d360_datastream_run",
        "d360_dmo_mapping_update",
        "d360_ir_full_update",
        "d360_transform_update"
    );

    private static final String GOLDEN_FILE_DIR = "src/test/resources/golden";
    private static final String GOLDEN_FILE_PREFIX = "cdp-connect-udf-";
    private static final String GOLDEN_FILE_SUFFIX = ".xml";

    /**
     * Snapshot of write tools that did not have an entry in
     * {@code payload-examples.json} when this gate was introduced. The set is
     * a one-time capture so the gate can hard-fail without breaking PRs that
     * touch unrelated code. The set may only shrink — adding an example for
     * one of these tools requires removing the name from this set in the same
     * PR (enforced by {@link #payloadExampleSnapshotShouldNotHaveStaleEntries()}).
     * New write tools are NOT allowed to be added here; they must ship with an
     * example.
     */
    private static final Set<String> KNOWN_MISSING_PAYLOAD_EXAMPLES = Set.of(
        "d360_activation_target_update",
        "d360_activation_update",
        "d360_ci_disable",
        "d360_ci_enable",
        "d360_ci_run",
        "d360_ci_update",
        "d360_ci_validate",
        "d360_connection_create_snowflake",
        "d360_connection_test",
        "d360_connection_update",
        "d360_dataaction_target_update",
        "d360_datakit_undeploy",
        "d360_dataspace_create",
        "d360_dataspace_member_add",
        "d360_dataspace_update",
        "d360_datastream_create",
        "d360_datastream_create_sfdc",
        "d360_datastream_create_snowflake",
        "d360_datastream_run",
        "d360_datastream_update",
        "d360_dlo_create",
        "d360_dlo_update",
        "d360_dmo_create",
        "d360_dmo_field_mapping_add",
        "d360_dmo_mapping_update",
        "d360_dmo_update",
        "d360_ir_full_update",
        "d360_ir_publish",
        "d360_ir_run",
        "d360_ir_update",
        "d360_metadata_search",
        "d360_query_sql",
        "d360_retriever_config_create",
        "d360_retriever_config_update",
        "d360_retriever_create",
        "d360_retriever_update",
        "d360_sdm_calc_dim_update",
        "d360_sdm_calc_measure_update",
        "d360_sdm_clone",
        "d360_sdm_data_object_update",
        "d360_sdm_metric_create",
        "d360_sdm_metric_update",
        "d360_sdm_relationship_update",
        "d360_sdm_update",
        "d360_sdm_validate",
        "d360_search_index_create",
        "d360_search_index_update",
        "d360_segment_deactivate",
        "d360_segment_publish",
        "d360_segment_update",
        "d360_standard_mapping_create",
        "d360_transform_run",
        "d360_transform_schedule_set",
        "d360_transform_update",
        "d360_transform_validate"
    );

    private static Set<String> liveToolNames;
    private static Set<String> familyCatalogNames;
    private static Map<String, Method> liveToolMethods;
    private static FamilyCatalog catalog;

    @BeforeAll
    static void scanToolAnnotations() throws Exception {
        liveToolNames = new TreeSet<>();
        liveToolMethods = new TreeMap<>();
        String pkg = "com.salesforce.data360.mcp.tools";
        List<Class<?>> classes = findClasses(pkg);
        for (Class<?> clazz : classes) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(McpTool.class)) {
                    String name = m.getAnnotation(McpTool.class).name();
                    liveToolNames.add(name);
                    liveToolMethods.put(name, m);
                }
            }
        }

        catalog = new FamilyCatalog();
        familyCatalogNames = new TreeSet<>(catalog.getAllToolNames());
    }

    @Test
    void liveToolsShouldHaveFamilyCatalogEntries() {
        Set<String> missing = new TreeSet<>(liveToolNames);
        missing.removeAll(familyCatalogNames);
        missing.removeAll(FACADE_TOOLS);
        assertThat(missing)
            .as("Live @McpTool methods missing from FamilyCatalog")
            .isEmpty();
    }

    @Test
    void familyCatalogShouldNotHaveStaleEntries() {
        Set<String> stale = new TreeSet<>(familyCatalogNames);
        stale.removeAll(liveToolNames);
        assertThat(stale)
            .as("FamilyCatalog entries with no matching live @McpTool")
            .isEmpty();
    }

    @Test
    void toolNamePrefixesShouldBeConsistent() {
        Set<String> nonD360 = liveToolNames.stream()
            .filter(name -> !FACADE_TOOLS.contains(name))
            .filter(name -> !name.startsWith("d360_"))
            .collect(Collectors.toSet());
        assertThat(nonD360)
            .as("Non-facade tools should use d360_ prefix")
            .isEmpty();
    }

    @Test
    void everyApiBackedToolShouldDeclareApiEndpoint() {
        Set<String> missing = new TreeSet<>();
        for (String name : liveToolNames) {
            if (FACADE_TOOLS.contains(name) || NO_ENDPOINT_TOOLS.contains(name)) continue;
            Method m = liveToolMethods.get(name);
            if (m == null || !m.isAnnotationPresent(ApiEndpoint.class)) {
                missing.add(name);
            }
        }
        assertThat(missing)
            .as("@McpTool methods missing @ApiEndpoint — add @ApiEndpoint(path=..., verb=...) "
                + "above @McpTool, or add the tool name to NO_ENDPOINT_TOOLS if it has no single API path")
            .isEmpty();
    }

    @Test
    void noEndpointToolsShouldNotHaveStaleEntries() {
        Set<String> stale = new TreeSet<>(NO_ENDPOINT_TOOLS);
        stale.removeAll(liveToolNames);
        assertThat(stale)
            .as("NO_ENDPOINT_TOOLS contains entries that are not live @McpTool methods — "
                + "remove them so the exemption list stays trustworthy")
            .isEmpty();
    }

    @Test
    void writeToolsShouldHavePayloadExamples() {
        Set<String> writeVerbs = Set.of("POST", "PUT", "PATCH");
        Set<String> writeTools = liveToolNames.stream()
            .filter(n -> !FACADE_TOOLS.contains(n) && !NO_ENDPOINT_TOOLS.contains(n))
            .filter(n -> {
                FamilyCatalog.Endpoint ep = catalog.getEndpoint(n);
                return ep != null && writeVerbs.contains(ep.verb());
            })
            .collect(Collectors.toCollection(TreeSet::new));

        Set<String> missing = new TreeSet<>(writeTools);
        missing.removeAll(PayloadExamples.PAYLOAD_EXAMPLES.keySet());
        missing.removeAll(KNOWN_MISSING_PAYLOAD_EXAMPLES);

        assertThat(missing)
            .as("New write tools must ship with an entry in "
                + "src/main/resources/metadata/payload-examples.json — "
                + "agents rely on these examples to construct valid request bodies")
            .isEmpty();
    }

    @Test
    void payloadExampleSnapshotShouldNotHaveStaleEntries() {
        Set<String> stale = new TreeSet<>(KNOWN_MISSING_PAYLOAD_EXAMPLES);
        stale.retainAll(PayloadExamples.PAYLOAD_EXAMPLES.keySet());
        assertThat(stale)
            .as("KNOWN_MISSING_PAYLOAD_EXAMPLES contains tools that NOW have examples — "
                + "remove them from the set so the snapshot keeps shrinking")
            .isEmpty();

        Set<String> noLongerLive = new TreeSet<>(KNOWN_MISSING_PAYLOAD_EXAMPLES);
        noLongerLive.removeAll(liveToolNames);
        assertThat(noLongerLive)
            .as("KNOWN_MISSING_PAYLOAD_EXAMPLES contains tools that are not live @McpTool methods — "
                + "remove them")
            .isEmpty();
    }

    @Test
    void apiVersionMatchesGoldenFile() {
        String version = new AppProperties().getApiVersion();
        File goldenFile = new File(GOLDEN_FILE_DIR, GOLDEN_FILE_PREFIX + version + GOLDEN_FILE_SUFFIX);
        assertThat(goldenFile)
            .as("Connect API gold file for version %s must exist at %s — "
                + "if AppProperties.apiVersion changes, drop the matching gold file "
                + "(pulled from core/cdp-connect-api/test/unit/java/resources/goldfiles/) "
                + "into %s in the same PR",
                version, goldenFile.getPath(), GOLDEN_FILE_DIR)
            .exists()
            .isFile();
    }

    @Test
    void apiEndpointPathsExistInGoldenFile() throws Exception {
        String version = new AppProperties().getApiVersion();
        File goldenFile = new File(GOLDEN_FILE_DIR, GOLDEN_FILE_PREFIX + version + GOLDEN_FILE_SUFFIX);
        if (!goldenFile.isFile()) {
            // apiVersionMatchesGoldenFile reports the missing-file case; nothing to do here.
            return;
        }

        Map<String, Set<String>> goldenPaths = parseGoldenFile(goldenFile);

        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Method> entry : liveToolMethods.entrySet()) {
            String name = entry.getKey();
            Method m = entry.getValue();
            if (FACADE_TOOLS.contains(name)
                || NO_ENDPOINT_TOOLS.contains(name)
                || NO_GOLDEN_FILE_TOOLS.contains(name)
                || KNOWN_GOLDEN_FILE_FAILURES.contains(name)) continue;
            if (!m.isAnnotationPresent(ApiEndpoint.class)) continue;
            ApiEndpoint ann = m.getAnnotation(ApiEndpoint.class);
            String normalized = normalizePathTemplate(ann.path());
            Set<String> verbs = goldenPaths.get(normalized);
            if (verbs == null) {
                missing.add(String.format(
                    "%s: path %s not in gold file (normalized: %s)",
                    name, ann.path(), normalized));
            } else if (!verbs.contains(ann.verb())) {
                missing.add(String.format(
                    "%s: gold file has path %s but verb %s not declared (gold has %s)",
                    name, ann.path(), ann.verb(), verbs));
            }
        }

        assertThat(missing)
            .as("@ApiEndpoint paths must exist in the Connect API gold file at %s — "
                + "either fix the path/verb to match the live API, or refresh the gold file "
                + "if the API itself changed. If the endpoint genuinely lives in a different "
                + "Connect API surface (not CDP), add the tool name to NO_GOLDEN_FILE_TOOLS.",
                goldenFile.getPath())
            .isEmpty();
    }

    @Test
    void goldenFileFailureSnapshotShouldNotHaveStaleEntries() throws Exception {
        String version = new AppProperties().getApiVersion();
        File goldenFile = new File(GOLDEN_FILE_DIR, GOLDEN_FILE_PREFIX + version + GOLDEN_FILE_SUFFIX);
        if (!goldenFile.isFile()) {
            return;
        }
        Map<String, Set<String>> goldenPaths = parseGoldenFile(goldenFile);

        Set<String> nowPassing = new TreeSet<>();
        for (String name : KNOWN_GOLDEN_FILE_FAILURES) {
            Method m = liveToolMethods.get(name);
            if (m == null || !m.isAnnotationPresent(ApiEndpoint.class)) continue;
            ApiEndpoint ann = m.getAnnotation(ApiEndpoint.class);
            Set<String> verbs = goldenPaths.get(normalizePathTemplate(ann.path()));
            if (verbs != null && verbs.contains(ann.verb())) {
                nowPassing.add(name);
            }
        }
        assertThat(nowPassing)
            .as("KNOWN_GOLDEN_FILE_FAILURES contains tools that NOW agree with the gold file — "
                + "remove them from the set so the snapshot keeps shrinking")
            .isEmpty();

        Set<String> noLongerLive = new TreeSet<>(KNOWN_GOLDEN_FILE_FAILURES);
        noLongerLive.removeAll(liveToolNames);
        assertThat(noLongerLive)
            .as("KNOWN_GOLDEN_FILE_FAILURES contains tools that are not live @McpTool methods — "
                + "remove them")
            .isEmpty();
    }

    @Test
    void noGoldenFileToolsShouldNotHaveStaleEntries() {
        Set<String> stale = new TreeSet<>(NO_GOLDEN_FILE_TOOLS);
        stale.removeAll(liveToolNames);
        assertThat(stale)
            .as("NO_GOLDEN_FILE_TOOLS contains entries that are not live @McpTool methods — "
                + "remove them so the exemption list stays trustworthy")
            .isEmpty();
    }

    @Test
    void apiEndpointAnnotationShouldMatchFamilyCatalog() {
        List<String> mismatches = new ArrayList<>();
        for (Map.Entry<String, Method> entry : liveToolMethods.entrySet()) {
            String name = entry.getKey();
            Method m = entry.getValue();
            if (!m.isAnnotationPresent(ApiEndpoint.class)) continue;
            ApiEndpoint annotation = m.getAnnotation(ApiEndpoint.class);
            FamilyCatalog.Endpoint catalogEndpoint = catalog.getEndpoint(name);
            if (catalogEndpoint == null) {
                mismatches.add(name + ": no FamilyCatalog entry");
                continue;
            }
            if (!annotation.verb().equals(catalogEndpoint.verb())
                    || !annotation.path().equals(catalogEndpoint.path())) {
                mismatches.add(String.format(
                    "%s: @ApiEndpoint(verb=%s, path=%s) vs FamilyCatalog(verb=%s, path=%s)",
                    name, annotation.verb(), annotation.path(),
                    catalogEndpoint.verb(), catalogEndpoint.path()));
            }
        }
        assertThat(mismatches)
            .as("@ApiEndpoint annotations must match FamilyCatalog rows — fix one of them so they agree")
            .isEmpty();
    }

    /**
     * Parses the Connect API gold file into a {@code Map<normalizedPath, Set<verb>>}.
     *
     * <p>The gold file's structure is {@code <resource> → <rest>/<url-paths>/<url-path>}
     * for paths and {@code <resource> → <verbs>/<verb>} for verbs. Each {@code <resource>}
     * block shares its url-paths across all of its verbs (Cartesian product).
     *
     * <p>Path templates use {@code ${name}} in the gold file; we normalize to {@code {name}}
     * so the comparison is independent of brace style. Variable names are kept so that
     * misnamed variables ({@code {id}} vs {@code {segmentId}}) still produce a mismatch —
     * if that turns out to be too brittle, we can switch to a structural-only normalization.
     */
    private static Map<String, Set<String>> parseGoldenFile(File goldenFile) throws Exception {
        Map<String, Set<String>> result = new HashMap<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        try (InputStream in = new FileInputStream(goldenFile)) {
            XMLStreamReader r = factory.createXMLStreamReader(in);
            int depth = 0;
            int resourceDepth = -1;
            int urlPathsDepth = -1;
            int verbsDepth = -1;
            List<String> currentPaths = new ArrayList<>();
            List<String> currentVerbs = new ArrayList<>();
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    depth++;
                    String local = r.getLocalName();
                    switch (local) {
                        case "resource":
                            if (resourceDepth == -1) {
                                resourceDepth = depth;
                                currentPaths.clear();
                                currentVerbs.clear();
                            }
                            break;
                        case "url-paths":
                            urlPathsDepth = depth;
                            break;
                        case "url-path":
                            if (urlPathsDepth != -1) {
                                String text = r.getElementText();
                                if (text != null && !text.isBlank()) {
                                    currentPaths.add(normalizePathTemplate(text.trim()));
                                }
                                depth--;  // getElementText consumes the matching END_ELEMENT
                            }
                            break;
                        case "verbs":
                            verbsDepth = depth;
                            break;
                        case "verb":
                            if (verbsDepth != -1) {
                                String name = r.getAttributeValue(null, "name");
                                if (name != null) currentVerbs.add(name);
                            }
                            break;
                        default:
                            break;
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String local = r.getLocalName();
                    if ("url-paths".equals(local) && depth == urlPathsDepth) urlPathsDepth = -1;
                    if ("verbs".equals(local) && depth == verbsDepth) verbsDepth = -1;
                    if ("resource".equals(local) && depth == resourceDepth) {
                        for (String p : currentPaths) {
                            result.computeIfAbsent(p, k -> new HashSet<>()).addAll(currentVerbs);
                        }
                        resourceDepth = -1;
                        currentPaths.clear();
                        currentVerbs.clear();
                    }
                    depth--;
                }
            }
            r.close();
        }
        return result;
    }

    private static final Pattern PATH_VARIABLE = Pattern.compile("\\$?\\{[^}]+\\}");

    /**
     * Normalizes a path template for comparison by collapsing every variable placeholder
     * (whether {@code {name}} or {@code ${name}}) to a single {@code {}} token.
     *
     * <p>This deliberately ignores the variable name. The gold file uses descriptive names
     * ({@code ${segmentId}}, {@code ${dloName}}); annotations often use shorter generic
     * names ({@code {id}}, {@code {name}}). The test asserts that the path *shape* exists
     * in the API, not that we chose a particular variable label. Picking a descriptive
     * name vs a generic one is a docs concern, not a contract concern.
     */
    static String normalizePathTemplate(String path) {
        return PATH_VARIABLE.matcher(path).replaceAll("{}");
    }

    private static List<Class<?>> findClasses(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<Class<?>> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                findClassesInDir(new File(resource.getFile()), packageName, classes);
            }
        }
        return classes;
    }

    private static void findClassesInDir(File dir, String packageName, List<Class<?>> classes)
            throws ClassNotFoundException {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                findClassesInDir(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (NoClassDefFoundError e) {
                }
            }
        }
    }
}
