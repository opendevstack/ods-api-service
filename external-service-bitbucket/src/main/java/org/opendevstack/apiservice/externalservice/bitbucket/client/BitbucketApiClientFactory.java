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
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
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
    @Cacheable(value = "bitbucketApiClients", key = "#instanceName")
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

        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for Bitbucket connection. " +
                    "This should only be used in development environments!");
            restTemplate.setRequestFactory(createTrustAllRequestFactory(config));
        } else {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(config.getConnectionTimeout());
            requestFactory.setReadTimeout(config.getReadTimeout());
            restTemplate.setRequestFactory(requestFactory);
        }

        return restTemplate;
    }

    /**
     * Create a {@link SimpleClientHttpRequestFactory} that trusts all SSL certificates
     * <b>only for this specific RestTemplate</b>, without modifying the JVM-wide defaults.
     * <p>
     * WARNING: This should only be used in development environments.
     *
     * @param config Instance configuration (for timeouts)
     * @return A request factory whose connections skip SSL verification
     */
    @SuppressWarnings({"java:S4830", "java:S1186"}) // Intentionally disabling SSL validation for development
    private SimpleClientHttpRequestFactory createTrustAllRequestFactory(BitbucketInstanceConfig config) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // No validation performed - development only
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // No validation performed - development only
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            final javax.net.ssl.HostnameVerifier trustAllHostnames = (hostname, session) -> true;

            // Override prepareConnection so SSL settings apply only to this RestTemplate
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws java.io.IOException {
                    if (connection instanceof HttpsURLConnection httpsConnection) {
                        httpsConnection.setSSLSocketFactory(sslSocketFactory);
                        httpsConnection.setHostnameVerifier(trustAllHostnames);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };
            requestFactory.setConnectTimeout(config.getConnectionTimeout());
            requestFactory.setReadTimeout(config.getReadTimeout());
            return requestFactory;

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to configure SSL trust all certificates, falling back to default factory", e);
            SimpleClientHttpRequestFactory fallback = new SimpleClientHttpRequestFactory();
            fallback.setConnectTimeout(config.getConnectionTimeout());
            fallback.setReadTimeout(config.getReadTimeout());
            return fallback;
        }
    }
}
