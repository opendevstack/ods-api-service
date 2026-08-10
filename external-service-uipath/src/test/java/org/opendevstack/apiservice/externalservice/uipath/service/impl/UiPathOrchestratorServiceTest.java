package org.opendevstack.apiservice.externalservice.uipath.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.uipath.config.UiPathProperties;
import org.opendevstack.apiservice.externalservice.uipath.exception.UiPathException;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathAuthResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathODataResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UiPathOrchestratorServiceImpl.
 * Mocks the RestClient fluent chain to test all method paths.
 */
@ExtendWith(MockitoExtension.class)
class UiPathOrchestratorServiceTest {

    // --- RestClient fluent-chain mocks ---
    @Mock private RestClient restClient;

    // POST chain
    @Mock private RestClient.RequestBodyUriSpec    postUriSpec;
    @Mock private RestClient.RequestBodySpec       postBodySpec;
    @Mock private RestClient.ResponseSpec          postResponseSpec;

    // GET chain
    @Mock private RestClient.RequestHeadersUriSpec getUriSpec;
    @Mock private RestClient.RequestHeadersSpec<?> getHeadersSpec;
    @Mock private RestClient.ResponseSpec          getResponseSpec;

    private UiPathProperties properties;
    private UiPathOrchestratorServiceImpl service;

    private static final String HOST = "https://orchestrator.example.com";

    @BeforeEach
    void setUp() {
        properties = new UiPathProperties();
        properties.setHost(HOST);
        properties.setClientId("testuser");
        properties.setClientSecret("testpass");
        properties.setTenancyName("default");
        properties.setOrganizationUnitId("123");
        properties.setTimeout(30000);

        service = new UiPathOrchestratorServiceImpl(restClient, properties);

        // Wire POST chain
        lenient().when(restClient.post()).thenReturn(postUriSpec);
        lenient().when(postUriSpec.uri(anyString())).thenReturn(postBodySpec);
        lenient().when(postBodySpec.contentType(any())).thenReturn(postBodySpec);
        lenient().doReturn(postBodySpec).when(postBodySpec).body(any(Object.class));
        lenient().doReturn(postBodySpec).when(postBodySpec).headers(any());
        lenient().when(postBodySpec.retrieve()).thenReturn(postResponseSpec);

        // Wire GET chain
        lenient().when(restClient.get()).thenReturn(getUriSpec);
        lenient().doReturn(getHeadersSpec).when(getUriSpec).uri(anyString());
        lenient().doReturn(getHeadersSpec).when(getHeadersSpec).headers(any());
        lenient().when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
    }

    // helper: configure a successful auth response
    private void givenAuthSucceeds(String token) {
        UiPathAuthResponse auth = new UiPathAuthResponse();
        auth.setSuccess(true);
        auth.setResult(token);
        when(postResponseSpec.body(UiPathAuthResponse.class)).thenReturn(auth);
    }

    // =========================================================================
    // authenticate
    // =========================================================================

    @Test
    void authenticate_Success() throws Exception {
        givenAuthSucceeds("test-token-12345");

        String token = service.authenticate();

        assertNotNull(token);
        assertEquals("test-token-12345", token);
    }

    @Test
    void authenticate_Failure() {
        UiPathAuthResponse auth = new UiPathAuthResponse();
        auth.setSuccess(false);
        auth.setError("Invalid credentials");
        when(postResponseSpec.body(UiPathAuthResponse.class)).thenReturn(auth);

        assertThrows(UiPathException.AuthenticationException.class, () -> service.authenticate());
    }

    // =========================================================================
    // addQueueItem
    // =========================================================================

    @Test
    void addQueueItem_Success() throws Exception {
        // Auth returns token on first call; queue item returned on second call
        UiPathAuthResponse auth = new UiPathAuthResponse();
        auth.setSuccess(true);
        auth.setResult("test-token");

        UiPathQueueItem createdItem = new UiPathQueueItem();
        createdItem.setId(12345L);
        createdItem.setReference("TEST-001");
        createdItem.setStatus("NEW");

        when(postResponseSpec.body(UiPathAuthResponse.class)).thenReturn(auth);
        when(postResponseSpec.body(UiPathQueueItem.class)).thenReturn(createdItem);

        Map<String, Object> content = new HashMap<>();
        content.put("Project Key", "TEST-001");

        UiPathQueueItemRequest request = UiPathQueueItemRequest.builder()
                .queueName("Q_EDP_Project_Requests")
                .reference("TEST-001")
                .specificContent(content)
                .build();

        UiPathQueueItem result = service.addQueueItem(request);

        assertNotNull(result);
        assertEquals(12345L, result.getId());
        assertEquals("TEST-001", result.getReference());
        assertEquals("NEW", result.getStatus());
    }

