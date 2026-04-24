package org.opendevstack.apiservice.externalservice.bitbucket.client;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration.BitbucketInstanceConfig;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating {@link BitbucketApiClient} instances.
 * Uses the factory pattern to provide configured clients for different Bitbucket instances.
 * Clients are cached and reused for efficiency.
 */
@Component
@Slf4j
public class BitbucketApiClientFactory {

    private final BitbucketServiceConfiguration configuration;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param configuration      Bitbucket service configuration
     * @param restTemplateBuilder RestTemplate builder for creating HTTP clients
     */
    public BitbucketApiClientFactory(BitbucketServiceConfiguration configuration,
                                     RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        this.restTemplateBuilder = restTemplateBuilder;

        log.info("BitbucketApiClientFactory initialized with {} instance(s)",
                 configuration.getInstances().size());
    }

    /**
     * Resolve the effective instance name.
     * <ul>
     *   <li>If the default instance is configured via {@code externalservices.bitbucket.default-instance}, it is returned.</li>
     *   <li>Otherwise the first entry of the instances map is returned (insertion order).</li>
     *   <li>If no instances are configured at all, a {@link BitbucketException} is thrown.</li>
     * </ul>
     *
     * @return The resolved default instance name (never {@code null}/blank)
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
     * Get a {@link BitbucketApiClient} for a specific instance.
     * If {@code instanceName} is {@code null} or blank, a {@link BitbucketException} is thrown.
     *
     * @param instanceName Name of the Bitbucket instance
     * @return Configured BitbucketApiClient
     * @throws BitbucketException if the instance name is null/blank or not configured
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

        RestTemplate restTemplate = createRestTemplate(instanceConfig);
        return new BitbucketApiClient(instanceName, instanceConfig, restTemplate);
    }

    /**
     * Get the default client, as determined by {@code externalservices.bitbucket.default-instance}.
     * Falls back to the first configured instance when {@code default-instance} is not set.
     *
     * @return BitbucketApiClient for the default instance
     * @throws BitbucketException if no instances are configured
     */
    @Cacheable(value = "bitbucketApiClients", key = "'default'")
    public BitbucketApiClient getClient() throws BitbucketException {
        String defaultInstanceName = getDefaultInstanceName();
        BitbucketInstanceConfig instanceConfig = configuration.getInstances().get(defaultInstanceName);
        RestTemplate restTemplate = createRestTemplate(instanceConfig);

        return new BitbucketApiClient(defaultInstanceName, instanceConfig, restTemplate);
    }

    /**
     * Get all available instance names.
     *
     * @return Set of configured instance names
     */
    public Set<String> getAvailableInstances() {
        return configuration.getInstances().keySet();
    }

    /**
     * Check if an instance is configured.
     *
     * @param instanceName Name of the instance to check
     * @return true if configured, false otherwise
     */
    public boolean hasInstance(String instanceName) {
        return configuration.getInstances().containsKey(instanceName);
    }

    /**
     * Clear the client cache (useful for testing or when configuration changes).
     */
    @CacheEvict(value = "bitbucketApiClients", allEntries = true)
    public void clearCache() {
        log.info("Clearing BitbucketApiClient cache");
    }
    
    /**
     * Create a configured RestTemplate for a Bitbucket instance.
     *
     * @param config Configuration for the instance
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(BitbucketInstanceConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        if (!StringUtils.hasText(config.getTrustStorePath())) {
            log.info("No custom trust store configured for Bitbucket instance, using JVM default trust store");
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(config.getConnectionTimeout());
            requestFactory.setReadTimeout(config.getReadTimeout());
            restTemplate.setRequestFactory(requestFactory);
            return restTemplate;
        }

        try {
            log.info("Loading custom trust store for Bitbucket instance from: {}", config.getTrustStorePath());
            KeyStore trustStore = KeyStore.getInstance(config.getTrustStoreType());
            try (FileInputStream fis = new FileInputStream(config.getTrustStorePath())) {
                char[] password = StringUtils.hasText(config.getTrustStorePassword())
                        ? config.getTrustStorePassword().toCharArray()
                        : new char[0];
                trustStore.load(fis, password);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    if (connection instanceof HttpsURLConnection httpsConnection) {
                        httpsConnection.setSSLSocketFactory(sslSocketFactory);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };
            requestFactory.setConnectTimeout(config.getConnectionTimeout());
            requestFactory.setReadTimeout(config.getReadTimeout());
            restTemplate.setRequestFactory(requestFactory);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to load custom trust store '{}' for Bitbucket instance, falling back to JVM default: {}",
                    config.getTrustStorePath(), e.getMessage());
            SimpleClientHttpRequestFactory fallback = new SimpleClientHttpRequestFactory();
            fallback.setConnectTimeout(config.getConnectionTimeout());
            fallback.setReadTimeout(config.getReadTimeout());
            restTemplate.setRequestFactory(fallback);
        }

        return restTemplate;
    }
}
