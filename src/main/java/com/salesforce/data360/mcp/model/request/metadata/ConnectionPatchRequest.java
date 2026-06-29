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
package com.salesforce.data360.mcp.model.request.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for {@code PATCH /ssot/connections/{id}}.
 *
 * <p>Flat union mirroring the abstract {@code ConnectionPatchInputRepresentation} parent and
 * its concrete {@code MarketingCloudConnectionPatchInputRepresentation} subclass. Only the
 * fields that apply to the chosen {@code connectorType} should be populated; the rest are
 * omitted from the wire via {@code @JsonInclude(NON_NULL)}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionPatchRequest {

    @McpToolParam(description = "ype of the connector. For example, AwsRdsPostgres, AzureBlob, Databricks, Gcs, IngestApi, SalesforceDotCom, SalesforceMarketingCloud, Sftp, StreamingApp, and so forth.", required = false)
    private String connectorType;

    // ----- MarketingCloudConnectionPatch -----

    @McpToolParam(description = "MarketingCloud only: list of MC business units added to activate", required = false)
    private List<String> addActivationBusinessUnits;

    @McpToolParam(description = "MarketingCloud only: list of MC business unit to dataspace mappings to be added", required = false)
    private List<McBuToDataSpaceInput> addBusinessUnitsToDataSpaces;

    @McpToolParam(description = "MarketingCloud only: list of MC business units added to ingest data from", required = false)
    private List<String> addIngestionBusinessUnits;

    @McpToolParam(description = "MarketingCloud only: whether to allow profile business unit mapping data", required = false)
    private Boolean createProfileBuMappings;

    @McpToolParam(description = "MarketingCloud only: list of MC business units removed to activate", required = false)
    private List<String> removeActivationBusinessUnits;

    @McpToolParam(description = "MarketingCloud only: list of MC business unit to dataspace mappings to be removed", required = false)
    private List<McBuToDataSpaceInput> removeBusinessUnitsToDataSpaces;

    @McpToolParam(description = "MarketingCloud only: list of MC business units removed to ingest data from", required = false)
    private List<String> removeIngestionBusinessUnits;

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public List<String> getAddActivationBusinessUnits() {
        return addActivationBusinessUnits;
    }

    public void setAddActivationBusinessUnits(List<String> addActivationBusinessUnits) {
        this.addActivationBusinessUnits = addActivationBusinessUnits;
    }

    public List<McBuToDataSpaceInput> getAddBusinessUnitsToDataSpaces() {
        return addBusinessUnitsToDataSpaces;
    }

    public void setAddBusinessUnitsToDataSpaces(List<McBuToDataSpaceInput> addBusinessUnitsToDataSpaces) {
        this.addBusinessUnitsToDataSpaces = addBusinessUnitsToDataSpaces;
    }

    public List<String> getAddIngestionBusinessUnits() {
        return addIngestionBusinessUnits;
    }

    public void setAddIngestionBusinessUnits(List<String> addIngestionBusinessUnits) {
        this.addIngestionBusinessUnits = addIngestionBusinessUnits;
    }

    public Boolean getCreateProfileBuMappings() {
        return createProfileBuMappings;
    }

    public void setCreateProfileBuMappings(Boolean createProfileBuMappings) {
        this.createProfileBuMappings = createProfileBuMappings;
    }

    public List<String> getRemoveActivationBusinessUnits() {
        return removeActivationBusinessUnits;
    }

    public void setRemoveActivationBusinessUnits(List<String> removeActivationBusinessUnits) {
        this.removeActivationBusinessUnits = removeActivationBusinessUnits;
    }

    public List<McBuToDataSpaceInput> getRemoveBusinessUnitsToDataSpaces() {
        return removeBusinessUnitsToDataSpaces;
    }

    public void setRemoveBusinessUnitsToDataSpaces(List<McBuToDataSpaceInput> removeBusinessUnitsToDataSpaces) {
        this.removeBusinessUnitsToDataSpaces = removeBusinessUnitsToDataSpaces;
    }

    public List<String> getRemoveIngestionBusinessUnits() {
        return removeIngestionBusinessUnits;
    }

    public void setRemoveIngestionBusinessUnits(List<String> removeIngestionBusinessUnits) {
        this.removeIngestionBusinessUnits = removeIngestionBusinessUnits;
    }
}
