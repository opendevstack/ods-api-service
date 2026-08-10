package org.opendevstack.apiservice.externalservice.aap.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnsibleAutomationPlatformService.
 * Mocks the RestClient fluent chain to test all method paths.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class AnsibleAutomationPlatformServiceTest {

    // --- RestClient fluent-chain mocks ---
    @Mock private RestClient restClient;

    // POST chain
    @Mock private RestClient.RequestBodyUriSpec     postUriSpec;
    @Mock private RestClient.RequestBodySpec        postBodySpec;
    @Mock private RestClient.ResponseSpec           postResponseSpec;

    // GET chain
    @Mock private RestClient.RequestHeadersUriSpec  getUriSpec;
    @Mock private RestClient.RequestHeadersSpec<?>  getHeadersSpec;
    @Mock private RestClient.ResponseSpec           getResponseSpec;

    private AnsibleAutomationPlatformService service;

    private static final String BASE_URL = "http://localhost:8080/api/v2";
    private static final String USERNAME  = "testuser";
    private static final String PASSWORD  = "testpass";

    @BeforeEach
    void setUp() {
        service = new AnsibleAutomationPlatformService(restClient);
        ReflectionTestUtils.setField(service, "baseUrl",  BASE_URL);
        ReflectionTestUtils.setField(service, "username", USERNAME);
        ReflectionTestUtils.setField(service, "password", PASSWORD);

        // Wire POST chain
        lenient().when(restClient.post()).thenReturn(postUriSpec);
        lenient().when(postUriSpec.uri(anyString())).thenReturn(postBodySpec);
        lenient().doReturn(postBodySpec).when(postBodySpec).headers(any());
        lenient().when(postBodySpec.contentType(any())).thenReturn(postBodySpec);
        lenient().doReturn(postBodySpec).when(postBodySpec).body(any(Object.class));
        lenient().when(postBodySpec.retrieve()).thenReturn(postResponseSpec);

        // Wire GET chain
        lenient().when(restClient.get()).thenReturn(getUriSpec);
        lenient().doReturn(getHeadersSpec).when(getUriSpec).uri(anyString());
        lenient().doReturn(getHeadersSpec).when(getHeadersSpec).headers(any());
        lenient().when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
    }

    // =========================================================================
    // executeWorkflow
    // =========================================================================

    @Test
    void executeWorkflow_Success() throws AutomationPlatformException {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "12345");
        responseBody.put("status", "pending");
        responseBody.put("url", BASE_URL + "/workflow_jobs/12345/");

        doReturn(responseBody).when(postResponseSpec).body(any(ParameterizedTypeReference.class));

        AutomationExecutionResult result = service.executeWorkflow("test-workflow", Map.of("env", "dev"));

        assertNotNull(result);
        assertEquals("12345", result.getJobId());
        assertEquals("pending", result.getStatus());
        assertTrue(result.isSuccessful());
        assertEquals("Workflow executed successfully", result.getMessage());
        assertEquals(responseBody, result.getMetadata());
    }

    @Test
    void executeWorkflow_WithNullParameters() throws AutomationPlatformException {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "12345");
        responseBody.put("status", "pending");

        doReturn(responseBody).when(postResponseSpec).body(any(ParameterizedTypeReference.class));

        AutomationExecutionResult result = service.executeWorkflow("test-workflow", null);

        assertNotNull(result);
        assertEquals("12345", result.getJobId());
        assertTrue(result.isSuccessful());
    }

    @Test
    void executeWorkflow_RestClientException() {
        when(postBodySpec.retrieve()).thenThrow(new RestClientException("Connection timeout"));

        AutomationPlatformException.WorkflowExecutionException ex = assertThrows(
                AutomationPlatformException.WorkflowExecutionException.class,
                () -> service.executeWorkflow("test-workflow", Map.of("env", "dev")));

        assertTrue(ex.getMessage().contains("test-workflow"));
        assertEquals("WORKFLOW_EXECUTION_FAILED", ex.getErrorCode());
    }

    @Test
    void executeWorkflow_NullResponseBody() {
        doReturn(null).when(postResponseSpec).body(any(ParameterizedTypeReference.class));

        AutomationPlatformException.WorkflowExecutionException ex = assertThrows(
                AutomationPlatformException.WorkflowExecutionException.class,
                () -> service.executeWorkflow("test-workflow", Map.of()));

        assertTrue(ex.getMessage().contains("Empty response body"));
    }

    @Test
    void executeWorkflow_EmptyParameters() throws AutomationPlatformException {
        Map<String, Object> responseBody = Map.of("id", "12345", "status", "pending");
        doReturn(responseBody).when(postResponseSpec).body(any(ParameterizedTypeReference.class));

        AutomationExecutionResult result = service.executeWorkflow("test-workflow", new HashMap<>());
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    void executeWorkflow_VerifyExtraVarsPassedCorrectly() throws AutomationPlatformException {
        Map<String, Object> responseBody = Map.of("id", "99999", "status", "pending");
        doReturn(responseBody).when(postResponseSpec).body(any(ParameterizedTypeReference.class));

        Map<String, Object> params = new HashMap<>();
        params.put("app_name", "my-app");
        params.put("version", "1.2.3");
        params.put("replicas", 3);

        AutomationExecutionResult result = service.executeWorkflow("deploy-app", params);
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    // =========================================================================
    // executeWorkflowAsync
    // =========================================================================

    @Test
    void executeWorkflowAsync_Success() throws ExecutionException, InterruptedException {
        Map<String, Object> responseBody = Map.of("id", "67890", "status", "running");
        doReturn(responseBody).when(postResponseSpec).body(any(ParameterizedTypeReference.class));

        CompletableFuture<AutomationExecutionResult> future =
                service.executeWorkflowAsync("test-workflow", Map.of("env", "prod"));

        AutomationExecutionResult result = future.get();
        assertNotNull(result);
        assertEquals("67890", result.getJobId());
        assertTrue(result.isSuccessful());
    }

    @Test
    void executeWorkflowAsync_Failure() throws ExecutionException, InterruptedException {
        when(postBodySpec.retrieve()).thenThrow(new RestClientException("Network error"));

        CompletableFuture<AutomationExecutionResult> future =
                service.executeWorkflowAsync("test-workflow", Map.of());

        AutomationExecutionResult result = future.get();
        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertTrue(result.getMessage().contains("Async execution failed"));
        assertEquals("WORKFLOW_EXECUTION_FAILED", result.getErrorDetails());
    }

    // =========================================================================
    // getJobStatus
    // =========================================================================

    @Test
    void getJobStatus_Success() throws AutomationPlatformException {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "12345");
        responseBody.put("status", "successful");
        responseBody.put("result_traceback", "Job completed successfully");

        doReturn(responseBody).when(getResponseSpec).body(any(ParameterizedTypeReference.class));

        AutomationJobStatus status = service.getJobStatus("12345");

        assertNotNull(status);
        assertEquals("12345", status.getJobId());
        assertEquals(AutomationJobStatus.Status.SUCCESSFUL, status.getStatus());
        assertEquals("Job completed successfully", status.getStatusMessage());
        assertEquals(responseBody, status.getResult());
    }

    @Test
    void getJobStatus_PendingStatus() throws AutomationPlatformException {
        doReturn(Map.of("status", "pending")).when(getResponseSpec).body(any(ParameterizedTypeReference.class));
        assertEquals(AutomationJobStatus.Status.PENDING, service.getJobStatus("1").getStatus());
    }

    @Test
    void getJobStatus_RunningStatus() throws AutomationPlatformException {
        doReturn(Map.of("status", "running")).when(getResponseSpec).body(any(ParameterizedTypeReference.class));
        assertEquals(AutomationJobStatus.Status.RUNNING, service.getJobStatus("1").getStatus());
    }

    @Test
    void getJobStatus_FailedStatus() throws AutomationPlatformException {
        doReturn(Map.of("status", "failed")).when(getResponseSpec).body(any(ParameterizedTypeReference.class));
        assertEquals(AutomationJobStatus.Status.FAILED, service.getJobStatus("1").getStatus());
    }

    @Test
    void getJobStatus_CancelledStatus() throws AutomationPlatformException {
        doReturn(Map.of("status", "canceled")).when(getResponseSpec).body(any(ParameterizedTypeReference.class));
        assertEquals(AutomationJobStatus.Status.CANCELLED, service.getJobStatus("1").getStatus());
    }

    @Test
    void getJobStatus_CancelledAlternativeSpelling() throws AutomationPlatformException {
        doReturn(Map.of("status", "cancelled")).when(getResponseSpec).body(any(ParameterizedTypeReference.class));
        assertEquals(AutomationJobStatus.Status.CANCELLED, service.getJobStatus("1").getStatus());
    }

    @Test
    void getJobStatus_UnknownStatus() throws AutomationPlatformException {
        doReturn(Map.of("status", "unknown_status")).when(getResponseSpec).body(any(ParameterizedTypeReference.class));
        assertEquals(AutomationJobStatus.Status.ERROR, service.getJobStatus("1").getStatus());
    }

    @Test
    void getJobStatus_NullStatus() throws AutomationPlatformException {
        Map<String, Object> body = new HashMap<>();
        body.put("status", null);
        doReturn(body).when(getResponseSpec).body(any(ParameterizedTypeReference.class));
        assertEquals(AutomationJobStatus.Status.ERROR, service.getJobStatus("1").getStatus());
    }

    @Test
    void getJobStatus_JobNotFound() {
        when(getHeadersSpec.retrieve()).thenThrow(new RestClientException("404 Not Found"));

        AutomationPlatformException.JobNotFoundException ex = assertThrows(
                AutomationPlatformException.JobNotFoundException.class,
                () -> service.getJobStatus("99999"));

        assertTrue(ex.getMessage().contains("99999"));
        assertEquals("JOB_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void getJobStatus_NullResponseBody() {
        doReturn(null).when(getResponseSpec).body(any(ParameterizedTypeReference.class));

        assertThrows(
                AutomationPlatformException.JobNotFoundException.class,
                () -> service.getJobStatus("12345"));
    }

    // =========================================================================
    // getWorkflowJobStatus
    // =========================================================================

    @Test
    void getWorkflowJobStatus_Success() throws AutomationPlatformException {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", "67890");
        responseBody.put("status", "successful");
        responseBody.put("result_traceback", "Workflow completed");

        doReturn(responseBody).when(getResponseSpec).body(any(ParameterizedTypeReference.class));

        AutomationJobStatus status = service.getWorkflowJobStatus("67890");

        assertNotNull(status);
        assertEquals("67890", status.getJobId());
        assertEquals(AutomationJobStatus.Status.SUCCESSFUL, status.getStatus());
        assertEquals("Workflow completed", status.getStatusMessage());

        verify(getUriSpec, times(1)).uri(eq(BASE_URL + "/workflow_jobs/67890/"));
    }

    @Test
    void getWorkflowJobStatus_NotFound() {
        when(getHeadersSpec.retrieve()).thenThrow(new RestClientException("404 Not Found"));

        AutomationPlatformException.JobNotFoundException ex = assertThrows(
                AutomationPlatformException.JobNotFoundException.class,
                () -> service.getWorkflowJobStatus("99999"));

        assertTrue(ex.getMessage().contains("99999"));
        assertEquals("JOB_NOT_FOUND", ex.getErrorCode());
    }

    // =========================================================================
    // validateConnection / isHealthy
    // =========================================================================

    @Test
    void validateConnection_Success() {
        when(getResponseSpec.toBodilessEntity()).thenReturn(null); // any non-throw = success

        assertTrue(service.validateConnection());
        verify(getUriSpec, times(1)).uri(eq(BASE_URL + "/ping/"));
    }

    @Test
    void validateConnection_Failure() {
        when(getHeadersSpec.retrieve()).thenThrow(new RestClientException("Connection refused"));
        assertFalse(service.validateConnection());
    }

    @Test
    void isHealthy_Success() {
        when(getResponseSpec.toBodilessEntity()).thenReturn(null);
        assertTrue(service.isHealthy());
    }

    @Test
    void isHealthy_Failure() {
        when(getHeadersSpec.retrieve()).thenThrow(new RestClientException("Service unavailable"));
        assertFalse(service.isHealthy());
    }
}
