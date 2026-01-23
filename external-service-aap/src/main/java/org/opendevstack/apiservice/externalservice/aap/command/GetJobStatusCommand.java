package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.springframework.stereotype.Component;

/**
 * Command for getting job status from the Automation Platform.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetJobStatusCommand implements ExternalServiceCommand<GetJobStatusRequest, AutomationJobStatus> {
    
    private final AutomationPlatformService automationPlatformService;
    
    @Override
    public AutomationJobStatus execute(GetJobStatusRequest request) throws ExternalServiceException {
        try {
            validateRequest(request);
            
            log.debug("Getting job status for job ID: {}", request.getJobId());
            
            return automationPlatformService.getJobStatus(request.getJobId());
            
        } catch (Exception e) {
            log.error("Failed to get job status for job ID: {}", request.getJobId(), e);
            throw new ExternalServiceException(
                "Failed to get job status: " + e.getMessage(),
                e,
                "JOB_STATUS_CHECK_FAILED",
                "aap",
                "getJobStatus"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "get-job-status";
    }
    
    @Override
    public String getServiceName() {
        return "aap";
    }
    
    
    public void validateRequest(GetJobStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getJobId() == null || request.getJobId().trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty");
        }
    }
}
