package com.salesforce.data360.mcp.tools.datastream;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThirdPartyConnectorsDataStreamToolsTest {

    @Mock
    private Data360Client client;

    private ThirdPartyConnectorsDataStreamTools tools;

    @BeforeEach
    void setUp() {
        tools = new ThirdPartyConnectorsDataStreamTools(client);
    }

    private List<Map<String, Object>> buildSampleFields() {
        return List.of(
            Map.of("name", "Internal_Id", "dataType", "Text", "isPrimaryKey", true),
            Map.of("name", "CreatedTime", "dataType", "DateTime", "isPrimaryKey", false, "format", "MM/dd/yyyy HH:mm:ss.SSS"),
            Map.of("name", "Task", "dataType", "Text", "isPrimaryKey", false),
            Map.of("name", "Status", "dataType", "Text", "isPrimaryKey", false),
            Map.of("name", "Duration", "dataType", "Number", "isPrimaryKey", false)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCreateThirdPartyConnectorsDataStream_success() {
        Map<String, Object> mockResponse = Map.of("id", "stream-123", "name", "Tasks_airtable_dev_conn");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        String result = tools.createThirdPartyConnectorsDataStream(
            "airtable_dev_conn", "Airtable_airtable_dev_conn", "Tasks",
            buildSampleFields(),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("stream-123");
        assertThat(result).contains("Tasks_airtable_dev_conn");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams");

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsEntry("name", "Tasks_airtable_dev_conn");
        assertThat(body).containsEntry("label", "Tasks airtable dev conn");
        assertThat(body).containsEntry("datasource", "Airtable_airtable_dev_conn");
        assertThat(body).containsEntry("datastreamType", "CONNECTORSFRAMEWORK");
        assertThat(body).containsEntry("dataAccessMode", "INGEST");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConnectorInfoIsCorrect() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "my_conn", "Airtable_my_conn", "People",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> connectorInfo = (Map<String, Object>) bodyCaptor.getValue().get("connectorInfo");
        assertThat(connectorInfo).containsEntry("connectorType", "DataConnector");
        Map<String, Object> details = (Map<String, Object>) connectorInfo.get("connectorDetails");
        assertThat(details).containsEntry("name", "my_conn");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAdvancedAttributesContainsObjectName() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Tasks",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> advAttrs = (Map<String, Object>) bodyCaptor.getValue().get("advancedAttributes");
        assertThat(advAttrs).containsEntry("objectName", "Tasks");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSourceFieldsIncludeFormat() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            buildSampleFields(),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        List<Map<String, Object>> sourceFields = (List<Map<String, Object>>) bodyCaptor.getValue().get("sourceFields");
        assertThat(sourceFields).hasSize(5);

        Map<String, Object> createdTime = sourceFields.get(1);
        assertThat(createdTime).containsEntry("name", "CreatedTime");
        assertThat(createdTime).containsEntry("dataType", "DateTime");
        assertThat(createdTime).containsEntry("format", "MM/dd/yyyy HH:mm:ss.SSS");

        Map<String, Object> textField = sourceFields.get(2);
        assertThat(textField).doesNotContainKey("format");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMappingsFromFields() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            buildSampleFields(),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        List<Map<String, Object>> mappings = (List<Map<String, Object>>) bodyCaptor.getValue().get("mappings");
        assertThat(mappings).hasSize(5);
        assertThat(mappings.get(0)).containsEntry("sourceFieldLabel", "Internal_Id");
        assertThat(mappings.get(0)).containsEntry("targetFieldName", "Internal_Id");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDataLakeObjectInfo() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "my_conn", "Src_my_conn", "Tasks",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> dloInfo = (Map<String, Object>) bodyCaptor.getValue().get("dataLakeObjectInfo");
        assertThat(dloInfo).containsEntry("name", "Tasks_my_conn__dll");
        assertThat(dloInfo).containsEntry("label", "Tasks my conn");
        assertThat(dloInfo).containsEntry("category", "Profile");
        assertThat(dloInfo).containsKey("capabilities");
        assertThat(dloInfo).containsKey("dataLakeFieldInputRepresentations");
        assertThat(dloInfo).containsKey("dataspaceInfo");

        List<Map<String, Object>> dataspaceInfo = (List<Map<String, Object>>) dloInfo.get("dataspaceInfo");
        assertThat(dataspaceInfo).hasSize(1);
        assertThat(dataspaceInfo.get(0)).containsEntry("name", "default");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRefreshConfigDefaults() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> refreshConfig = (Map<String, Object>) bodyCaptor.getValue().get("refreshConfig");
        assertThat(refreshConfig).containsEntry("refreshMode", "TOTAL_REPLACE");
        assertThat(refreshConfig).containsEntry("fetchImmediately", true);
        Map<String, Object> freq = (Map<String, Object>) refreshConfig.get("frequency");
        assertThat(freq).containsEntry("frequencyType", "NONE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCustomOverrides() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            "MyStream", "My Stream Label", "MyDlo__dll", "My DLO", "Other",
            "custom-ds", "UPSERT", null, null, false,
            null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsEntry("name", "MyStream");
        assertThat(body).containsEntry("label", "My Stream Label");

        Map<String, Object> dloInfo = (Map<String, Object>) body.get("dataLakeObjectInfo");
        assertThat(dloInfo).containsEntry("name", "MyDlo__dll");
        assertThat(dloInfo).containsEntry("label", "My DLO");
        assertThat(dloInfo).containsEntry("category", "Other");

        List<Map<String, Object>> dsInfo = (List<Map<String, Object>>) dloInfo.get("dataspaceInfo");
        assertThat(dsInfo.get(0)).containsEntry("name", "custom-ds");

        Map<String, Object> refreshConfig = (Map<String, Object>) body.get("refreshConfig");
        assertThat(refreshConfig).containsEntry("refreshMode", "UPSERT");
        assertThat(refreshConfig).containsEntry("fetchImmediately", false);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEngagementCategory() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Events",
            List.of(
                Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true),
                Map.of("name", "CreatedDate", "dataType", "DateTime", "isPrimaryKey", false)
            ),
            null, null, null, null, "Engagement", null, null, null, "CreatedDate", null,
            null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> dloInfo = (Map<String, Object>) bodyCaptor.getValue().get("dataLakeObjectInfo");
        assertThat(dloInfo).containsEntry("category", "Engagement");
        assertThat(dloInfo).containsEntry("eventDateTimeFieldName", "CreatedDate");
    }

    @Test
    void testEngagementRequiresEventDateField() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Events",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, "Engagement", null, null, null, null, null,
            null, null, null, null);

        assertThat(result).contains("eventDateTimeFieldName is required");
    }

    @Test
    void testMissingConnectionName() {
        String result = tools.createThirdPartyConnectorsDataStream(
            null, "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("connectionName is required");
    }

    @Test
    void testEmptyFields() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("At least one field is required");
    }

    @Test
    void testNoPrimaryKey() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Col", "dataType", "Text", "isPrimaryKey", false)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("isPrimaryKey=true");
    }

    @Test
    void testNormalizesConnectorDataTypes() {
        List<Map<String, Object>> normalized = ThirdPartyConnectorsDataStreamTools.normalizeFields(List.of(
            Map.of("name", "id", "dataType", "VARCHAR", "isPrimaryKey", true),
            Map.of("name", "amount", "dataType", "FLOAT", "isPrimaryKey", false),
            Map.of("name", "ts", "dataType", "TIMESTAMP", "isPrimaryKey", false)
        ));

        assertThat(normalized.get(0)).containsEntry("dataType", "Text");
        assertThat(normalized.get(1)).containsEntry("dataType", "Number");
        assertThat(normalized.get(2)).containsEntry("dataType", "DateTime");
    }

    @Test
    void testUnsupportedDataType() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Col", "dataType", "BLOB", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("Unsupported dataType");
    }

    @Test
    void testApiError() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad request", "/data-streams"));

        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("error");
        assertThat(result).contains("Bad request");
        assertThat(result).contains("400");
    }

    @Test
    void testConnectionError() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException("Connection failed", new RuntimeException("timeout")));

        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("Connection failed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFieldLabelDefaultsToName() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "MyField", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> dloInfo = (Map<String, Object>) bodyCaptor.getValue().get("dataLakeObjectInfo");
        List<Map<String, Object>> dlFields = (List<Map<String, Object>>) dloInfo.get("dataLakeFieldInputRepresentations");
        assertThat(dlFields.get(0)).containsEntry("label", "MyField");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testExplicitFieldLabel() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "my_field", "label", "My Field", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> dloInfo = (Map<String, Object>) bodyCaptor.getValue().get("dataLakeObjectInfo");
        List<Map<String, Object>> dlFields = (List<Map<String, Object>>) dloInfo.get("dataLakeFieldInputRepresentations");
        assertThat(dlFields.get(0)).containsEntry("label", "My Field");
    }

    @Test
    void testInvalidCategory() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, "Invalid", null, null, null, null, null,
            null, null, null, null);

        assertThat(result).contains("Invalid category");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHourlyFrequency() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "HOURLY", null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> refreshConfig = (Map<String, Object>) bodyCaptor.getValue().get("refreshConfig");
        Map<String, Object> freq = (Map<String, Object>) refreshConfig.get("frequency");
        assertThat(freq).containsEntry("frequencyType", "HOURLY");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDailyFrequency() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "DAILY", List.of(14), null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> refreshConfig = (Map<String, Object>) bodyCaptor.getValue().get("refreshConfig");
        Map<String, Object> freq = (Map<String, Object>) refreshConfig.get("frequency");
        assertThat(freq).containsEntry("frequencyType", "DAILY");
        assertThat(freq).containsEntry("hours", List.of(14));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWeeklyFrequency() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "WEEKLY", List.of(13), "WEDNESDAY", null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> refreshConfig = (Map<String, Object>) bodyCaptor.getValue().get("refreshConfig");
        Map<String, Object> freq = (Map<String, Object>) refreshConfig.get("frequency");
        assertThat(freq).containsEntry("frequencyType", "WEEKLY");
        assertThat(freq).containsEntry("hours", List.of(13));
        assertThat(freq).containsEntry("refreshDayOfWeek", "WEDNESDAY");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMonthlyFrequency() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "MONTHLY", List.of(19), null, List.of(6));

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> refreshConfig = (Map<String, Object>) bodyCaptor.getValue().get("refreshConfig");
        Map<String, Object> freq = (Map<String, Object>) refreshConfig.get("frequency");
        assertThat(freq).containsEntry("frequencyType", "MONTHLY");
        assertThat(freq).containsEntry("hours", List.of(19));
        assertThat(freq).containsEntry("refreshDayOfMonth", List.of(6));
    }

    @Test
    void testWeeklyFrequencyMissingDayOfWeek() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "WEEKLY", List.of(13), null, null);

        assertThat(result).contains("refreshDayOfWeek is required");
    }

    @Test
    void testMonthlyFrequencyMissingDayOfMonth() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "MONTHLY", List.of(19), null, null);

        assertThat(result).contains("refreshDayOfMonth is required");
    }

    @Test
    void testDailyFrequencyMissingHours() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "DAILY", null, null, null);

        assertThat(result).contains("hours is required");
    }

    @Test
    void testInvalidFrequencyType() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, null, null, null, null,
            "BIWEEKLY", null, null, null);

        assertThat(result).contains("Invalid frequencyType");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIncrementalRefreshMode() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "x"));

        tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "People",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, "INCREMENTAL", "CreatedTime", null, null,
            null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        Map<String, Object> advAttrs = (Map<String, Object>) body.get("advancedAttributes");
        assertThat(advAttrs).containsEntry("objectName", "People");
        assertThat(advAttrs).containsEntry("incrementalColumn", "CreatedTime");

        Map<String, Object> refreshConfig = (Map<String, Object>) body.get("refreshConfig");
        assertThat(refreshConfig).containsEntry("refreshMode", "INCREMENTAL");
    }

    @Test
    void testIncrementalRequiresIncrementalColumn() {
        String result = tools.createThirdPartyConnectorsDataStream(
            "conn", "Src_conn", "Obj",
            List.of(Map.of("name", "Id", "dataType", "Text", "isPrimaryKey", true)),
            null, null, null, null, null, null, "INCREMENTAL", null, null, null,
            null, null, null, null);

        assertThat(result).contains("incrementalColumn is required");
    }
}