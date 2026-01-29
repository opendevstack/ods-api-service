package org.opendevstack.apiservice.externalservice.aap.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecuteWorkflowAsyncCommand Tests")
class ExecuteWorkflowAsyncCommandTest {

    @Mock
    private AutomationPlatformService automationPlatformService;

    @InjectMocks
    private ExecuteWorkflowAsyncCommand command;

    private ExecuteWorkflowAsyncRequest validRequest;
    private AutomationExecutionResult expectedResult;

    @BeforeEach
    void setUp() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "value1");

        validRequest = ExecuteWorkflowAsyncRequest.builder()
                .workflowName("test-workflow")
                .parameters(parameters)
                .build();

        expectedResult = new AutomationExecutionResult("job-123", "pending", true, "Workflow initiated");
    }

    @Test
    @DisplayName("Should execute workflow asynchronously and return result")
    void execute_withValidRequest_shouldReturnResult() throws Exception {
        CompletableFuture<AutomationExecutionResult> future = CompletableFuture.completedFuture(expectedResult);
        when(automationPlatformService.executeWorkflowAsync("test-workflow", validRequest.getParameters()))
                .thenReturn(future);

        AutomationExecutionResult result = command.execute(validRequest);

        assertNotNull(result);
        assertEquals("job-123", result.getJobId());
        assertTrue(result.isSuccessful());
        verify(automationPlatformService).executeWorkflowAsync("test-workflow", validRequest.getParameters());
    }

    @Test
    @DisplayName("Should throw exception when request is null")
    void execute_withNullRequest_shouldThrowException() {
        assertThrows(ExternalServiceException.class, () -> command.execute(null));
    }

    @Test
    @DisplayName("Should throw exception when workflow name is null")
    void execute_withNullWorkflowName_shouldThrowException() {
        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName(null)
                .build();

        assertThrows(ExternalServiceException.class, () -> command.execute(request));
    }

    @Test
    @DisplayName("Should throw exception when workflow name is empty")
    void execute_withEmptyWorkflowName_shouldThrowException() {
        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName("")
                .build();

        assertThrows(ExternalServiceException.class, () -> command.execute(request));
    }

    @Test
    @DisplayName("Should handle future execution exception")
    void execute_whenFutureFails_shouldWrapException() throws Exception {
        CompletableFuture<AutomationExecutionResult> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Async execution failed"));
        
        when(automationPlatformService.executeWorkflowAsync("test-workflow", validRequest.getParameters()))
                .thenReturn(failedFuture);

        ExternalServiceException exception = assertThrows(ExternalServiceException.class, 
                () -> command.execute(validRequest));
        
        assertEquals("ASYNC_WORKFLOW_EXECUTION_FAILED", exception.getErrorCode());
        assertEquals("aap", exception.getServiceName());
    }

    @Test
    @DisplayName("Should execute with null parameters")
    void execute_withNullParameters_shouldSucceed() throws Exception {
        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName("test-workflow")
                .parameters(null)
                .build();

        CompletableFuture<AutomationExecutionResult> future = CompletableFuture.completedFuture(expectedResult);
        when(automationPlatformService.executeWorkflowAsync("test-workflow", null))
                .thenReturn(future);

        AutomationExecutionResult result = command.execute(request);

        assertNotNull(result);
        assertEquals("job-123", result.getJobId());
    }

    @Test
    @DisplayName("Should include callback URL in request")
    void execute_withCallbackUrl_shouldIncludeInRequest() throws Exception {
        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName("test-workflow")
                .parameters(validRequest.getParameters())
                .callbackUrl("http://callback.example.com/notify")
                .build();

        CompletableFuture<AutomationExecutionResult> future = CompletableFuture.completedFuture(expectedResult);
        when(automationPlatformService.executeWorkflowAsync("test-workflow", request.getParameters()))
                .thenReturn(future);

        AutomationExecutionResult result = command.execute(request);

        assertNotNull(result);
        assertEquals("http://callback.example.com/notify", request.getCallbackUrl());
    }

    @Test
    @DisplayName("Should return correct command name")
    void getCommandName_shouldReturnCorrectName() {
        assertEquals("execute-workflow-async", command.getCommandName());
    }

    @Test
    @DisplayName("Should return correct service name")
    void getServiceName_shouldReturnCorrectName() {
        assertEquals("aap", command.getServiceName());
    }
}
