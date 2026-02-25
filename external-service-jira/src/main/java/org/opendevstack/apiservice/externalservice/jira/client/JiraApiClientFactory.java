package org.opendevstack.apiservice.externalservice.jira.client;

import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration;
import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration.JiraInstanceConfig;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating {@link JiraApiClient} instances.
 * Uses the factory pattern to provide configured clients for different Jira instances.
 * Clients are cached and reused for efficiency.
 */
@Component
@Slf4j
public class JiraApiClientFactory {

    private final JiraServiceConfiguration configuration;
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

        log.info("JiraApiClientFactory initialized with {} instance(s)",
                configuration.getInstances().size());
    }

    /**
     * Resolve the effective instance name.
     * <ul>
     *   <li>If the default instance is configured via {@code externalservices.jira.default-instance}, it is returned.</li>
     *   <li>Otherwise the first entry of the instances map is returned (insertion order).</li>
     *   <li>If no instances are configured at all, a {@link JiraException} is thrown.</li>
     * </ul>
     *
     * @return The resolved default instance name (never {@code null}/blank)
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
     * Get a {@link JiraApiClient} for a specific instance.
     * If {@code instanceName} is {@code null} or blank, the default instance is used.
     *
     * @param instanceName Name of the Jira instance, or {@code null}/{@code ""} for the default
     * @return Configured JiraApiClient
     * @throws JiraException if the instance is not configured
     */
    @Cacheable(value = "jiraApiClients", key = "#instanceName")
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

        RestTemplate restTemplate = createRestTemplate(instanceConfig);
        return new JiraApiClient(instanceName, instanceConfig, restTemplate);
    }

    /**
     * Get the default client, as determined by {@code externalservices.jira.default-instance}.
     * Falls back to the first configured instance when {@code default-instance} is not set.
     *
     * @return JiraApiClient for the default instance
     * @throws JiraException if no instances are configured
     */
    @Cacheable(value = "jiraApiClients", key = "'default'")
    public JiraApiClient getClient() throws JiraException {
        String defaultInstanceName = getDefaultInstanceName();
        JiraInstanceConfig instanceConfig = configuration.getInstances().get(defaultInstanceName);
        RestTemplate restTemplate = createRestTemplate(instanceConfig);

        return  new JiraApiClient(defaultInstanceName, instanceConfig, restTemplate);
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
    @CacheEvict(value = "jiraApiClients", allEntries = true)
    public void clearCache() {
        log.info("Clearing JiraApiClient cache");
    }

    /**
     * Create a configured RestTemplate for a Jira instance.
     *
     * @param config Configuration for the instance
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(JiraInstanceConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for Jira connection. "
                    + "This should only be used in development environments!");
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
    @SuppressWarnings({"java:S4830", "java:S1186"})
    private SimpleClientHttpRequestFactory createTrustAllRequestFactory(JiraInstanceConfig config) {
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
