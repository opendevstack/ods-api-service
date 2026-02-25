package org.opendevstack.apiservice.externalservice.ocp.client;

import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration.OpenshiftInstanceConfig;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.authorization.v1.SelfSubjectAccessReview;
import io.fabric8.kubernetes.api.model.authorization.v1.SelfSubjectAccessReviewBuilder;
import io.fabric8.kubernetes.api.model.authorization.v1.ResourceAttributes;
import io.fabric8.kubernetes.api.model.authorization.v1.ResourceAttributesBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with OpenShift API using Fabric8 OpenShift Client.
 * Provides methods to retrieve secrets and other resources from an OpenShift cluster.
 */
@Slf4j
public class OpenshiftApiClient {
    
    private final String instanceName;
    private final OpenshiftInstanceConfig config;
    private final OpenShiftClient openShiftClient;
    
    /**
     * Constructor for OpenshiftApiClient
     * 
     * @param instanceName Name of the OpenShift instance
     * @param config Configuration for this instance
     * @param openShiftClient Fabric8 OpenShift client configured for this instance
     */
    public OpenshiftApiClient(String instanceName, OpenshiftInstanceConfig config, OpenShiftClient openShiftClient) {
        this.instanceName = instanceName;
        this.config = config;
        this.openShiftClient = openShiftClient;
    }
    
    /**
     * Get a secret from the OpenShift cluster
     * 
     * @param secretName Name of the secret to retrieve
     * @return Map containing the decoded secret data
     * @throws OpenshiftException if the secret cannot be retrieved
     */
    public Map<String, String> getSecret(String secretName) throws OpenshiftException {
        return getSecret(secretName, config.getNamespace());
    }
    
    /**
     * Get a secret from a specific namespace in the OpenShift cluster
     * 
     * @param secretName Name of the secret to retrieve
     * @param namespace Namespace where the secret is located
     * @return Map containing the decoded secret data
     * @throws OpenshiftException if the secret cannot be retrieved
     */
    public Map<String, String> getSecret(String secretName, String namespace) throws OpenshiftException {
        log.debug("Retrieving secret '{}' from namespace '{}' in OpenShift instance '{}'", 
                  secretName, namespace, instanceName);
        
        try {
            Secret secret = openShiftClient.secrets()
                    .inNamespace(namespace)
                    .withName(secretName)
                    .get();
            
            if (secret == null) {
                throw new OpenshiftException(
                    String.format("Failed to retrieve secret '%s' from namespace '%s'. Secret not found.", 
                                  secretName, namespace)
                );
            }
            
            return decodeSecretData(secret);
            
        } catch (KubernetesClientException e) {
            log.error("Error retrieving secret '{}' from OpenShift instance '{}'", secretName, instanceName, e);
            throw new OpenshiftException(
                String.format("Failed to retrieve secret '%s' from OpenShift instance '%s'", secretName, instanceName), e);
        }
    }
    
    /**
     * Get a specific value from a secret
     * 
     * @param secretName Name of the secret
     * @param key Key within the secret data
     * @return The decoded secret value
     * @throws OpenshiftException if the secret or key cannot be retrieved
     */
    public String getSecretValue(String secretName, String key) throws OpenshiftException {
        return getSecretValue(secretName, key, config.getNamespace());
    }
    
    /**
     * Get a specific value from a secret in a specific namespace
     * 
     * @param secretName Name of the secret
     * @param key Key within the secret data
     * @param namespace Namespace where the secret is located
     * @return The decoded secret value
     * @throws OpenshiftException if the secret or key cannot be retrieved
     */
    public String getSecretValue(String secretName, String key, String namespace) throws OpenshiftException {
        Map<String, String> secretData = getSecret(secretName, namespace);
        
        if (secretData.containsKey(key)) {
            return secretData.get(key);
        } else {
            throw new OpenshiftException(
                String.format("Key '%s' not found in secret '%s' in namespace '%s'", 
                              key, secretName, namespace)
            );
        }
    }
    
    /**
     * Check if a secret exists
     * 
     * @param secretName Name of the secret
     * @return true if the secret exists, false otherwise
     */
    public boolean secretExists(String secretName) throws OpenshiftException {
        return secretExists(secretName, config.getNamespace());
    }
    
    /**
     * Check if a secret exists in a specific namespace
     * 
     * @param secretName Name of the secret
     * @param namespace Namespace where the secret might be located
     * @return true if the secret exists, false otherwise
     */
    public boolean secretExists(String secretName, String namespace) throws OpenshiftException {
        try {
            Secret secret = openShiftClient.secrets()
                    .inNamespace(namespace)
                    .withName(secretName)
                    .get();
            boolean exists = secret != null;
            if (!exists) {
                log.debug("Secret '{}' does not exist in namespace '{}'", secretName, namespace);
            }
            return exists;
        } catch (KubernetesClientException e) {
            if (e.getCode() == 403) {
                log.debug("Secret '{}' does not exist in namespace '{}' or you don't have access to it", secretName, namespace);
                return false;
            }
            log.error("Error checking if secret '{}' exists in namespace '{}'", 
                      secretName, namespace, namespace, e);
            throw new OpenshiftException(
                String.format("Failed to check if secret '%s' exists in OpenShift namespace '%s'", 
                              secretName, namespace), e);
        }
    }
    
