package org.opendevstack.apiservice.externalservice.ocp.command.secret;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Command to retrieve an entire secret from OpenShift.
 * Supports retrieving from default or specified namespace.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetSecretCommand implements ExternalServiceCommand<GetSecretRequest, Map<String, String>> {
    
    private final OpenshiftService openshiftService;
    
    @Override
    public Map<String, String> execute(GetSecretRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            String namespace = request.getNamespace();
            if (namespace != null && !namespace.trim().isEmpty()) {
                log.info("Retrieving secret '{}' from namespace '{}' in instance '{}'",
                        request.getSecretName(), namespace, request.getInstanceName());
                return openshiftService.getSecret(request.getInstanceName(), request.getSecretName(), namespace);
            } else {
                log.info("Retrieving secret '{}' from default namespace in instance '{}'",
                        request.getSecretName(), request.getInstanceName());
                return openshiftService.getSecret(request.getInstanceName(), request.getSecretName());
            }
        } catch (Exception e) {
            log.error("Failed to retrieve secret '{}' from instance '{}': {}",
                    request.getSecretName(), request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to retrieve secret: " + e.getMessage(),
                    e,
                    "GET_SECRET_FAILED",
                    "openshift",
                    "getSecret"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "get-secret";
    }
    
    @Override
    public String getServiceName() {
        return "openshift";
    }
    
    @Override
    public void validateRequest(GetSecretRequest request) {
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
