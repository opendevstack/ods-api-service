package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Command for executing a workflow asynchronously on the Automation Platform.
 * This command initiates the workflow and returns immediately with the job ID,
 * allowing the caller to poll for status separately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecuteWorkflowAsyncCommand implements ExternalServiceCommand<ExecuteWorkflowAsyncRequest, AutomationExecutionResult> {
    
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    
    private final AutomationPlatformService automationPlatformService;
    
    @Override
    public AutomationExecutionResult execute(ExecuteWorkflowAsyncRequest request) throws ExternalServiceException {
        String workflowName = "unknown";
        try {
            validateRequest(request);
            workflowName = request.getWorkflowName();
            
            log.info("Executing workflow asynchronously: {} with parameters: {}", 
                    workflowName, request.getParameters());
            
            CompletableFuture<AutomationExecutionResult> future = 
                    automationPlatformService.executeWorkflowAsync(workflowName, request.getParameters());
            
            // Wait for the initial response (job creation) with a timeout
            // The actual workflow execution continues asynchronously
            AutomationExecutionResult result = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            log.info("Async workflow '{}' initiated with job ID: {}", 
                    workflowName, result.getJobId());
            
            return result;
            
        } catch (TimeoutException e) {
            log.error("Timeout waiting for async workflow initiation: {}", workflowName, e);
            throw new ExternalServiceException(
                "Timeout waiting for workflow initiation",
                e,
                "WORKFLOW_INITIATION_TIMEOUT",
                "aap",
                "executeWorkflowAsync"
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for async workflow: {}", workflowName, e);
            throw new ExternalServiceException(
                "Workflow execution interrupted",
                e,
                "WORKFLOW_EXECUTION_INTERRUPTED",
                "aap",
                "executeWorkflowAsync"
            );
        } catch (ExecutionException e) {
            log.error("Failed to execute async workflow: {}", workflowName, e);
            throw new ExternalServiceException(
                "Failed to execute async workflow: " + e.getCause().getMessage(),
                e.getCause(),
                "ASYNC_WORKFLOW_EXECUTION_FAILED",
                "aap",
                "executeWorkflowAsync"
            );
        } catch (Exception e) {
            log.error("Unexpected error executing async workflow: {}", workflowName, e);
            throw new ExternalServiceException(
                "Failed to execute async workflow: " + e.getMessage(),
                e,
                "ASYNC_WORKFLOW_EXECUTION_FAILED",
                "aap",
                "executeWorkflowAsync"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "execute-workflow-async";
    }
    
    @Override
    public String getServiceName() {
        return "aap";
    }
    
    
    public void validateRequest(ExecuteWorkflowAsyncRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getWorkflowName() == null || request.getWorkflowName().trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow name cannot be null or empty");
        }
    }
}
