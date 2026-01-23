package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.springframework.stereotype.Component;

/**
 * Command for executing a workflow on the Automation Platform.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecuteWorkflowCommand implements ExternalServiceCommand<ExecuteWorkflowRequest, AutomationExecutionResult> {
    
    private final AutomationPlatformService automationPlatformService;
    
    @Override
    public AutomationExecutionResult execute(ExecuteWorkflowRequest request) throws ExternalServiceException {
        try {
            validateRequest(request);
            
            log.info("Executing workflow: {} with parameters: {}", request.getWorkflowName(), request.getParameters());
            
            if (request.isAsync()) {
                // For async, we would need to return immediately with a job ID
                // For now, execute synchronously
                log.warn("Async execution requested but not fully implemented, executing synchronously");
            }
            
            return automationPlatformService.executeWorkflow(request.getWorkflowName(), request.getParameters());
            
        } catch (Exception e) {
            log.error("Failed to execute workflow: {}", request.getWorkflowName(), e);
            throw new ExternalServiceException(
                "Failed to execute workflow: " + e.getMessage(),
                e,
                "WORKFLOW_EXECUTION_FAILED",
                "aap",
                "executeWorkflow"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "execute-workflow";
    }
    
    @Override
    public String getServiceName() {
        return "aap";
    }
    
    
    public void validateRequest(ExecuteWorkflowRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getWorkflowName() == null || request.getWorkflowName().trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow name cannot be null or empty");
        }
    }
}