    // =========================================================================
    // getQueueItemById
    // =========================================================================

    @Test
    void getQueueItemById_Success() throws Exception {
        givenAuthSucceeds("test-token");

        UiPathQueueItem queueItem = new UiPathQueueItem();
        queueItem.setId(12345L);
        queueItem.setReference("TEST-001");
        queueItem.setStatus("SUCCESSFUL");

        when(getResponseSpec.body(UiPathQueueItem.class)).thenReturn(queueItem);

        UiPathQueueItem result = service.getQueueItemById(12345L);

        assertNotNull(result);
        assertEquals(12345L, result.getId());
        assertEquals("SUCCESSFUL", result.getStatus());
        assertTrue(result.isSuccessful());
        assertTrue(result.isFinalized());
    }

    @Test
    void getQueueItemById_NotFound() throws Exception {
        givenAuthSucceeds("test-token");
        when(getHeadersSpec.retrieve()).thenThrow(new RestClientException("404 Not Found"));

        assertThrows(UiPathException.QueueItemNotFoundException.class,
                () -> service.getQueueItemById(99999L));
    }

    // =========================================================================
    // getQueueItemsByReference
    // =========================================================================

    @Test
    void getQueueItemsByReference_Success() throws Exception {
        givenAuthSucceeds("test-token");

        UiPathQueueItem item1 = new UiPathQueueItem();
        item1.setId(100L);
        item1.setReference("TEST-001");
        item1.setStatus("FAILED");

        UiPathQueueItem item2 = new UiPathQueueItem();
        item2.setId(200L);
        item2.setReference("TEST-001");
        item2.setStatus("SUCCESSFUL");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item1, item2));

        when(getResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(odataResponse);

        List<UiPathQueueItem> results = service.getQueueItemsByReference("TEST-001");

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    // =========================================================================
    // getLatestQueueItemByReference
    // =========================================================================

    @Test
    void getLatestQueueItemByReference_Success() throws Exception {
        givenAuthSucceeds("test-token");

        UiPathQueueItem item1 = new UiPathQueueItem();
        item1.setId(100L);
        item1.setStatus("FAILED");

        UiPathQueueItem item2 = new UiPathQueueItem();
        item2.setId(200L);
        item2.setStatus("SUCCESSFUL");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item1, item2));

        when(getResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(odataResponse);

        Optional<UiPathQueueItem> result = service.getLatestQueueItemByReference("TEST-001");

        assertTrue(result.isPresent());
        assertEquals(200L, result.get().getId());
        assertEquals("SUCCESSFUL", result.get().getStatus());
    }

    @Test
    void getLatestQueueItemByReference_NotFound() throws Exception {
        givenAuthSucceeds("test-token");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of());

        when(getResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(odataResponse);

        Optional<UiPathQueueItem> result = service.getLatestQueueItemByReference("NONEXISTENT");

        assertFalse(result.isPresent());
    }

    // =========================================================================
    // hasQueueItemFinalized
    // =========================================================================

    @Test
    void hasQueueItemFinalized_Success() throws Exception {
        givenAuthSucceeds("test-token");

        UiPathQueueItem item = new UiPathQueueItem();
        item.setId(200L);
        item.setStatus("SUCCESSFUL");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item));

        when(getResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(odataResponse);

        assertTrue(service.hasQueueItemFinalized("TEST-001"));
    }

    @Test
    void hasQueueItemFinalized_StillProcessing() throws Exception {
        givenAuthSucceeds("test-token");

        UiPathQueueItem item = new UiPathQueueItem();
        item.setId(200L);
        item.setStatus("IN_PROGRESS");

        UiPathODataResponse<UiPathQueueItem> odataResponse = new UiPathODataResponse<>();
        odataResponse.setValue(List.of(item));

        when(getResponseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(odataResponse);

        assertFalse(service.hasQueueItemFinalized("TEST-001"));
    }

    // =========================================================================
    // validateConnection / isHealthy
    // =========================================================================

    @Test
    void validateConnection_Success() throws Exception {
        givenAuthSucceeds("test-token");
        assertTrue(service.validateConnection());
    }

    @Test
    void validateConnection_Failure() {
        when(postBodySpec.retrieve()).thenThrow(new RestClientException("Connection refused"));
        assertFalse(service.validateConnection());
    }
}
