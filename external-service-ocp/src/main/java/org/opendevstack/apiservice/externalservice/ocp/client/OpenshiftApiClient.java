package org.opendevstack.apiservice.externalservice.ocp.client;

import org.opendevstack.apiservice.externalservice.ocp.config.OpenshiftServiceConfiguration.OpenshiftInstanceConfig;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException.ErrorCodes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with OpenShift API.
 * Provides methods to retrieve secrets and other resources from an OpenShift cluster.
 * Implements comprehensive error handling with detailed error codes for different failure scenarios.
 */
@Slf4j
public class OpenshiftApiClient {
    
    private final String instanceName;
    private final OpenshiftInstanceConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor for OpenshiftApiClient
     * 
     * @param instanceName Name of the OpenShift instance
     * @param config Configuration for this instance
     * @param restTemplate RestTemplate configured with appropriate timeouts and SSL settings
     */
    public OpenshiftApiClient(String instanceName, OpenshiftInstanceConfig config, RestTemplate restTemplate) {
        this.instanceName = instanceName;
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
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
        
        String url = String.format("%s/api/v1/namespaces/%s/secrets/%s", 
                                   config.getApiUrl(), namespace, secretName);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseSecretData(response.getBody());
            } else {
                throw new OpenshiftException(
                    String.format("Failed to retrieve secret '%s' from namespace '%s'. Status: %s", 
                                  secretName, namespace, response.getStatusCode()),
                    ErrorCodes.INVALID_RESPONSE,
                    "getSecret",
                    instanceName
                );
            }
            
        } catch (HttpClientErrorException e) {
            throw handleHttpClientError(e, "getSecret", buildResourceContext("secret", secretName, namespace));
        } catch (HttpServerErrorException e) {
            throw handleHttpServerError(e, "getSecret", buildResourceContext("secret", secretName, namespace));
        } catch (ResourceAccessException e) {
            throw handleResourceAccessError(e, "getSecret");
        } catch (RestClientException e) {
            throw handleRestClientError(e, "getSecret");
        } catch (OpenshiftException e) {
            // Re-throw OpenshiftException from parseSecretData or status check
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving secret '{}' from instance '{}'", secretName, instanceName, e);
            throw new OpenshiftException(
                String.format("Unexpected error retrieving secret '%s' from instance '%s': %s", 
                              secretName, instanceName, e.getMessage()),
                e,
                ErrorCodes.OPENSHIFT_ERROR,
                "getSecret",
                instanceName
            );
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
                              key, secretName, namespace),
                ErrorCodes.KEY_NOT_FOUND,
                "getSecretValue",
                instanceName
            );
        }
    }
    
    /**
     * Check if a secret exists
     * 
     * @param secretName Name of the secret
     * @return true if the secret exists, false otherwise
     * @throws OpenshiftException if there's a technical failure checking existence
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
     * @throws OpenshiftException if there's a technical failure checking existence
     */
    public boolean secretExists(String secretName, String namespace) throws OpenshiftException {
        try {
            getSecret(secretName, namespace);
            return true;
        } catch (OpenshiftException e) {
            // If it's a NOT_FOUND error, the secret doesn't exist (expected case)
            if (ErrorCodes.NOT_FOUND.equals(e.getErrorCode())) {
                log.debug("Secret '{}' does not exist in namespace '{}'", secretName, namespace);
                return false;
            }
            // For any other technical error (connection, timeout, etc.), propagate it
            log.warn("Technical error checking if secret '{}' exists in namespace '{}': {}", 
                     secretName, namespace, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create HTTP headers with authentication token
     * 
     * @return HttpHeaders with authorization and content type
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    /**
     * Parse secret data from the API response and decode base64 values
     * 
     * @param jsonResponse JSON response from OpenShift API
     * @return Map with decoded secret values
     * @throws OpenshiftException if parsing fails
     */
    private Map<String, String> parseSecretData(String jsonResponse) throws OpenshiftException {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataNode = root.get("data");
            
            Map<String, String> secretData = new HashMap<>();
            
            if (dataNode != null && dataNode.isObject()) {
                dataNode.fieldNames().forEachRemaining(key -> {
                    String base64Value = dataNode.get(key).asText();
                    String decodedValue = decodeBase64(base64Value);
                    secretData.put(key, decodedValue);
                });
            }
            
            log.debug("Successfully parsed secret data with {} keys", secretData.size());
            return secretData;
            
        } catch (Exception e) {
            log.error("Error parsing secret data", e);
            throw new OpenshiftException(
                "Failed to parse secret data from OpenShift response: " + e.getMessage(),
                e,
                ErrorCodes.PARSE_ERROR,
                "parseSecretData",
                instanceName
            );
        }
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
     * Validate connection to the OpenShift instance by calling the whoami endpoint.
     * 
     * @return The authenticated user/service account name
     * @throws OpenshiftException if connection or authentication fails
     */
    public String whoAmI() throws OpenshiftException {
        log.debug("Validating connection to OpenShift instance '{}'", instanceName);
        
        String url = String.format("%s/apis/user.openshift.io/v1/users/~", config.getApiUrl());
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String username = parseUsername(response.getBody());
                log.info("Successfully connected to OpenShift instance '{}' as '{}'", instanceName, username);
                return username;
            } else {
                throw new OpenshiftException(
                    String.format("Failed to validate connection to instance '%s'. Status: %s", 
                                  instanceName, response.getStatusCode()),
                    ErrorCodes.INVALID_RESPONSE,
                    "validateConnection",
                    instanceName
                );
            }
            
        } catch (HttpClientErrorException e) {
            throw handleHttpClientError(e, "validateConnection", null);
        } catch (HttpServerErrorException e) {
            throw handleHttpServerError(e, "validateConnection", null);
        } catch (ResourceAccessException e) {
            throw handleResourceAccessError(e, "validateConnection");
        } catch (RestClientException e) {
            throw handleRestClientError(e, "validateConnection");
        } catch (OpenshiftException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error validating connection to instance '{}'", instanceName, e);
            throw new OpenshiftException(
                String.format("Unexpected error validating connection to instance '%s': %s", 
                              instanceName, e.getMessage()),
                e,
                ErrorCodes.OPENSHIFT_ERROR,
                "validateConnection",
                instanceName
            );
        }
    }
    
    /**
     * Check if connection to the OpenShift instance is valid.
     * 
     * @return true if connection is valid, false otherwise
     */
    public boolean isConnectionValid() {
        try {
            whoAmI();
            return true;
        } catch (OpenshiftException e) {
            log.debug("Connection validation failed for instance '{}': {}", instanceName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Parse the username from the whoami response
     * 
     * @param jsonResponse JSON response from the users/~ endpoint
     * @return The username
     * @throws OpenshiftException if parsing fails
     */
    private String parseUsername(String jsonResponse) throws OpenshiftException {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode metadataNode = root.get("metadata");
            
            if (metadataNode != null && metadataNode.has("name")) {
                return metadataNode.get("name").asText();
            }
            
            // Fallback: return "unknown" if name not found
            log.warn("Could not extract username from whoami response");
            return "unknown";
            
        } catch (Exception e) {
            log.error("Error parsing whoami response", e);
            throw new OpenshiftException(
                "Failed to parse whoami response: " + e.getMessage(),
                e,
                ErrorCodes.PARSE_ERROR,
                "parseUsername",
                instanceName
            );
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

    // ========================================================================
    // ERROR HANDLING
    // ========================================================================
    
    /**
     * Handle HTTP 4xx client errors with specific error codes.
     * 
     * @param e The HTTP client error exception
     * @param operation The operation being performed
     * @param resourceContext Optional context about the resource (e.g., "secret 'my-secret' in namespace 'ns'")
     * @return OpenshiftException with appropriate error code and message
     */
    private OpenshiftException handleHttpClientError(HttpClientErrorException e, 
                                                      String operation, 
                                                      String resourceContext) {
        String errorCode;
        String message;
        
        switch (e.getStatusCode().value()) {
            case 401:
                errorCode = ErrorCodes.UNAUTHORIZED;
                message = String.format("Unauthorized: Token is invalid or expired for instance '%s'", instanceName);
                break;
            case 403:
                errorCode = ErrorCodes.FORBIDDEN;
                message = resourceContext != null
                    ? String.format("Forbidden: Insufficient permissions to access %s on instance '%s'", resourceContext, instanceName)
                    : String.format("Forbidden: Insufficient permissions on instance '%s'", instanceName);
                break;
            case 404:
                errorCode = ErrorCodes.NOT_FOUND;
                message = resourceContext != null
                    ? String.format("Not found: %s on instance '%s'", resourceContext, instanceName)
                    : String.format("Resource not found on instance '%s'", instanceName);
                break;
            case 400:
                errorCode = ErrorCodes.BAD_REQUEST;
                message = resourceContext != null
                    ? String.format("Bad request for %s: %s", resourceContext, e.getMessage())
                    : String.format("Bad request: %s", e.getMessage());
                break;
            default:
                errorCode = ErrorCodes.OPENSHIFT_ERROR;
                message = resourceContext != null
                    ? String.format("HTTP %d error accessing %s on instance '%s': %s", 
                                   e.getStatusCode().value(), resourceContext, instanceName, e.getMessage())
                    : String.format("HTTP %d error on instance '%s': %s", 
                                   e.getStatusCode().value(), instanceName, e.getMessage());
        }
        
        log.error("{} (HTTP {})", message, e.getStatusCode().value(), e);
        return new OpenshiftException(message, e, errorCode, operation, instanceName);
    }
    
    /**
     * Handle HTTP 5xx server errors.
     * 
     * @param e The HTTP server error exception
     * @param operation The operation being performed
     * @param resourceContext Optional context about the resource
     * @return OpenshiftException with appropriate error code and message
     */
    private OpenshiftException handleHttpServerError(HttpServerErrorException e,
                                                      String operation,
                                                      String resourceContext) {
        String errorCode = (e.getStatusCode().value() == 503) 
            ? ErrorCodes.SERVICE_UNAVAILABLE 
            : ErrorCodes.INTERNAL_SERVER_ERROR;
        
        String message = resourceContext != null
            ? String.format("OpenShift server error (HTTP %d) accessing %s on instance '%s': %s",
                           e.getStatusCode().value(), resourceContext, instanceName, e.getMessage())
            : String.format("OpenShift server error (HTTP %d) on instance '%s': %s",
                           e.getStatusCode().value(), instanceName, e.getMessage());
        
        log.error(message, e);
        return new OpenshiftException(message, e, errorCode, operation, instanceName);
    }
    
    /**
     * Handle resource access errors (connection, timeout, I/O).
     * 
     * @param e The resource access exception
     * @param operation The operation being performed
     * @return OpenshiftException with appropriate error code and message
     */
    private OpenshiftException handleResourceAccessError(ResourceAccessException e, String operation) {
        Throwable cause = e.getCause();
        String errorCode;
        String message;
        
        if (cause instanceof SocketTimeoutException) {
            errorCode = ErrorCodes.READ_TIMEOUT;
            message = String.format(
                "Read timeout on instance '%s'. The API did not respond within %dms.",
                instanceName, config.getReadTimeout()
            );
        } else if (cause instanceof ConnectException) {
            errorCode = ErrorCodes.CONNECTION_FAILED;
            message = String.format(
                "Connection failed to instance '%s' (URL: %s): %s",
                instanceName, config.getApiUrl(), cause.getMessage()
            );
        } else if (cause instanceof UnknownHostException) {
            errorCode = ErrorCodes.DNS_RESOLUTION_FAILED;
            message = String.format(
                "DNS resolution failed for instance '%s' (URL: %s): %s",
                instanceName, config.getApiUrl(), cause.getMessage()
            );
        } else if (cause instanceof IOException) {
            errorCode = ErrorCodes.CONNECTION_FAILED;
            message = String.format(
                "I/O error communicating with instance '%s': %s",
                instanceName, cause.getMessage()
            );
        } else {
            errorCode = ErrorCodes.CONNECTION_FAILED;
            message = String.format(
                "Resource access error on instance '%s': %s",
                instanceName, e.getMessage()
            );
        }
        
        log.error(message, e);
        return new OpenshiftException(message, e, errorCode, operation, instanceName);
    }
    
    /**
     * Handle generic RestClient errors.
     * 
     * @param e The REST client exception
     * @param operation The operation being performed
     * @return OpenshiftException with appropriate error code and message
     */
    private OpenshiftException handleRestClientError(RestClientException e, String operation) {
        String message = String.format(
            "REST client error on instance '%s': %s",
            instanceName, e.getMessage()
        );
        
        log.error(message, e);
        return new OpenshiftException(message, e, ErrorCodes.OPENSHIFT_ERROR, operation, instanceName);
    }
    
    /**
     * Build a resource context string for error messages.
     * 
     * @param resourceType Type of resource (e.g., "secret")
     * @param resourceName Name of the resource
     * @param namespace Optional namespace
     * @return Formatted context string
     */
    private String buildResourceContext(String resourceType, String resourceName, String namespace) {
        if (namespace != null) {
            return String.format("%s '%s' in namespace '%s'", resourceType, resourceName, namespace);
        }
        return String.format("%s '%s'", resourceType, resourceName);
    }    
}

