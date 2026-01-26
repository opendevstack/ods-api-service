package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

/**
 * Command to check if an OpenShift instance is configured.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HasInstanceCommand implements ExternalServiceCommand<HasInstanceRequest, Boolean> {
    
    private final OpenshiftService openshiftService;
    
    @Override
    public Boolean execute(HasInstanceRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            log.info("Checking if OpenShift instance '{}' is configured", request.getInstanceName());
            boolean exists = openshiftService.hasInstance(request.getInstanceName());
            log.info("OpenShift instance '{}' exists: {}", request.getInstanceName(), exists);
            return exists;
        } catch (Exception e) {
            log.error("Failed to check if instance '{}' exists: {}", request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to check instance existence: " + e.getMessage(),
                    e,
                    "HAS_INSTANCE_FAILED",
                    "openshift",
                    "hasInstance"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "has-instance";
    }
    
    @Override
    public String getServiceName() {
        return "openshift";
    }
    
    @Override
    public void validateRequest(HasInstanceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getInstanceName() == null || request.getInstanceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Instance name cannot be null or empty");
        }
    }
}
