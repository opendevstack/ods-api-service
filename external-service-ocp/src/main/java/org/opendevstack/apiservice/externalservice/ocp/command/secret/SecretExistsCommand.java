package org.opendevstack.apiservice.externalservice.ocp.command.secret;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

/**
 * Command to check if a secret exists in OpenShift.
 * Supports checking in default or specified namespace.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecretExistsCommand implements ExternalServiceCommand<SecretExistsRequest, Boolean> {
    
    private final OpenshiftService openshiftService;
    
    @Override
    public Boolean execute(SecretExistsRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            String namespace = request.getNamespace();
            if (namespace != null && !namespace.trim().isEmpty()) {
                log.info("Checking if secret '{}' exists in namespace '{}' in instance '{}'",
                        request.getSecretName(), namespace, request.getInstanceName());
                return openshiftService.secretExists(
                        request.getInstanceName(),
                        request.getSecretName(),
                        namespace
                );
            } else {
                log.info("Checking if secret '{}' exists in default namespace in instance '{}'",
                        request.getSecretName(), request.getInstanceName());
                return openshiftService.secretExists(request.getInstanceName(), request.getSecretName());
            }
        } catch (Exception e) {
            log.error("Failed to check if secret '{}' exists in instance '{}': {}",
                    request.getSecretName(), request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to check secret existence: " + e.getMessage(),
                    e,
                    "SECRET_EXISTS_CHECK_FAILED",
                    "openshift",
                    "secretExists"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "secret-exists";
    }
    
    @Override
    public String getServiceName() {
        return "openshift";
    }
    
    @Override
    public void validateRequest(SecretExistsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getInstanceName() == null || request.getInstanceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Instance name cannot be null or empty");
        }
        if (!openshiftService.hasInstance(request.getInstanceName())) {
            throw new IllegalArgumentException("OpenShift instance '" + request.getInstanceName() + "' does not exist");
        }
        if (request.getSecretName() == null || request.getSecretName().trim().isEmpty()) {
            throw new IllegalArgumentException("Secret name cannot be null or empty");
        }
    }
}
