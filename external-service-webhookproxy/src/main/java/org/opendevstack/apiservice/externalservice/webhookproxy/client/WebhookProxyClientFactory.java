package org.opendevstack.apiservice.externalservice.webhookproxy.client;

import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration;
import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration.ClusterConfig;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating WebhookProxyClient instances.
 * Uses the factory pattern to provide configured clients for different
 * clusters.
 * Clients are cached per cluster+project combination for efficiency.
 */
@Component
@Slf4j
public class WebhookProxyClientFactory {

    private final WebhookProxyConfiguration configuration;
    private final Map<String, WebhookProxyClient> clientCache;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Constructor with dependency injection
     * 
     * @param configuration       Webhook proxy configuration
     * @param restTemplateBuilder RestTemplate builder for creating HTTP clients
     */
    public WebhookProxyClientFactory(WebhookProxyConfiguration configuration,
            RestTemplateBuilder restTemplateBuilder) {
        this.configuration = configuration;
        this.restTemplateBuilder = restTemplateBuilder;
        this.clientCache = new ConcurrentHashMap<>();

        log.info("WebhookProxyClientFactory initialized with {} cluster(s)",
                configuration.getClusters().size());
    }

    /**
     * Get a WebhookProxyClient for a specific cluster and project
     * 
     * @param clusterName Name of the cluster (e.g., "cluster-a", "cluster-b")
     * @param projectKey  Project key (e.g., "example-project")
     * @return Configured WebhookProxyClient
     * @throws WebhookProxyException.ConfigurationException if the cluster is not
     *                                                      configured
     */
    public WebhookProxyClient getClient(String clusterName, String projectKey)
            throws WebhookProxyException.ConfigurationException {

        String cacheKey = clusterName + ":" + projectKey;

        // Check cache first
        if (clientCache.containsKey(cacheKey)) {
            log.debug("Returning cached client for cluster '{}' and project '{}'", clusterName, projectKey);
            return clientCache.get(cacheKey);
        }

        // Get cluster configuration
        ClusterConfig clusterConfig = configuration.getClusters().get(clusterName);

        if (clusterConfig == null) {
            throw new WebhookProxyException.ConfigurationException(
                    String.format("Cluster '%s' is not configured. Available clusters: %s",
                            clusterName, configuration.getClusters().keySet()));
        }

        log.info("Creating new WebhookProxyClient for cluster '{}' and project '{}'", clusterName, projectKey);

        // Build the webhook proxy URL dynamically
        String webhookProxyUrl = clusterConfig.buildWebhookProxyUrl(projectKey);
        log.debug("Webhook proxy URL: {}", webhookProxyUrl);

        RestTemplate restTemplate = createRestTemplate(clusterConfig);
        WebhookProxyClient client = new WebhookProxyClient(clusterName, projectKey, webhookProxyUrl,
                clusterConfig, restTemplate);

        // Cache the client
        clientCache.put(cacheKey, client);

        return client;
    }

    /**
     * Get all available cluster names
     * 
     * @return Set of configured cluster names
     */
    public Set<String> getAvailableClusters() {
        return configuration.getClusters().keySet();
    }

    /**
     * Check if a cluster is configured
     * 
     * @param clusterName Name of the cluster to check
     * @return true if configured, false otherwise
     */
    public boolean hasCluster(String clusterName) {
        return configuration.getClusters().containsKey(clusterName);
    }

    /**
     * Create a RestTemplate configured for a specific cluster.
     *
     * @param config Cluster configuration
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(ClusterConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        if (config.isTrustAllCertificates()) {
            log.warn("Creating RestTemplate with SSL certificate verification DISABLED for webhook proxy - " +
                    "this should only be used in development environments");
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
     * @param config Cluster configuration (for timeouts)
     * @return A request factory whose connections skip SSL verification
     */
    @SuppressWarnings({"java:S4830", "java:S1186"}) // Intentionally disabling SSL validation for development
    private SimpleClientHttpRequestFactory createTrustAllRequestFactory(ClusterConfig config) {
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
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
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
