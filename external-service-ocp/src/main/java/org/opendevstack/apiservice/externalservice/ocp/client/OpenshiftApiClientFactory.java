package org.opendevstack.apiservice.externalservice.ocp.client;

import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration;
import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration.OpenshiftInstanceConfig;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Factory for creating OpenshiftApiClient instances.
 * Uses the factory pattern to provide configured Fabric8 OpenShift clients for different instances.
 * Clients are cached and reused for efficiency.
 */
@Component
@Slf4j
public class OpenshiftApiClientFactory {
    
    private final OpenshiftServiceConfiguration configuration;
    
    /**
     * Constructor with dependency injection
     * 
     * @param configuration OpenShift service configuration
     */
    public OpenshiftApiClientFactory(OpenshiftServiceConfiguration configuration) {
        this.configuration = configuration;
        
        log.info("OpenshiftApiClientFactory initialized with {} instance(s)", 
                 configuration.getInstances().size());
    }
    
    /**
     * Get an OpenshiftApiClient for a specific instance.
     * If {@code instanceName} is {@code null} or blank, an exception is thrown.
     * 
     * @param instanceName Name of the OpenShift instance
     * @return Configured OpenshiftApiClient
     * @throws OpenshiftException if the instance is not configured
     */
    @Cacheable(value = "openshiftApiClients", key = "#instanceName", condition = "#instanceName != null && !#instanceName.isBlank()")
    public OpenshiftApiClient getClient(String instanceName) throws OpenshiftException {
        if (instanceName == null || instanceName.isBlank()) {
            throw new OpenshiftException(
                String.format("Provide instance name. Available instances: %s",
                       configuration.getInstances().keySet()));
        }

        OpenshiftInstanceConfig instanceConfig = configuration.getInstances().get(instanceName);
        
        if (instanceConfig == null) {
            throw new OpenshiftException(
                String.format("OpenShift instance '%s' is not configured. Available instances: %s", 
                              instanceName, configuration.getInstances().keySet())
            );
        }
        
        log.info("Creating new OpenshiftApiClient for instance '{}'", instanceName);
        
        OpenShiftClient openShiftClient = createOpenShiftClient(instanceConfig);
        return new OpenshiftApiClient(instanceName, instanceConfig, openShiftClient);
    }
    
    /**
     * Get the default client (first configured instance).
     * 
     * @return OpenshiftApiClient for the first configured instance
     * @throws OpenshiftException if no instances are configured
     */
    @Cacheable(value = "openshiftApiClients", key = "'default'")
    public OpenshiftApiClient getDefaultClient() throws OpenshiftException {
        if (configuration.getInstances().isEmpty()) {
            throw new OpenshiftException("No OpenShift instances configured");
        }
        
        String firstInstanceName = configuration.getInstances().keySet().iterator().next();
        log.debug("Using default instance: '{}'", firstInstanceName);

        OpenshiftInstanceConfig instanceConfig = configuration.getInstances().get(firstInstanceName);
        OpenShiftClient openShiftClient = createOpenShiftClient(instanceConfig);
        return new OpenshiftApiClient(firstInstanceName, instanceConfig, openShiftClient);
    }
    
    /**
     * Get all available instance names
     * 
     * @return Set of configured instance names
     */
    public Set<String> getAvailableInstances() {
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
     * Clear the client cache (useful for testing or when configuration changes).
     */
    @CacheEvict(value = "openshiftApiClients", allEntries = true)
    public void clearCache() {
        log.info("Clearing OpenshiftApiClient cache");
    }
    
    /**
     * Create a configured Fabric8 OpenShiftClient for an OpenShift instance
     * 
     * @param config Configuration for the instance
     * @return Configured OpenShiftClient
     */
    private OpenShiftClient createOpenShiftClient(OpenshiftInstanceConfig config) {
        ConfigBuilder configBuilder = new ConfigBuilder()
                .withMasterUrl(config.getApiUrl())
                .withOauthToken(config.getToken())
                .withNamespace(config.getNamespace())
                .withConnectionTimeout(config.getConnectionTimeout())
                .withRequestTimeout(config.getReadTimeout());

        if (config.isTrustAllCertificates()) {
            log.warn("Trust all certificates is enabled for OpenShift connection. " +
                    "This should only be used in development environments!");
            configBuilder.withTrustCerts(true)
                         .withDisableHostnameVerification(true);
        }

        return new KubernetesClientBuilder()
                .withConfig(configBuilder.build())
                .build()
                .adapt(OpenShiftClient.class);
    }
}

