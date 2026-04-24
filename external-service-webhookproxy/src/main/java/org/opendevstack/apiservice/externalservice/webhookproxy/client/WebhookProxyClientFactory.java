package org.opendevstack.apiservice.externalservice.webhookproxy.client;

import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration;
import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration.ClusterConfig;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.util.StringUtils;

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
     * Create a RestTemplate configured for a specific cluster
     * 
     * @param config Cluster configuration
     * @return Configured RestTemplate
     */
    private RestTemplate createRestTemplate(ClusterConfig config) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        if (!StringUtils.hasText(config.getTrustStorePath())) {
            log.info("No custom trust store configured for webhook proxy cluster, using JVM default trust store");
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(config.getConnectionTimeout());
            requestFactory.setReadTimeout(config.getReadTimeout());
            restTemplate.setRequestFactory(requestFactory);
            return restTemplate;
        }

        try {
            log.info("Loading custom trust store for webhook proxy cluster from: {}", config.getTrustStorePath());
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
            log.error("Failed to load custom trust store '{}' for webhook proxy cluster, falling back to JVM default: {}",
                    config.getTrustStorePath(), e.getMessage());
            SimpleClientHttpRequestFactory fallback = new SimpleClientHttpRequestFactory();
            fallback.setConnectTimeout(config.getConnectionTimeout());
            fallback.setReadTimeout(config.getReadTimeout());
            restTemplate.setRequestFactory(fallback);
        }

        return restTemplate;
    }
}
