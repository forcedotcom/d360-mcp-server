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
package com.salesforce.data360.mcp.auth;

import com.salesforce.data360.mcp.config.AppProperties;

public final class AuthModeResolver {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String NONE = "none";

    private AuthModeResolver() {
    }

    public static String determineAuthMode(AppProperties properties) {
        if (isNotBlank(properties.getAccessToken()) && isNotBlank(properties.getInstanceUrl())) {
            return ACCESS_TOKEN;
        }
        if (CLIENT_CREDENTIALS.equals(properties.getAuthFlow())
            && isNotBlank(properties.getClientId())
            && isNotBlank(properties.getClientSecret())) {
            return CLIENT_CREDENTIALS;
        }
        return NONE;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
