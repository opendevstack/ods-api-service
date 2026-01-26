package org.opendevstack.apiservice.externalservice.webhookproxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.WebhookProxyService;
import org.springframework.stereotype.Component;

/**
 * Adapter that wraps WebhookProxyService to implement the ExternalService interface.
 * Provides default cluster handling for health checks across all configured clusters.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookProxyServiceAdapter implements ExternalService {

    private final WebhookProxyService webhookProxyService;
    private final WebhookProxyConfiguration webhookProxyConfiguration;

    @Override
    public String getServiceName() {
        return "webhookproxy";
    }

    @Override
    public boolean validateConnection() {
        // Validate all configured clusters
        try {
            if (webhookProxyConfiguration.getClusters() == null || webhookProxyConfiguration.getClusters().isEmpty()) {
                log.warn("No WebhookProxy clusters configured");
                return false;
            }

            // Check at least one cluster is valid
            for (String clusterName : webhookProxyConfiguration.getClusters().keySet()) {
                try {
                    // Use a test project key for validation
                    if (webhookProxyService.validateConnection(clusterName, "test")) {
                        return true; // At least one cluster is valid
                    }
                } catch (Exception e) {
                    log.debug("Cluster {} validation failed: {}", clusterName, e.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error validating WebhookProxy connections", e);
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        // Check health across all configured clusters
        try {
            if (webhookProxyConfiguration.getClusters() == null || webhookProxyConfiguration.getClusters().isEmpty()) {
                log.warn("No WebhookProxy clusters configured");
                return false;
            }

            // Check at least one cluster is healthy
            for (String clusterName : webhookProxyConfiguration.getClusters().keySet()) {
                try {
                    // Use a test project key for health check
                    if (webhookProxyService.isHealthy(clusterName, "test")) {
                        return true; // At least one cluster is healthy
                    }
                } catch (Exception e) {
                    log.debug("Cluster {} health check failed: {}", clusterName, e.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking WebhookProxy health", e);
            return false;
        }
    }

    /**
     * Get the underlying multi-cluster service for direct access when needed.
     */
    public WebhookProxyService getUnderlyingService() {
        return webhookProxyService;
    }
}
