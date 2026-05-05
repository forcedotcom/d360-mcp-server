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
package com.salesforce.data360.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "data360")
public class AppProperties {

    private String instanceUrl;
    private String accessToken;
    // Field defaults mirror application.yml; they apply when the class is
    // instantiated directly (tests) or when a property is missing entirely.
    private String loginUrl = "https://login.salesforce.com";
    private String clientId;
    private String clientSecret;
    private String authFlow;
    private String sfOrgAlias;
    private String apiVersion = "66.0";
    private String searchStrategy = "keyword";

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthFlow() {
        return authFlow;
    }

    public void setAuthFlow(String authFlow) {
        this.authFlow = authFlow;
    }

    public String getSfOrgAlias() {
        return sfOrgAlias;
    }

    public void setSfOrgAlias(String sfOrgAlias) {
        this.sfOrgAlias = sfOrgAlias;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getSearchStrategy() {
        return searchStrategy;
    }

    public void setSearchStrategy(String searchStrategy) {
        this.searchStrategy = searchStrategy;
    }
}
