package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.springframework.stereotype.Component;

/**
 * Command for getting workflow job status from the Automation Platform.
 * This is different from regular job status as it queries the workflow_jobs endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetWorkflowJobStatusCommand implements ExternalServiceCommand<GetWorkflowJobStatusRequest, AutomationJobStatus> {
    
    private final AutomationPlatformService automationPlatformService;
    
    @Override
    public AutomationJobStatus execute(GetWorkflowJobStatusRequest request) throws ExternalServiceException {
        try {
            validateRequest(request);
            
            log.debug("Getting workflow job status for workflow ID: {}", request.getWorkflowId());
            
            return automationPlatformService.getWorkflowJobStatus(request.getWorkflowId());
            
        } catch (Exception e) {
            String workflowId = request != null ? request.getWorkflowId() : "unknown";
            log.error("Failed to get workflow job status for workflow ID: {}", workflowId, e);
            throw new ExternalServiceException(
                "Failed to get workflow job status: " + e.getMessage(),
                e,
                "WORKFLOW_JOB_STATUS_CHECK_FAILED",
                "aap",
                "getWorkflowJobStatus"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "get-workflow-job-status";
    }
    
    @Override
    public String getServiceName() {
        return "aap";
    }
    
    
    public void validateRequest(GetWorkflowJobStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getWorkflowId() == null || request.getWorkflowId().trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow ID cannot be null or empty");
        }
    }
}
