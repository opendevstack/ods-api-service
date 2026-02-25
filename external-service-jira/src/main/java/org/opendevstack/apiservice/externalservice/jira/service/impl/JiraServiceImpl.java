package org.opendevstack.apiservice.externalservice.jira.service.impl;

import org.opendevstack.apiservice.externalservice.jira.client.ApiClient;
import org.opendevstack.apiservice.externalservice.jira.client.JiraApiClient;
import org.opendevstack.apiservice.externalservice.jira.client.JiraApiClientFactory;
import org.opendevstack.apiservice.externalservice.jira.client.api.ProjectApi;
import org.opendevstack.apiservice.externalservice.jira.client.api.ServerInfoApi;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Set;

/**
 * Implementation of {@link JiraService}.
 * Uses {@link JiraApiClientFactory} to obtain clients for different Jira instances
 * and delegates operations to the appropriate generated API client.
 */
@Service
@Slf4j
public class JiraServiceImpl implements JiraService {

    private final JiraApiClientFactory clientFactory;

    /**
     * Constructor with dependency injection.
     *
     * @param clientFactory Factory for creating Jira API clients
     */
    public JiraServiceImpl(JiraApiClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        log.info("JiraServiceImpl initialized");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #projectExists(String, String)} using the default instance.
     */
    @Override
    public boolean projectExists(String projectIdOrKey) throws JiraException {
        return projectExists(getDefaultInstance(), projectIdOrKey);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses the generated {@link ProjectApi} to call
     * {@code GET /rest/api/2/project/{projectIdOrKey}}.
     * Returns {@code true} on a 200 response and {@code false} on 404.
     * All other errors are wrapped in a {@link JiraException}.
     * If {@code instanceName} is {@code null} or blank, the default instance is used.
     */
    @Override
    public boolean projectExists(String instanceName, String projectIdOrKey) throws JiraException {
        // getClient already resolves null/blank to the default instance
        log.debug("Checking if project '{}' exists in Jira instance '{}' (null/blank = default)", projectIdOrKey, instanceName);

        try {
            JiraApiClient jiraClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = jiraClient.getApiClient();

            ProjectApi projectApi = new ProjectApi(apiClient);
            projectApi.getProject(projectIdOrKey, null, null);

            log.debug("Project '{}' exists in instance '{}'", projectIdOrKey, jiraClient.getInstanceName());
            return true;

        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Project '{}' not found in Jira instance '{}'", projectIdOrKey, instanceName);
            return false;

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.warn("Access denied when checking project '{}' in instance '{}': {}",
                    projectIdOrKey, instanceName, e.getMessage());
            throw new JiraException(
                    String.format("Access denied when checking project '%s' in instance '%s'",
                            projectIdOrKey, instanceName), e);

        } catch (RestClientException e) {
            log.error("Error checking project '{}' in instance '{}'", projectIdOrKey, instanceName, e);
            throw new JiraException(
                    String.format("Failed to check project '%s' in Jira instance '%s'",
                            projectIdOrKey, instanceName), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultInstance() throws JiraException {
        return clientFactory.getDefaultInstanceName();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Calls {@code GET /rest/api/2/serverInfo} on the default configured Jira instance.
     * Returns {@code false} (without throwing) if no instances are configured or the call fails.
     */
    @Override
    public boolean isHealthy() {
        try {
            Set<String> instances = getAvailableInstances();
            if (instances.isEmpty()) {
                log.warn("No Jira instances configured – reporting unhealthy");
                return false;
            }

            JiraApiClient defaultClient = clientFactory.getClient();
            ApiClient apiClient = defaultClient.getApiClient();

            ServerInfoApi serverInfoApi = new ServerInfoApi(apiClient);
            serverInfoApi.getServerInfo();

            log.debug("Jira health check succeeded for instance '{}'", defaultClient.getInstanceName());
            return true;

        } catch (RestClientException e) {
            log.warn("Jira health check failed: {}", e.getMessage());
            return false;
        } catch (JiraException e) {
            log.warn("Jira health check failed – no default client available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Set<String> getAvailableInstances() {
        return clientFactory.getAvailableInstances();
    }

    @Override
    public boolean hasInstance(String instanceName) {
        return clientFactory.hasInstance(instanceName);
    }
}
