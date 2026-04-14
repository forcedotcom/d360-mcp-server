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
package com.salesforce.data360.mcp.util;

import com.salesforce.data360.mcp.model.common.ApiException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolUtilsTest {

    @Test
    void buildPath_noParams_returnsBasePath() {
        String result = ToolUtils.buildPath("/dmos", Map.of());
        assertThat(result).isEqualTo("/dmos");
    }

    @Test
    void buildPath_singleParam_appendsQueryString() {
        String result = ToolUtils.buildPath("/dmos", Map.of("dataspace", "default"));
        assertThat(result).contains("/dmos?");
        assertThat(result).contains("dataspace=default");
    }

    @Test
    void buildPath_specialCharacters_areUrlEncoded() {
        String result = ToolUtils.buildPath("/query-sql", Map.of("dataspace", "my space&test"));
        assertThat(result).contains("my+space%26test");
    }

    @Test
    void buildPath_nullParams_returnsBasePath() {
        String result = ToolUtils.buildPath("/dmos", (Map<String, Object>) null);
        assertThat(result).isEqualTo("/dmos");
    }

    @Test
    void buildPath_dataspaceOnly_noSpecialChars() {
        String result = ToolUtils.buildPath("/segments", "default");
        assertThat(result).isEqualTo("/segments?dataspace=default");
    }

    @Test
    void buildPath_dataspaceOnly_null_returnsBasePath() {
        String result = ToolUtils.buildPath("/segments", (String) null);
        assertThat(result).isEqualTo("/segments");
    }

    @Test
    void buildPath_dataspaceOnly_blank_returnsBasePath() {
        String result = ToolUtils.buildPath("/segments", "   ");
        assertThat(result).isEqualTo("/segments");
    }

    @Test
    void buildPath_dataspaceOnly_specialChars_urlEncoded() {
        String result = ToolUtils.buildPath("/segments", "my space");
        assertThat(result).contains("dataspace=my+space");
    }

    @Test
    void encodePath_normalSegment_unchanged() {
        assertThat(ToolUtils.encodePath("abc123")).isEqualTo("abc123");
    }

    @Test
    void encodePath_slashInSegment_encoded() {
        assertThat(ToolUtils.encodePath("a/b")).isEqualTo("a%2Fb");
    }

    @Test
    void encodePath_spaces_encoded() {
        assertThat(ToolUtils.encodePath("my name")).isEqualTo("my%20name");
    }

    @Test
    void errorResponse_includesStatusCodeAndMessage() {
        ApiException ex = new ApiException(400, "Bad Request", "/dmos");
        String json = ToolUtils.errorResponse(ex);
        assertThat(json).contains("\"statusCode\"");
        assertThat(json).contains("400");
        assertThat(json).contains("Bad Request");
        assertThat(json).contains("\"path\"");
        assertThat(json).contains("/dmos");
    }
}
