package org.opendevstack.apiservice.externalservice.bitbucket.client;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.http.ExternalServiceSslProperties;
import org.opendevstack.apiservice.externalservice.api.http.RestClientFactory;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration.BitbucketInstanceConfig;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

/**
 * Factory for creating {@link BitbucketApiClient} instances.
 *
 * <p>SSL wiring is delegated to {@link RestClientFactory} in {@code external-service-api}.
 * A {@link RestTemplate} is produced (not {@code RestClient}) because the OpenAPI-generated
 * {@link ApiClient} only accepts {@code RestTemplate}.
 *
 * <p>Clients are cached by instance name via Spring's {@code @Cacheable}.
 */
@Component
@Slf4j
public class BitbucketApiClientFactory {

    private final BitbucketServiceConfiguration configuration;

    public BitbucketApiClientFactory(BitbucketServiceConfiguration configuration) {
        this.configuration = configuration;
        log.info("BitbucketApiClientFactory initialized with {} instance(s)",
                configuration.getInstances().size());
    }

    /**
     * Resolve the effective default instance name.
     *
     * @return The resolved instance name (never {@code null}/blank)
     * @throws BitbucketException if no Bitbucket instances are configured
     */
    public String getDefaultInstanceName() throws BitbucketException {
        String defaultInstance = configuration.getDefaultInstance();
        if (defaultInstance != null && !defaultInstance.isBlank()) {
            return defaultInstance;
        }

        Map<String, ?> instances = configuration.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new BitbucketException("No Bitbucket instances configured");
        }

        return instances.keySet().iterator().next();
    }

    /**
     * Get a {@link BitbucketApiClient} for a named instance.
     *
     * @param instanceName Name of the Bitbucket instance (must not be null/blank)
     * @return Configured {@link BitbucketApiClient}
     * @throws BitbucketException if the instance is not configured
     */
    @Cacheable(value = "bitbucketApiClients", key = "#instanceName",
            condition = "#instanceName != null && !#instanceName.isBlank()")
    public BitbucketApiClient getClient(String instanceName) throws BitbucketException {
        if (instanceName == null || instanceName.isBlank()) {
            throw new BitbucketException(
                    String.format("Provide instance name. Available instances: %s",
                            configuration.getInstances().keySet()));
        }

        BitbucketInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);
        if (instanceConfig == null) {
            throw new BitbucketException(
                    String.format("Bitbucket instance '%s' is not configured. Available instances: %s",
                            instanceName, configuration.getInstances().keySet()));
        }

        log.info("Creating new BitbucketApiClient for instance '{}'", instanceName);
        return new BitbucketApiClient(instanceName, instanceConfig, buildRestTemplate(instanceConfig));
    }

    /**
     * Get the default client.
     *
     * @return {@link BitbucketApiClient} for the default instance
     * @throws BitbucketException if no instances are configured
     */
    @Cacheable(value = "bitbucketApiClients", key = "'default'")
    public BitbucketApiClient getClient() throws BitbucketException {
        String defaultInstanceName = getDefaultInstanceName();
        BitbucketInstanceConfig instanceConfig = configuration.getInstances().get(defaultInstanceName);
        return new BitbucketApiClient(defaultInstanceName, instanceConfig, buildRestTemplate(instanceConfig));
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
    @CacheEvict(value = "bitbucketApiClients", allEntries = true)
    public void clearCache() {
        log.info("Clearing BitbucketApiClient cache");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private RestTemplate buildRestTemplate(BitbucketInstanceConfig config) {
        log.info("Creating RestTemplate for Bitbucket instance (connect={}ms, read={}ms)",
                config.getConnectionTimeout(), config.getReadTimeout());
        return RestClientFactory.buildRestTemplate(
                toSslProperties(config),
                config.getConnectionTimeout(),
                config.getReadTimeout());
    }

    /**
     * Adapt {@link BitbucketInstanceConfig} SSL fields to {@link ExternalServiceSslProperties}
     * so we can pass them to {@link RestClientFactory} without changing the generated config class.
     */
    private static ExternalServiceSslProperties toSslProperties(BitbucketInstanceConfig config) {
        ExternalServiceSslProperties ssl = new ExternalServiceSslProperties();
        ssl.setTrustStorePath(config.getTrustStorePath());
        ssl.setTrustStorePassword(config.getTrustStorePassword());
        ssl.setTrustStoreType(config.getTrustStoreType());
        return ssl;
    }
}