    /**
     * Check if a project exists in the OpenShift cluster
     * 
     * @param projectName Name of the project
     * @return true if the project exists, false if not found
     * @throws OpenshiftException if any other error occurs
     */
    public boolean projectExists(String projectName) throws OpenshiftException {
        try {
            log.debug("Checking if project '{}' exists in OpenShift instance '{}'", projectName, instanceName);
            
            Project project = openShiftClient.projects()
                    .withName(projectName)
                    .get();
            
            if (project != null) {
                log.debug("Project '{}' exists in instance '{}'", projectName, instanceName);
                return true;
            } else {
                log.debug("Project '{}' does not exist in instance '{}'", projectName, instanceName);
                return false;
            }
            
        } catch (KubernetesClientException e) {
            if (e.getCode() == 403) {
                log.debug("Project '{}' does not exist in instance '{}' or you don't have access to it", projectName, instanceName);
                return false;
            }
            log.error("Error checking if project '{}' exists in instance '{}': {}", 
                     projectName, instanceName, e.getMessage(), e);
            throw new OpenshiftException(
                String.format("Failed to check if project '%s' exists in OpenShift instance '%s'", 
                              projectName, instanceName),
                e
            );
        }
    }
    
    /**
     * Decode secret data from a Fabric8 Secret object, decoding base64 values
     * 
     * @param secret The Fabric8 Secret object
     * @return Map with decoded secret values
     */
    private Map<String, String> decodeSecretData(Secret secret) {
        Map<String, String> secretData = new HashMap<>();
        Map<String, String> data = secret.getData();
        
        if (data != null) {
            data.forEach((key, base64Value) -> {
                String decodedValue = decodeBase64(base64Value);
                secretData.put(key, decodedValue);
            });
        }
        
        log.debug("Successfully parsed secret data with {} keys", secretData.size());
        return secretData;
    }
    
    /**
     * Decode a base64 encoded string
     * 
     * @param base64Value Base64 encoded string
     * @return Decoded string
     */
    private String decodeBase64(String base64Value) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode base64 value, returning original value", e);
            return base64Value;
        }
    }
    
    /**
     * Get the instance name this client is configured for
     * 
     * @return Instance name
     */
    public String getInstanceName() {
        return instanceName;
    }
    
    /**
     * Get the default namespace for this instance
     * 
     * @return Default namespace
     */
    public String getDefaultNamespace() {
        return config.getNamespace();
    }
    
    /**
     * Check if the current service account has permission to get secrets in the given namespace.
     * Uses a SelfSubjectAccessReview to verify RBAC permissions.
     *
     * @param namespace Namespace to check permissions in
     * @return true if the account can get secrets, false otherwise
     */
    public boolean canGetSecrets(String namespace) {
        return canGetSecrets(namespace, null);
    }

    /**
     * Check if the current service account has permission to get a specific secret (or secrets in general)
     * in the given namespace. Uses a SelfSubjectAccessReview to verify RBAC permissions.
     *
     * @param namespace Namespace to check permissions in
     * @param secretName Optional specific secret name; if null, checks general access to secrets
     * @return true if the account can get secrets, false otherwise
     */
    public boolean canGetSecrets(String namespace, String secretName) {
        try {
            log.debug("Checking 'get' permission on secrets in namespace '{}' for instance '{}'",
                      namespace, instanceName);

            ResourceAttributesBuilder raBuilder = new ResourceAttributesBuilder()
                    .withNamespace(namespace)
                    .withVerb("get")
                    .withResource("secrets");

            if (secretName != null && !secretName.isEmpty()) {
                raBuilder.withName(secretName);
            }

            SelfSubjectAccessReview review = new SelfSubjectAccessReviewBuilder()
                    .withNewSpec()
                        .withResourceAttributes(raBuilder.build())
                    .endSpec()
                    .build();

            SelfSubjectAccessReview result = openShiftClient.authorization().v1()
                    .selfSubjectAccessReview()
                    .create(review);

            boolean allowed = result.getStatus() != null && Boolean.TRUE.equals(result.getStatus().getAllowed());

            if (!allowed) {
                String reason = result.getStatus() != null ? result.getStatus().getReason() : "unknown";
                log.warn("Permission denied: cannot 'get' secrets in namespace '{}' on instance '{}'. Reason: {}",
                         namespace, instanceName, reason);
            } else {
                log.debug("Permission granted: can 'get' secrets in namespace '{}' on instance '{}'",
                          namespace, instanceName);
            }

            return allowed;

        } catch (KubernetesClientException e) {
            log.error("Error checking secret access permissions in namespace '{}' on instance '{}'",
                      namespace, instanceName, e);
            return false;
        }
    }

    /**
     * Close the underlying OpenShift client
     */
    public void close() {
        if (openShiftClient != null) {
            openShiftClient.close();
        }
    }
}

