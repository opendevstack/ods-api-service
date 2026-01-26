package org.opendevstack.apiservice.externalservice.ocp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

/**
 * Adapter that wraps OpenshiftService to implement the ExternalService interface.
 * Provides default instance handling for health checks across all configured OpenShift clusters.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenshiftServiceAdapter implements ExternalService {

    private final OpenshiftService openshiftService;
    private final OpenshiftServiceConfiguration openshiftConfiguration;

    @Override
    public String getServiceName() {
        return "openshift";
    }

    @Override
    public boolean validateConnection() {
        // Validate all configured instances
        try {
            if (openshiftConfiguration.getInstances() == null || openshiftConfiguration.getInstances().isEmpty()) {
                log.warn("No OpenShift instances configured");
                return false;
            }

            // Check at least one instance is valid
            for (String instanceName : openshiftConfiguration.getInstances().keySet()) {
                try {
                    if (openshiftService.validateConnection(instanceName)) {
                        return true; // At least one instance is valid
                    }
                } catch (Exception e) {
                    log.debug("Instance {} validation failed: {}", instanceName, e.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error validating OpenShift connections", e);
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        // Check health across all configured instances
        try {
            if (openshiftConfiguration.getInstances() == null || openshiftConfiguration.getInstances().isEmpty()) {
                log.warn("No OpenShift instances configured");
                return false;
            }

            // Check at least one instance is healthy
            for (String instanceName : openshiftConfiguration.getInstances().keySet()) {
                try {
                    if (openshiftService.isHealthy(instanceName)) {
                        return true; // At least one instance is healthy
                    }
                } catch (Exception e) {
                    log.debug("Instance {} health check failed: {}", instanceName, e.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking OpenShift health", e);
            return false;
        }
    }

    /**
     * Get the underlying multi-instance service for direct access when needed.
     */
    public OpenshiftService getUnderlyingService() {
        return openshiftService;
    }
}
