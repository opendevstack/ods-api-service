package org.opendevstack.apiservice.externalservice.api;

/**
 * Base interface for all external services.
 * All external service integrations should implement this interface.
 */
public interface ExternalService {

    /**
     * Checks if the external service is healthy and reachable.
     * This method is used by health indicators and should not throw exceptions.
     *
     * @return true if the service is healthy, false otherwise
     */
    boolean isHealthy();
}
