package org.opendevstack.apiservice.externalservice.jira.client;

import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration;
import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration.JiraInstanceConfig;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating {@link JiraApiClient} instances.
 * Uses the factory pattern to provide configured clients for different Jira instances.
 * Clients are cached and reused for efficiency.
 */
@Component
@Slf4j
public class JiraApiClientFactory {

    private final JiraServiceConfiguration configuration;
    private final Map<String, JiraApiClient> clientCache;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param configuration      Jira service configuration
     * @param restTemplateBuilder RestTemplate builder for creating HTTP clients
     */
    public JiraApiClientFactory(JiraServiceConfiguration configuration,
                                RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        this.restTemplateBuilder = restTemplateBuilder;
        this.clientCache = new ConcurrentHashMap<>();

        log.info("JiraApiClientFactory initialized with {} instance(s)",
                configuration.getInstances().size());
    }

    /**
     * Resolve the effective instance name.
     * <ul>
     *   <li>If {@code instanceName} is non-null and non-blank, it is returned as-is.</li>
     *   <li>If {@code instanceName} is {@code null} or blank and a default instance is configured
     *       via {@code externalservices.jira.default-instance}, that value is returned.</li>
     *   <li>Otherwise the first entry of the instances map is returned (insertion order).</li>
     *   <li>If no instances are configured at all, a {@link JiraException} is thrown.</li>
     * </ul>
     *
     * @param instanceName Explicit instance name, or {@code null}/{@code ""} to use the default
     * @return The resolved instance name (never {@code null}/blank)
     * @throws JiraException if no Jira instances are configured
     */
    public String resolveInstanceName(String instanceName) throws JiraException {
        if (instanceName != null && !instanceName.isBlank()) {
            return instanceName;
        }

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
     * Get a {@link JiraApiClient} for a specific instance.
     * If {@code instanceName} is {@code null} or blank, the default instance is used.
     *
     * @param instanceName Name of the Jira instance, or {@code null}/{@code ""} for the default
     * @return Configured JiraApiClient
     * @throws JiraException if the instance is not configured
     */
    public JiraApiClient getClient(String instanceName) throws JiraException {
        instanceName = resolveInstanceName(instanceName);

        if (clientCache.containsKey(instanceName)) {
            log.debug("Returning cached client for instance '{}'", instanceName);
            return clientCache.get(instanceName);
        }

        JiraInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);

        if (instanceConfig == null) {
            throw new JiraException(
                    String.format("Jira instance '%s' is not configured. Available instances: %s",
                            instanceName, configuration.getInstances().keySet()));
        }

        log.info("Creating new JiraApiClient for instance '{}'", instanceName);

        RestTemplate restTemplate = createRestTemplate(instanceConfig);
        JiraApiClient client = new JiraApiClient(instanceName, instanceConfig, restTemplate);

        clientCache.put(instanceName, client);

        return client;
    }

    /**
     * Get the default client, as determined by {@code externalservices.jira.default-instance}.
     * Falls back to the first configured instance when {@code default-instance} is not set.
     *
     * @return JiraApiClient for the default instance
     * @throws JiraException if no instances are configured
     */
    public JiraApiClient getDefaultClient() throws JiraException {
        return getClient(null);
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
    public void clearCache() {
        log.info("Clearing JiraApiClient cache");
        clientCache.clear();
    }

    /**
     * Create a configured RestTemplate for a Jira instance.
     *
     * @param config Configuration for the instance
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(JiraInstanceConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(config.getConnectionTimeout());
        requestFactory.setReadTimeout(config.getReadTimeout());
        restTemplate.setRequestFactory(requestFactory);

        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for Jira connection. "
                    + "This should only be used in development environments!");
            configureTrustAllCertificates(restTemplate);
        }

        return restTemplate;
    }

    /**
     * Configure RestTemplate to trust all SSL certificates.
     * WARNING: This should only be used in development environments.
     *
     * @param restTemplate RestTemplate to configure
     */
    @SuppressWarnings({"java:S4830", "java:S1186"})
    private void configureTrustAllCertificates(RestTemplate restTemplate) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    // Intentionally empty - trusting all certificates for development environments
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // No validation performed - development only
                    }
                    // Intentionally empty - trusting all certificates for development environments
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // No validation performed - development only
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // Intentionally disabling hostname verification for development environments
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to configure SSL trust all certificates", e);
        }
    }
}
