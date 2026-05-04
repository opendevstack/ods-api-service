package org.opendevstack.apiservice.externalservice.jira.client;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.http.ExternalServiceSslProperties;
import org.opendevstack.apiservice.externalservice.api.http.RestClientFactory;
import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration;
import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration.JiraInstanceConfig;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

/**
 * Factory for creating {@link JiraApiClient} instances.
 *
 * <p>SSL wiring is delegated to {@link RestClientFactory} in {@code external-service-api}.
 * A {@link RestTemplate} is produced (not {@code RestClient}) because the OpenAPI-generated
 * {@link ApiClient} only accepts {@code RestTemplate}.
 *
 * <p>Clients are cached by instance name via Spring's {@code @Cacheable}.
 */
@Component
@Slf4j
public class JiraApiClientFactory {

    private final JiraServiceConfiguration configuration;

    public JiraApiClientFactory(JiraServiceConfiguration configuration) {
        this.configuration = configuration;
        log.info("JiraApiClientFactory initialized with {} instance(s)",
                configuration.getInstances().size());
    }

    /**
     * Resolve the effective default instance name.
     *
     * @return The resolved instance name (never {@code null}/blank)
     * @throws JiraException if no Jira instances are configured
     */
    public String getDefaultInstanceName() throws JiraException {
        String defaultInstance = configuration.getDefaultInstance();
        if (defaultInstance != null && !defaultInstance.isBlank()) {
            return defaultInstance;
        }

        Map<String, ?> instances = configuration.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new JiraException("No Jira instances configured");
        }

        return instances.keySet().iterator().next();
    }

    /**
     * Get a {@link JiraApiClient} for a named instance.
     *
     * @param instanceName Name of the Jira instance (must not be null/blank)
     * @return Configured {@link JiraApiClient}
     * @throws JiraException if the instance is not configured
     */
    @Cacheable(value = "jiraApiClients", key = "#instanceName", condition = "#instanceName != null && !#instanceName.isBlank()")
    public JiraApiClient getClient(String instanceName) throws JiraException {
        if (instanceName == null || instanceName.isBlank()) {
            throw new JiraException(
                    String.format("Provide instance name. Available instances: %s",
                            configuration.getInstances().keySet()));
        }

        JiraInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);
        if (instanceConfig == null) {
            throw new JiraException(
                    String.format("Jira instance '%s' is not configured. Available instances: %s",
                            instanceName, configuration.getInstances().keySet()));
        }

        log.info("Creating new JiraApiClient for instance '{}'", instanceName);
        return new JiraApiClient(instanceName, instanceConfig, buildRestTemplate(instanceConfig));
    }

    /**
     * Get the default client.
     *
     * @return {@link JiraApiClient} for the default instance
     * @throws JiraException if no instances are configured
     */
    @Cacheable(value = "jiraApiClients", key = "'default'")
    public JiraApiClient getClient() throws JiraException {
        String defaultInstanceName = getDefaultInstanceName();
        JiraInstanceConfig instanceConfig = configuration.getInstances().get(defaultInstanceName);
        return new JiraApiClient(defaultInstanceName, instanceConfig, buildRestTemplate(instanceConfig));
    }

    /** @return all configured instance names */
    public Set<String> getAvailableInstances() {
        return configuration.getInstances().keySet();
    }

    /** @return {@code true} if the named instance is configured */
    public boolean hasInstance(String instanceName) {
        return configuration.getInstances().containsKey(instanceName);
    }

    /** Clear the client cache (useful for testing or when configuration changes). */
    @CacheEvict(value = "jiraApiClients", allEntries = true)
    public void clearCache() {
        log.info("Clearing JiraApiClient cache");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private RestTemplate buildRestTemplate(JiraInstanceConfig config) {
        log.info("Creating RestTemplate for Jira instance (connect={}ms, read={}ms)",
                config.getConnectionTimeout(), config.getReadTimeout());
        return RestClientFactory.buildRestTemplate(
                toSslProperties(config),
                config.getConnectionTimeout(),
                config.getReadTimeout());
    }

    /**
     * Adapt {@link JiraInstanceConfig} SSL fields to {@link ExternalServiceSslProperties}
     * so we can pass them to {@link RestClientFactory} without changing the generated
     * config class.
     */
    private static ExternalServiceSslProperties toSslProperties(JiraInstanceConfig config) {
        ExternalServiceSslProperties ssl = new ExternalServiceSslProperties();
        ssl.setTrustStorePath(config.getTrustStorePath());
        ssl.setTrustStorePassword(config.getTrustStorePassword());
        ssl.setTrustStoreType(config.getTrustStoreType());
        return ssl;
    }
}
