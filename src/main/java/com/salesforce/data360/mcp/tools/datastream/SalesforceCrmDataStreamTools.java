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
package com.salesforce.data360.mcp.tools.datastream;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.request.datastream.ConnectorInput;
import com.salesforce.data360.mcp.model.request.datastream.DataLakeObjectInput;
import com.salesforce.data360.mcp.model.request.datastream.DataSpaceInput;
import com.salesforce.data360.mcp.model.request.datastream.DataStreamCreateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data stream creation tool for Salesforce CRM connector.
 * Pre-fills SFDC-specific values (datastreamType, connectorType, connectorName, dataspace)
 * so the caller only needs to specify the variable parts.
 */
@Component
public class SalesforceCrmDataStreamTools extends AbstractConnectorDataStreamTools {

    private static final String DATASTREAM_TYPE = "SFDC";
    private static final String CONNECTOR_TYPE = "SalesforceDotCom";
    private static final String DEFAULT_CONNECTOR_ALIAS = "Home";
    private static final String DEFAULT_DATASPACE = "default";
    private static final String DEFAULT_CATEGORY = "Other";

    public SalesforceCrmDataStreamTools(Data360Client client) {
        super(client);
    }

    /**
     * Create a data stream that ingests from a Salesforce CRM object.
     */
    @ApiEndpoint(path = "/ssot/data-streams", verb = "POST")
    @McpTool(
            name = "d360_datastream_create_sfdc",
            description = "Create a data stream from a Salesforce CRM object. "
                    + "PREREQUISITE: Before calling this tool, call 'd360_connection_list' with connectorType=SalesforceDotCom "
                    + "and present the user with the organizationId, alias, and connectorType from the output "
                    + "so they can select the connection to use. "
                    + "ALWAYS use the connection's alias as the connectionAliasOrOrganizationId parameter. Only fall back to organizationId if alias is not present. "
                    + "The dataStreamName and dloName are auto-derived as '{sourceObject}_{connectionAliasOrOrganizationId}' "
                    + "unless explicitly overridden. "
                    + "IMPORTANT: Event/transactional objects (Case, CaseComment, Task, Event, ActivityHistory) MUST use "
                    + "category='Engagement' with eventDateTimeFieldName (e.g. 'CreatedDate'). "
                    + "Profile objects (Contact, Account, Lead) use category='Profile'. "
    )
    public String createSfdcDataStream(
            @McpToolParam(description = "Salesforce CRM source object name (e.g. 'Benefit', 'Account', 'Contact')") String sourceObject,
            @McpToolParam(description = "Alias or organizationId of the connection selected from d360_connection_list output") String connectionAliasOrOrganizationId,
            @McpToolParam(description = "DLO category: 'Profile', 'Engagement', 'Other'", required = false) String category,
            @McpToolParam(description = "Override for data stream name; defaults to '{sourceObject}_{connectionAliasOrOrganizationId}'", required = false) String dataStreamName,
            @McpToolParam(description = "Override for Data Lake Object name; defaults to '{sourceObject}_{connectionAliasOrOrganizationId}'", required = false) String dloName,
            @McpToolParam(description = "Dataspace name override (defaults to 'default')", required = false) String dataspace,
            @McpToolParam(description = "Name of the event date/time field. Required when category is 'Engagement'.", required = false) String eventDateTimeFieldName
    ) {
        String resolvedConnectionName = (connectionAliasOrOrganizationId != null) ? connectionAliasOrOrganizationId : DEFAULT_CONNECTOR_ALIAS;
        String resolvedName = (dataStreamName != null) ? dataStreamName : sourceObject + "_" + resolvedConnectionName;
        String resolvedDloName = (dloName != null) ? dloName : sourceObject + "_" + resolvedConnectionName;
        String resolvedCategory = (category != null) ? category : DEFAULT_CATEGORY;

        if ("Engagement".equalsIgnoreCase(resolvedCategory) && (eventDateTimeFieldName == null || eventDateTimeFieldName.isBlank())) {
            return JsonUtil.toJson(Map.of("error", "eventDateTimeFieldName is required when category is Engagement"));
        }

        String resolvedDataspace = (dataspace != null) ? dataspace : DEFAULT_DATASPACE;

        DataStreamCreateRequest request = buildRequest(resolvedName, sourceObject, resolvedCategory,
                resolvedDloName, resolvedConnectionName, eventDateTimeFieldName, resolvedDataspace);
        return createDataStream(request);
    }

    private DataStreamCreateRequest buildRequest(String name, String sourceObject,
                                                  String category, String dloName,
                                                  String connectionName, String eventDateTimeFieldName,
                                                  String dataspace) {
        DataStreamCreateRequest request = new DataStreamCreateRequest();
        request.setName(name);
        request.setLabel(name);
        request.setDatastreamType(DATASTREAM_TYPE);

        // connectorInfo
        Map<String, Object> connectorDetails = new HashMap<>();
        connectorDetails.put("name", CONNECTOR_TYPE + "_" + connectionName);
        connectorDetails.put("sourceObject", sourceObject);

        ConnectorInput connectorInfo = new ConnectorInput();
        connectorInfo.setConnectorType(CONNECTOR_TYPE);
        connectorInfo.setConnectorDetails(connectorDetails);
        request.setConnectorInfo(connectorInfo);

        // dataLakeObjectInfo
        DataLakeObjectInput dloInfo = new DataLakeObjectInput();
        dloInfo.setName(dloName);
        dloInfo.setCategory(category);
        if (eventDateTimeFieldName != null) {
            dloInfo.setEventDateTimeFieldName(eventDateTimeFieldName);
        }
        DataSpaceInput dataspaceInput = new DataSpaceInput();
        dataspaceInput.setName(dataspace);
        dloInfo.setDataspaceInfo(List.of(dataspaceInput));
        request.setDataLakeObjectInfo(dloInfo);

        return request;
    }
}
