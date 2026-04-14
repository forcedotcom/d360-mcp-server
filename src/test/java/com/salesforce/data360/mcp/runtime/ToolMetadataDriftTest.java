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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.McpTool;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolMetadataDriftTest {

    private static final Set<String> FACADE_TOOLS = Set.of("search", "payload_examples", "execute");

    private static Set<String> liveToolNames;
    private static Set<String> familyCatalogNames;

    @BeforeAll
    static void scanToolAnnotations() throws Exception {
        liveToolNames = new TreeSet<>();
        String pkg = "com.salesforce.data360.mcp.tools";
        List<Class<?>> classes = findClasses(pkg);
        for (Class<?> clazz : classes) {
            Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(McpTool.class))
                .map(m -> m.getAnnotation(McpTool.class).name())
                .forEach(liveToolNames::add);
        }

        familyCatalogNames = new TreeSet<>(new FamilyCatalog().getAllToolNames());
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
