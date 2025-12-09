package org.opendevstack.apiservice.externalservice.commons;

/**
 * Base interface for all external services.
 * Provides common operations that all external services should implement.
 */
public interface ExternalService {
    
    /**
     * Get the unique name of this service.
     * @return service name (e.g., "bitbucket", "aap", "uipath")
     */
    String getServiceName();
    
    /**
     * Validate the connection to the external service.
     * @return true if connection is valid, false otherwise
     */
    boolean validateConnection();
    
    /**
     * Check if the service is healthy and ready to handle requests.
     * @return true if service is healthy, false otherwise
     */
    boolean isHealthy();
}
