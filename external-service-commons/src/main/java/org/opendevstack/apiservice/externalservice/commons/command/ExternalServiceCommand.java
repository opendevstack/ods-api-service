package org.opendevstack.apiservice.externalservice.commons.command;

import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;

/**
 * Command pattern interface for external service operations.
 * 
 * @param <Q> the request type for this command
 * @param <R> the response type for this command
 */
public interface ExternalServiceCommand<Q, R> {
    
    /**
     * Execute the command with the given request.
     * 
     * @param request the request data
     * @return the response data
     * @throws ExternalServiceException if the command execution fails
     */
    R execute(Q request) throws ExternalServiceException;
    
    /**
     * Get the name of this command.
     * @return command name (e.g., "getBranch", "executeWorkflow")
     */
    String getCommandName();
    
    /**
     * Get the service this command belongs to.
     * @return service name (e.g., "bitbucket", "aap")
     */
    String getServiceName();
    
    /**
     * Validate the request before execution.
     * 
     * @param request the request to validate
     * @throws IllegalArgumentException if request is invalid
     */
    default void validateRequest(Q request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
    }
}
