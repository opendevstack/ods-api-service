package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.springframework.stereotype.Component;

/**
 * Command for checking if the Automation Platform is healthy.
 * This command is designed for health indicators and does not throw exceptions on unhealthy status.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsHealthyCommand implements ExternalServiceCommand<IsHealthyRequest, Boolean> {
    
    private final AutomationPlatformService automationPlatformService;
    
    @Override
    public Boolean execute(IsHealthyRequest request) throws ExternalServiceException {
        try {
            log.debug("Checking health of Automation Platform");
            
            boolean isHealthy = automationPlatformService.isHealthy();
            
            if (request != null && request.isIncludeDetails()) {
                log.info("Automation Platform health check: {}", isHealthy ? "healthy" : "unhealthy");
            } else {
                log.debug("Automation Platform health check: {}", isHealthy ? "healthy" : "unhealthy");
            }
            
            return isHealthy;
            
        } catch (Exception e) {
            log.warn("Health check failed with exception: {}", e.getMessage());
            // Health checks should not throw exceptions, return false instead
            return false;
        }
    }
    
    @Override
    public String getCommandName() {
        return "is-healthy";
    }
    
    @Override
    public String getServiceName() {
        return "aap";
    }
}
