package org.opendevstack.apiservice.externalservice.ocp.client;

import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration;
import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration.OpenshiftInstanceConfig;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating OpenshiftApiClient instances.
 * Uses the factory pattern to provide configured clients for different OpenShift instances.
 * Clients are cached and reused for efficiency.
 */
@Component
@Slf4j
public class OpenshiftApiClientFactory {
    
    private final OpenshiftServiceConfiguration configuration;
    private final Map<String, OpenshiftApiClient> clientCache;
    private final RestTemplateBuilder restTemplateBuilder;
    
    /**
     * Constructor with dependency injection
     * 
     * @param configuration OpenShift service configuration
     * @param restTemplateBuilder RestTemplate builder for creating HTTP clients
     */
    public OpenshiftApiClientFactory(OpenshiftServiceConfiguration configuration, 
                                     RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        this.restTemplateBuilder = restTemplateBuilder;
        this.clientCache = new ConcurrentHashMap<>();
        
        log.info("OpenshiftApiClientFactory initialized with {} instance(s)", 
                 configuration.getInstances().size());
    }
    
    /**
     * Get an OpenshiftApiClient for a specific instance
     * 
     * @param instanceName Name of the OpenShift instance
     * @return Configured OpenshiftApiClient
     * @throws OpenshiftException if the instance is not configured
     */
    public OpenshiftApiClient getClient(String instanceName) throws OpenshiftException {
        // Check cache first
        if (clientCache.containsKey(instanceName)) {
            log.debug("Returning cached client for instance '{}'", instanceName);
            return clientCache.get(instanceName);
        }
        
        // Create new client
        OpenshiftInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);
        
        if (instanceConfig == null) {
            throw new OpenshiftException(
                String.format("OpenShift instance '%s' is not configured. Available instances: %s", 
                              instanceName, configuration.getInstances().keySet())
            );
        }
        
        log.info("Creating new OpenshiftApiClient for instance '{}'", instanceName);
        
        RestTemplate restTemplate = createRestTemplate(instanceConfig);
        OpenshiftApiClient client = new OpenshiftApiClient(instanceName, instanceConfig, restTemplate);
        
        // Cache the client
        clientCache.put(instanceName, client);
        
        return client;
    }
    
    /**
     * Get the default client (first configured instance)
     * 
     * @return OpenshiftApiClient for the first configured instance
     * @throws OpenshiftException if no instances are configured
     */
    public OpenshiftApiClient getDefaultClient() throws OpenshiftException {
        if (configuration.getInstances().isEmpty()) {
            throw new OpenshiftException("No OpenShift instances configured");
        }
        
        String firstInstanceName = configuration.getInstances().keySet().iterator().next();
        log.debug("Using default instance: '{}'", firstInstanceName);
        
        return getClient(firstInstanceName);
    }
    
    /**
     * Get all available instance names
     * 
     * @return Set of configured instance names
     */
    public java.util.Set<String> getAvailableInstances() {
        return configuration.getInstances().keySet();
    }
    
    /**
     * Check if an instance is configured
     * 
     * @param instanceName Name of the instance to check
     * @return true if configured, false otherwise
     */
    public boolean hasInstance(String instanceName) {
        return configuration.getInstances().containsKey(instanceName);
    }
    
    /**
     * Clear the client cache (useful for testing or when configuration changes)
     */
    public void clearCache() {
        log.info("Clearing OpenshiftApiClient cache");
        clientCache.clear();
    }
    
    /**
     * Create a configured RestTemplate for an OpenShift instance.
     *
     * @param config Configuration for the instance
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(OpenshiftInstanceConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for OpenShift connection. " +
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
    private SimpleClientHttpRequestFactory createTrustAllRequestFactory(OpenshiftInstanceConfig config) {
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
