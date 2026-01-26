package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

/**
 * Command to check if an OpenShift instance is healthy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsHealthyCommand implements ExternalServiceCommand<IsHealthyRequest, Boolean> {
    
    private final OpenshiftService openshiftService;
    
    @Override
    public Boolean execute(IsHealthyRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            log.info("Checking health of OpenShift instance '{}'", request.getInstanceName());
            boolean healthy = openshiftService.isHealthy(request.getInstanceName());
            log.info("OpenShift instance '{}' is healthy: {}", request.getInstanceName(), healthy);
            return healthy;
        } catch (Exception e) {
            log.error("Failed to check health of instance '{}': {}", request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to check instance health: " + e.getMessage(),
                    e,
                    "IS_HEALTHY_FAILED",
                    "openshift",
                    "isHealthy"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "is-healthy";
    }
    
    @Override
    public String getServiceName() {
        return "openshift";
    }
    
    @Override
    public void validateRequest(IsHealthyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getInstanceName() == null || request.getInstanceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Instance name cannot be null or empty");
        }
        if (!openshiftService.hasInstance(request.getInstanceName())) {
            throw new IllegalArgumentException("OpenShift instance '" + request.getInstanceName() + "' does not exist");
        }
    }
}
