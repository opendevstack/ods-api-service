package org.opendevstack.apiservice.externalservice.webhookproxy.client;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.http.ExternalServiceSslProperties;
import org.opendevstack.apiservice.externalservice.api.http.RestClientFactory;
import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration;
import org.opendevstack.apiservice.externalservice.webhookproxy.config.WebhookProxyConfiguration.ClusterConfig;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

/**
 * Factory for creating {@link WebhookProxyClient} instances.
 *
 * <p>SSL wiring is delegated to {@link RestClientFactory} in {@code external-service-api}.
 * A {@link RestTemplate} is produced because {@link WebhookProxyClient} is not backed by a
 * generated OpenAPI client — it uses {@code RestTemplate} directly.
 *
 * <p>Clients are cached per {@code clusterName:projectKey} via Spring's {@code @Cacheable}
 * (replaces the earlier manual {@code ConcurrentHashMap} cache).
 */
@Component
@Slf4j
public class WebhookProxyClientFactory {

    private final WebhookProxyConfiguration configuration;

    public WebhookProxyClientFactory(WebhookProxyConfiguration configuration) {
        this.configuration = configuration;
        log.info("WebhookProxyClientFactory initialized with {} cluster(s)",
                configuration.getClusters().size());
    }

    /**
     * Get a {@link WebhookProxyClient} for a specific cluster and project.
     *
     * @param clusterName Name of the cluster (e.g. {@code "cluster-a"})
     * @param projectKey  Project key (e.g. {@code "example-project"})
     * @return Configured {@link WebhookProxyClient}
     * @throws WebhookProxyException.ConfigurationException if the cluster is not configured
     */
    @Cacheable(value = "webhookProxyClients", key = "#clusterName + ':' + #projectKey",
            condition = "#clusterName != null && #projectKey != null")
    public WebhookProxyClient getClient(String clusterName, String projectKey)
            throws WebhookProxyException.ConfigurationException {

        ClusterConfig clusterConfig = configuration.getClusters().get(clusterName);
        if (clusterConfig == null) {
            throw new WebhookProxyException.ConfigurationException(
                    String.format("Cluster '%s' is not configured. Available clusters: %s",
                            clusterName, configuration.getClusters().keySet()));
        }

        log.info("Creating new WebhookProxyClient for cluster '{}' and project '{}'", clusterName, projectKey);

        String webhookProxyUrl = clusterConfig.buildWebhookProxyUrl(projectKey);
        log.debug("Webhook proxy URL: {}", webhookProxyUrl);

        RestTemplate restTemplate = buildRestTemplate(clusterConfig);
        return new WebhookProxyClient(clusterName, projectKey, webhookProxyUrl, clusterConfig, restTemplate);
    }

    /** @return all configured cluster names */
    public Set<String> getAvailableClusters() {
        return configuration.getClusters().keySet();
    }

    /** @return {@code true} if the named cluster is configured */
    public boolean hasCluster(String clusterName) {
        return configuration.getClusters().containsKey(clusterName);
    }

    /** Clear the client cache (useful for testing or when configuration changes). */
    @CacheEvict(value = "webhookProxyClients", allEntries = true)
    public void clearCache() {
        log.info("Clearing WebhookProxyClient cache");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private RestTemplate buildRestTemplate(ClusterConfig config) {
        log.info("Creating RestTemplate for webhook proxy cluster (connect={}ms, read={}ms)",
                config.getConnectionTimeout(), config.getReadTimeout());
        return RestClientFactory.buildRestTemplate(
                toSslProperties(config),
                config.getConnectionTimeout(),
                config.getReadTimeout());
    }

    /**
     * Adapt {@link ClusterConfig} SSL fields to {@link ExternalServiceSslProperties}
     * so we can pass them to {@link RestClientFactory} without changing the config class.
     */
    private static ExternalServiceSslProperties toSslProperties(ClusterConfig config) {
        ExternalServiceSslProperties ssl = new ExternalServiceSslProperties();
        ssl.setTrustStorePath(config.getTrustStorePath());
        ssl.setTrustStorePassword(config.getTrustStorePassword());
        ssl.setTrustStoreType(config.getTrustStoreType());
        return ssl;
    }
}
