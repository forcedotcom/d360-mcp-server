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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the Connect API endpoint that an {@code @McpTool} method calls —
 * its HTTP path and verb together.
 *
 * <p>Used by {@code ToolMetadataDriftTest} to assert that the path and verb
 * declared on each tool method match the corresponding row in
 * {@link FamilyCatalog}. Catches the bug class where the runtime code and the
 * catalog disagree on what API a tool calls.
 *
 * <p>Path templates use {@code {variable}} placeholders for any URL-encoded
 * segment the runtime substitutes. Verb is the literal HTTP method
 * ({@code "GET"}, {@code "POST"}, {@code "PATCH"}, {@code "PUT"},
 * {@code "DELETE"}).
 *
 * <p>Example:
 * <pre>{@code
 * @McpTool(name = "d360_segment_publish", description = "...")
 * @ApiEndpoint(path = "/ssot/segments/{id}/actions/publish", verb = "POST")
 * public String publishSegment(...) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiEndpoint {
    String path();
    String verb();
}
