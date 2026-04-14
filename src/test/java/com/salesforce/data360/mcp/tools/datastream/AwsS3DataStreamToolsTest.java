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
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.datastream.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsS3DataStreamToolsTest {

    @Mock
    private Data360Client client;

    private AwsS3DataStreamTools tools;

    @BeforeEach
    void setUp() {
        tools = new AwsS3DataStreamTools(client);
    }

    private DataStreamCreateRequest buildSampleRequest() {
        DataStreamCreateRequest request = new DataStreamCreateRequest();
        request.setName("AirlineBookings_S3");
        request.setLabel("Airline Bookings (S3)");
        request.setDataAccessMode("INGEST");
        request.setDatasource("AwsS3");

        // connectorInfo — connectorType will be overridden by the tool
        ConnectorInput connectorInfo = new ConnectorInput();
        Map<String, Object> connectorDetails = new HashMap<>();
        connectorDetails.put("name", "test");
        connectorInfo.setConnectorDetails(connectorDetails);
        request.setConnectorInfo(connectorInfo);

        // advancedAttributes
        Map<String, Object> advAttrs = new LinkedHashMap<>();
        advAttrs.put("fileType", "CSV");
        advAttrs.put("importDirectory", "/");
        advAttrs.put("fileName", "bookings.csv");
        advAttrs.put("delimiter", ",");
        advAttrs.put("headerlessRetrievalEnabled", false);
        request.setAdvancedAttributes(advAttrs);

        // DLO info
        DataLakeObjectInput dloInfo = new DataLakeObjectInput();
        dloInfo.setName("AirlineBookings_S3__dll");
        dloInfo.setLabel("Airline Bookings");
        dloInfo.setCategory("Other");

        DataSpaceInput dataspaceInput = new DataSpaceInput();
        dataspaceInput.setName("default");
        dloInfo.setDataspaceInfo(List.of(dataspaceInput));

        DataLakeFieldInput idField = new DataLakeFieldInput();
        idField.setName("booking_id");
        idField.setLabel("Booking Id");
        idField.setDataType("Text");
        idField.setIsPrimaryKey(true);

        DataLakeFieldInput priceField = new DataLakeFieldInput();
        priceField.setName("ticket_price_usd");
        priceField.setLabel("Ticket Price USD");
        priceField.setDataType("Number");
        priceField.setIsPrimaryKey(false);

        dloInfo.setDataLakeFieldInputRepresentations(List.of(idField, priceField));
        request.setDataLakeObjectInfo(dloInfo);

        // sourceFields
        DataStreamSourceFieldInput sf1 = new DataStreamSourceFieldInput();
        sf1.setName("booking_id");
        sf1.setDataType("Text");
        DataStreamSourceFieldInput sf2 = new DataStreamSourceFieldInput();
        sf2.setName("ticket_price_usd");
        sf2.setDataType("Number");
        request.setSourceFields(List.of(sf1, sf2));

        // mappings
        DataStreamFieldMappingInput m1 = new DataStreamFieldMappingInput();
        m1.setSourceFieldLabel("booking_id");
        m1.setTargetFieldName("booking_id");
        m1.setTargetFieldReturntype("Text");
        DataStreamFieldMappingInput m2 = new DataStreamFieldMappingInput();
        m2.setSourceFieldLabel("ticket_price_usd");
        m2.setTargetFieldName("ticket_price_usd");
        m2.setTargetFieldReturntype("Number");
        request.setMappings(List.of(m1, m2));

        // refreshConfig
        RefreshConfigInput refreshConfig = new RefreshConfigInput();
        refreshConfig.setRefreshMode("UPSERT");
        refreshConfig.setIsAccelerationEnabled(false);
        DataStreamFrequencyInput freq = new DataStreamFrequencyInput();
        freq.setFrequencyType("Daily");
        freq.setHours(List.of(6));
        refreshConfig.setFrequency(freq);
        request.setRefreshConfig(refreshConfig);

        return request;
    }

    @Test
    void testCreateS3DataStream_success() {
        Map<String, Object> mockResponse = Map.of("id", "stream-s3-123", "name", "AirlineBookings_S3");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        String result = tools.createS3DataStream(buildSampleRequest(), null);

        assertThat(result).contains("stream-s3-123");
        assertThat(result).contains("AirlineBookings_S3");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams");

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsEntry("name", "AirlineBookings_S3");
        assertThat(body).containsEntry("label", "Airline Bookings (S3)");
        assertThat(body).containsEntry("datasource", "AwsS3");
        assertThat(body).containsEntry("datastreamType", "CONNECTORSFRAMEWORK");
        assertThat(body).containsEntry("dataAccessMode", "INGEST");
    }

    @Test
    void testHardcodesDatastreamType() {
        Map<String, Object> mockResponse = Map.of("id", "stream-type");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        DataStreamCreateRequest request = buildSampleRequest();
        request.setDatastreamType("ShouldBeOverridden");

        tools.createS3DataStream(request, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).containsEntry("datastreamType", "CONNECTORSFRAMEWORK");
    }

    @Test
    void testHardcodesConnectorType() {
        Map<String, Object> mockResponse = Map.of("id", "stream-ct");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        DataStreamCreateRequest request = buildSampleRequest();
        request.getConnectorInfo().setConnectorType("ShouldBeOverridden");

        tools.createS3DataStream(request, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));
        Map<String, Object> connectorInfo = (Map<String, Object>) bodyCaptor.getValue().get("connectorInfo");
        assertThat(connectorInfo).containsEntry("connectorType", "DataConnector");
    }

    @Test
    void testSetsConnectorTypeWhenConnectorInfoMissing() {
        Map<String, Object> mockResponse = Map.of("id", "stream-no-ci");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        DataStreamCreateRequest request = buildSampleRequest();
        request.setConnectorInfo(null);

        tools.createS3DataStream(request, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));
        Map<String, Object> connectorInfo = (Map<String, Object>) bodyCaptor.getValue().get("connectorInfo");
        assertThat(connectorInfo).containsEntry("connectorType", "DataConnector");
    }

    @Test
    void testAdvancedAttributes() {
        Map<String, Object> mockResponse = Map.of("id", "stream-adv");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        tools.createS3DataStream(buildSampleRequest(), null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> advAttrs = (Map<String, Object>) bodyCaptor.getValue().get("advancedAttributes");
        assertThat(advAttrs).containsEntry("fileType", "CSV");
        assertThat(advAttrs).containsEntry("importDirectory", "/");
        assertThat(advAttrs).containsEntry("fileName", "bookings.csv");
        assertThat(advAttrs).containsEntry("delimiter", ",");
        assertThat(advAttrs).containsEntry("headerlessRetrievalEnabled", false);
    }

    @Test
    void testSourceFieldsAndMappings() {
        Map<String, Object> mockResponse = Map.of("id", "stream-fields");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        tools.createS3DataStream(buildSampleRequest(), null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();

        List<Map<String, Object>> sourceFields = (List<Map<String, Object>>) body.get("sourceFields");
        assertThat(sourceFields).hasSize(2);
        assertThat(sourceFields.get(0)).containsEntry("name", "booking_id");

        List<Map<String, Object>> mappings = (List<Map<String, Object>>) body.get("mappings");
        assertThat(mappings).hasSize(2);
        assertThat(mappings.get(0)).containsEntry("sourceFieldLabel", "booking_id");
        assertThat(mappings.get(0)).containsEntry("targetFieldName", "booking_id");
    }

    @Test
    void testRefreshConfig() {
        Map<String, Object> mockResponse = Map.of("id", "stream-refresh");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        tools.createS3DataStream(buildSampleRequest(), null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> refreshConfig = (Map<String, Object>) bodyCaptor.getValue().get("refreshConfig");
        assertThat(refreshConfig).containsEntry("refreshMode", "UPSERT");
        assertThat(refreshConfig).containsEntry("isAccelerationEnabled", false);
    }

    @Test
    void testWithDataspace() {
        Map<String, Object> mockResponse = Map.of("id", "stream-ds");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        tools.createS3DataStream(buildSampleRequest(), "custom-dataspace");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("dataspace=custom-dataspace");
    }

    @Test
    void testApiError() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad request", "/ssot/data-streams"));

        String result = tools.createS3DataStream(buildSampleRequest(), null);

        assertThat(result).contains("error");
        assertThat(result).contains("Bad request");
        assertThat(result).contains("400");
    }

    @Test
    void testConnectionError() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException("Connection failed", new RuntimeException("timeout")));

        String result = tools.createS3DataStream(buildSampleRequest(), null);

        assertThat(result).contains("Connection failed");
    }
}
