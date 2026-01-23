package org.opendevstack.apiservice.externalservice.bitbucket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.springframework.stereotype.Component;

/**
 * Adapter that wraps BitbucketService to implement the ExternalService interface.
 * Provides default instance handling for health checks across all configured Bitbucket instances.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BitbucketServiceAdapter implements ExternalService {

    private final BitbucketService bitbucketService;
    private final BitbucketServiceConfiguration bitbucketConfiguration;

    @Override
    public String getServiceName() {
        return "bitbucket";
    }

    @Override
    public boolean validateConnection() {
        // Validate all configured instances
        try {
            if (bitbucketConfiguration.getInstances() == null || bitbucketConfiguration.getInstances().isEmpty()) {
                log.warn("No Bitbucket instances configured");
                return false;
            }

            // Check at least one instance is valid
            for (String instanceName : bitbucketConfiguration.getInstances().keySet()) {
                try {
                    if (bitbucketService.validateConnection(instanceName)) {
                        return true; // At least one instance is valid
                    }
                } catch (Exception e) {
                    log.debug("Instance {} validation failed: {}", instanceName, e.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error validating Bitbucket connections", e);
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        // Check health across all configured instances
        try {
            if (bitbucketConfiguration.getInstances() == null || bitbucketConfiguration.getInstances().isEmpty()) {
                log.warn("No Bitbucket instances configured");
                return false;
            }

            // Check at least one instance is healthy
            for (String instanceName : bitbucketConfiguration.getInstances().keySet()) {
                try {
                    if (bitbucketService.isHealthy(instanceName)) {
                        return true; // At least one instance is healthy
                    }
                } catch (Exception e) {
                    log.debug("Instance {} health check failed: {}", instanceName, e.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking Bitbucket health", e);
            return false;
        }
    }

    /**
     * Get the underlying multi-instance service for direct access when needed.
     */
    public BitbucketService getUnderlyingService() {
        return bitbucketService;
    }
}
