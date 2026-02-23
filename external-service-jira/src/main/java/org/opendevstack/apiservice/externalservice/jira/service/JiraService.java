package org.opendevstack.apiservice.externalservice.jira.service;

import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;

import java.util.Set;

/**
 * Service interface for interacting with Jira Data Center / Server.
 * Provides high-level methods for Jira operations across multiple instances.
 */
public interface JiraService extends ExternalService {

    /**
     * Check if a project exists using the default Jira instance.
     *
     * @param projectIdOrKey The project ID or project key (case-insensitive)
     * @return {@code true} if the project exists and the caller has permission to view it,
     *         {@code false} if the project was not found (404)
     * @throws JiraException if the check fails due to a connectivity or unexpected error
     */
    boolean projectExists(String projectIdOrKey) throws JiraException;

    /**
     * Check if a project exists in a specific Jira instance.
     *
     * @param instanceName    Name of the Jira instance, or {@code null}/{@code ""} for the default
     * @param projectIdOrKey  The project ID or project key (case-insensitive)
     * @return {@code true} if the project exists and the caller has permission to view it,
     *         {@code false} if the project was not found (404)
     * @throws JiraException if the check fails due to a connectivity or unexpected error
     */
    boolean projectExists(String instanceName, String projectIdOrKey) throws JiraException;

    /**
     * Get all available Jira instance names.
     *
     * @return Set of configured instance names
     */
    Set<String> getAvailableInstances();

    /**
     * Get the effective default instance name.
     *
     * @return Name of the default instance
     * @throws JiraException if no instances are configured
     */
    String getDefaultInstance() throws JiraException;

    /**
     * Check if a Jira instance is configured.
     *
     * @param instanceName Name of the instance to check
     * @return {@code true} if configured, {@code false} otherwise
     */
    boolean hasInstance(String instanceName);

    /**
     * Checks if the Jira service is healthy and reachable.
     * This method performs a live call to the Jira {@code /rest/api/2/serverInfo} endpoint
     * using the default configured instance.
     * This method should not throw exceptions.
     *
     * @return {@code true} if the service is reachable, {@code false} otherwise
     */
    @Override
    boolean isHealthy();
}
