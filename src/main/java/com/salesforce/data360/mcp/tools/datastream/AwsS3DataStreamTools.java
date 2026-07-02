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
import com.salesforce.data360.mcp.model.request.datastream.DataStreamCreateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * Data stream creation tool for the AWS S3 connector.
 * Pre-fills S3-specific values (datastreamType, connectorType) so the caller
 * does not need to specify them.
 */
@Component
public class AwsS3DataStreamTools extends AbstractConnectorDataStreamTools {

    private static final String DATASTREAM_TYPE = "CONNECTORSFRAMEWORK";
    private static final String CONNECTOR_TYPE = "DataConnector";

    public AwsS3DataStreamTools(Data360Client client) {
        super(client);
    }

    /**
     * Create a data stream that ingests from an AWS S3 bucket.
     */
    @ApiEndpoint(path = "/ssot/data-streams", verb = "POST")
    @McpTool(
            name = "d360_datastream_create_aws_s3",
            description = "Create a data stream from an AWS S3 file. "
                    + "PREREQUISITE: Before calling this tool, call 'd360_connection_list' with connectorType=AwsS3 "
                    + "and present the user with the available connections so they can select the connection to use. "
                    + "The datastreamType and connectorInfo.connectorType are automatically set. "
                    + "Provide name, label, connectorInfo.connectorDetails, advancedAttributes (fileType, importDirectory, fileName, delimiter, headerlessRetrievalEnabled), "
                    + "dataLakeObjectInfo (with fields), sourceFields, mappings, and refreshConfig in the request body."
    )
    public String createS3DataStream(
            @McpToolParam(description = "Data stream creation request body. datastreamType and connectorInfo.connectorType are auto-set.") DataStreamCreateRequest request
    ) {
        request.setDatastreamType(DATASTREAM_TYPE);

        ConnectorInput connectorInfo = request.getConnectorInfo();
        if (connectorInfo == null) {
            connectorInfo = new ConnectorInput();
            request.setConnectorInfo(connectorInfo);
        }
        connectorInfo.setConnectorType(CONNECTOR_TYPE);

        return super.createDataStream(request);
    }
}
