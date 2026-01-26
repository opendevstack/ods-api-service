package org.opendevstack.apiservice.externalservice.ocp.command.secret;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

/**
 * Command to retrieve a specific value from an OpenShift secret.
 * Supports retrieving from default or specified namespace.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetSecretValueCommand implements ExternalServiceCommand<GetSecretValueRequest, String> {
    
    private final OpenshiftService openshiftService;
    
    @Override
    public String execute(GetSecretValueRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            String namespace = request.getNamespace();
            if (namespace != null && !namespace.trim().isEmpty()) {
                log.info("Retrieving secret value for key '{}' from secret '{}' in namespace '{}' in instance '{}'",
                        request.getKey(), request.getSecretName(), namespace, request.getInstanceName());
                return openshiftService.getSecretValue(
                        request.getInstanceName(),
                        request.getSecretName(),
                        request.getKey(),
                        namespace
                );
            } else {
                log.info("Retrieving secret value for key '{}' from secret '{}' in default namespace in instance '{}'",
                        request.getKey(), request.getSecretName(), request.getInstanceName());
                return openshiftService.getSecretValue(
                        request.getInstanceName(),
                        request.getSecretName(),
                        request.getKey()
                );
            }
        } catch (Exception e) {
            log.error("Failed to retrieve secret value for key '{}' from secret '{}' in instance '{}': {}",
                    request.getKey(), request.getSecretName(), request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to retrieve secret value: " + e.getMessage(),
                    e,
                    "GET_SECRET_VALUE_FAILED",
                    "openshift",
                    "getSecretValue"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "get-secret-value";
    }
    
    @Override
    public String getServiceName() {
        return "openshift";
    }
    
    @Override
    public void validateRequest(GetSecretValueRequest request) {
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
        if (request.getKey() == null || request.getKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }
}
