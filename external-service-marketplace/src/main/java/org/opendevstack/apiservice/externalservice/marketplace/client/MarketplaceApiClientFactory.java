package org.opendevstack.apiservice.externalservice.marketplace.client;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceServiceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceServiceConfig.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceClientException;
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

@Component
@Slf4j
public class MarketplaceApiClientFactory {

    private final MarketplaceServiceConfig configuration;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param configuration      Marketplace service configuration
     * @param restTemplateBuilder RestTemplate builder for creating HTTP clients
     */
    public MarketplaceApiClientFactory(MarketplaceServiceConfig configuration,
                                RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        this.restTemplateBuilder = restTemplateBuilder;

        log.info("MarketplaceApiClientFactory initialized with {} instance(s)",
                configuration.getInstances().size());
    }

    /**
     * Resolve the effective instance name.
     * <ul>
     *   <li>If the default instance is configured via {@code externalservices.marketplace.default-instance}, it is returned.</li>
     *   <li>Otherwise the first entry of the instances map is returned (insertion order).</li>
     *   <li>If no instances are configured at all, a {@link MarketplaceClientException} is thrown.</li>
     * </ul>
     *
     * @return The resolved instance name (never {@code null}/blank)
     * @throws MarketplaceClientException if no Marketplace instances are configured
     */
    public String getDefaultInstanceName() throws MarketplaceClientException {

        String defaultInstance = configuration.getDefaultInstance();
        if (defaultInstance != null && !defaultInstance.isBlank()) {
            return defaultInstance;
        }

        Map<String, ?> instances = configuration.getInstances();
        if (instances == null || instances.isEmpty()) {
            throw new MarketplaceClientException("No Marketplace instances configured");
        }

        return instances.keySet().iterator().next();
    }

    /**
     * Get a {@link MarketplaceApiClient} for a specific instance.
     * If {@code instanceName} is {@code null} or blank, the default instance is used.
     *
     * @param instanceName Name of the Marketplace instance, or {@code null}/{@code ""} for the default
     * @return Configured MarketplaceApiClient
     * @throws MarketplaceClientException if the instance is not configured
     */
    @Cacheable(value = "marketplaceApiClients", key = "#instanceName", condition = "#instanceName != null && !#instanceName.isBlank()")
    public MarketplaceApiClient getClient(String instanceName) throws MarketplaceClientException {
        if (instanceName == null || instanceName.isBlank()) {
            throw new MarketplaceClientException(
                    String.format("Provide instance name. Available instances: %s",
                            configuration.getInstances().keySet()));
        }

        MarketplaceInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);

        if (instanceConfig == null) {
            throw new MarketplaceClientException(
                    String.format("Marketplace instance '%s' is not configured. Available instances: %s",
                            instanceName, configuration.getInstances().keySet()));
        }

        log.info("Creating new MarketplaceApiClient for instance '{}'", instanceName);

        RestTemplate restTemplate = createRestTemplate(instanceConfig);
        return new MarketplaceApiClient(instanceName, instanceConfig, restTemplate);
    }

    /**
     * Get the default client, as determined by {@code externalservices.marketplace.default-instance}.
     * Falls back to the first configured instance when {@code default-instance} is not set.
     *
     * @return MarketplaceApiClient for the default instance
     * @throws MarketplaceClientException if no instances are configured
     */
    @Cacheable(value = "marketplaceApiClients", key = "'default'")
    public MarketplaceApiClient getClient() throws MarketplaceClientException {
        String defaultInstanceName = getDefaultInstanceName();
        MarketplaceInstanceConfig instanceConfig = configuration.getInstances().get(defaultInstanceName);
        RestTemplate restTemplate = createRestTemplate(instanceConfig);

        return new MarketplaceApiClient(defaultInstanceName, instanceConfig, restTemplate);
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
    @CacheEvict(value = "marketplaceApiClients", allEntries = true)
    public void clearCache() {
        log.info("Clearing MarketplaceApiClient cache");
    }

    /**
     * Create a configured RestTemplate for a Marketplace instance.
     *
     * @param config Configuration for the instance
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(MarketplaceInstanceConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(config.getConnectionTimeout());
        requestFactory.setReadTimeout(config.getReadTimeout());
        restTemplate.setRequestFactory(requestFactory);

        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for Marketplace API connection. "
                    + "This should only be used in development environments!");
            configureTrustAllCertificates();
        }

        return restTemplate;
    }

    /**
     * Configure RestTemplate to trust all SSL certificates.
     * WARNING: This should only be used in development environments.
     */
    @SuppressWarnings({"java:S4830", "java:S1186"})
    private void configureTrustAllCertificates() {
        try {
            TrustManager[] trustAllCerttificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        // Intentionally empty - trusting all certificates for development environments
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllCerttificates, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            // Intentionally disabling hostname verification for development environments
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            log.error("Failed to configure SSL trust all certificates for Marketplace API", ex);
        }
    }
}
