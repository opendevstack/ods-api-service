package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

/**
 * Command to validate connection to an OpenShift instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateConnectionCommand implements ExternalServiceCommand<ValidateConnectionRequest, Boolean> {
    
    private final OpenshiftService openshiftService;
    
    @Override
    public Boolean execute(ValidateConnectionRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            log.info("Validating connection to OpenShift instance '{}'", request.getInstanceName());
            boolean valid = openshiftService.validateConnection(request.getInstanceName());
            log.info("Connection to OpenShift instance '{}' is valid: {}", request.getInstanceName(), valid);
            return valid;
        } catch (Exception e) {
            log.error("Failed to validate connection to instance '{}': {}", request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to validate connection: " + e.getMessage(),
                    e,
                    "VALIDATE_CONNECTION_FAILED",
                    "openshift",
                    "validateConnection"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "validate-connection";
    }
    
    @Override
    public String getServiceName() {
        return "openshift";
    }
    
    @Override
    public void validateRequest(ValidateConnectionRequest request) {
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
