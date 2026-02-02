package org.opendevstack.apiservice.externalservice.ocp.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.command.instance.GetAvailableInstancesCommand;
import org.opendevstack.apiservice.externalservice.ocp.command.instance.GetAvailableInstancesRequest;
import org.opendevstack.apiservice.externalservice.ocp.command.instance.HasInstanceCommand;
import org.opendevstack.apiservice.externalservice.ocp.command.instance.HasInstanceRequest;
import org.opendevstack.apiservice.externalservice.ocp.command.secret.GetSecretCommand;
import org.opendevstack.apiservice.externalservice.ocp.command.secret.GetSecretRequest;
import org.opendevstack.apiservice.externalservice.ocp.command.secret.GetSecretValueCommand;
import org.opendevstack.apiservice.externalservice.ocp.command.secret.GetSecretValueRequest;
import org.opendevstack.apiservice.externalservice.ocp.command.secret.SecretExistsCommand;
import org.opendevstack.apiservice.externalservice.ocp.command.secret.SecretExistsRequest;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for OpenShift Commands.
 * 
 * This test runs against a real OpenShift cluster configured in application-local.yaml.
 * It requires actual OpenShift connectivity and valid credentials.
 * 
 * To run these tests:
 * 1. Ensure application-local.yaml has valid OpenShift configuration
 * 2. Set environment variable: OPENSHIFT_INTEGRATION_TEST_ENABLED=true
 * 3. Set test secret details:
 *    - OPENSHIFT_TEST_INSTANCE (e.g., "dev" or "prod")
 *    - OPENSHIFT_TEST_SECRET_NAME (e.g., "test-secret")
 *    - OPENSHIFT_TEST_NAMESPACE (optional, uses default if not set)
 *    - OPENSHIFT_TEST_SECRET_KEY (e.g., "username")
 * 
 * Example:
 * export OPENSHIFT_INTEGRATION_TEST_ENABLED=true
 * export OPENSHIFT_TEST_INSTANCE=dev
 * export OPENSHIFT_TEST_SECRET_NAME=test-credentials
 * export OPENSHIFT_TEST_NAMESPACE=default
 * export OPENSHIFT_TEST_SECRET_KEY=password
 * 
 * Then run: mvn test -Dtest=OpenshiftCommandIntegrationTest
 */
@SpringBootTest(classes = OpenshiftIntegrationTestConfig.class)
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "OPENSHIFT_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class OpenshiftCommandIntegrationTest {

    @Autowired
    private OpenshiftService openshiftService;

    @Autowired
    private GetSecretCommand getSecretCommand;

    @Autowired
    private GetSecretValueCommand getSecretValueCommand;

    @Autowired
    private SecretExistsCommand secretExistsCommand;

    @Autowired
    private GetAvailableInstancesCommand getAvailableInstancesCommand;

    @Autowired
    private HasInstanceCommand hasInstanceCommand;

    private String testInstance;
    private String testSecretName;
    private String testNamespace;
    private String testSecretKey;

    @BeforeEach
    void setUp() {
        // Read test parameters from environment variables
        testInstance = System.getenv().getOrDefault("OPENSHIFT_TEST_INSTANCE", "dev");
        testSecretName = System.getenv().getOrDefault("OPENSHIFT_TEST_SECRET_NAME", "test-secret");
        testNamespace = System.getenv("OPENSHIFT_TEST_NAMESPACE");
        testSecretKey = System.getenv().getOrDefault("OPENSHIFT_TEST_SECRET_KEY", "username");

        log.info("Running integration tests against OpenShift instance: {}", testInstance);
        log.info("Test secret: {}", testSecretName);
        if (testNamespace != null) {
            log.info("Test namespace: {}", testNamespace);
        }
        log.info("Test secret key: {}", testSecretKey);
    }

    @Test
    void testGetAvailableInstances() {
        // Act
        Set<String> instances = openshiftService.getAvailableInstances();

        // Assert
        assertNotNull(instances, "Available instances should not be null");
        assertFalse(instances.isEmpty(), "Available instances should not be empty");
        assertTrue(instances.contains(testInstance), "Test instance should be available");
        log.info("Available OpenShift instances: {}", instances);
    }

    @Test
    void testHasInstance() {
        // Act
        boolean hasInstance = openshiftService.hasInstance(testInstance);

        // Assert
        assertTrue(hasInstance, "Test instance '" + testInstance + "' should be configured");
    }

    @Test
    void testHasInstance_NonExistent() {
        // Act
        boolean hasInstance = openshiftService.hasInstance("nonexistent-instance");

        // Assert
        assertFalse(hasInstance, "Non-existent instance should return false");
    }

    @Test
    void testIsHealthy() {
        // Act
        boolean isHealthy = openshiftService.isHealthy(testInstance);

        // Assert
        assertTrue(isHealthy, "Test instance should be healthy");
        log.info("Instance health check passed: {}", testInstance);
    }

    // ========================================================================
    // Instance Command Integration Tests
    // ========================================================================

    @Test
    void testGetAvailableInstancesCommand() throws ExternalServiceException {
        // Arrange
        GetAvailableInstancesRequest request = GetAvailableInstancesRequest.builder().build();

        // Act
        Set<String> instances = getAvailableInstancesCommand.execute(request);

        // Assert
        assertNotNull(instances, "Available instances should not be null");
        assertFalse(instances.isEmpty(), "Available instances should not be empty");
        assertTrue(instances.contains(testInstance), "Test instance should be available");
        log.info("GetAvailableInstancesCommand returned {} instances: {}", instances.size(), instances);
    }

    @Test
    void testHasInstanceCommand_ExistentInstance() throws ExternalServiceException {
        // Arrange
        HasInstanceRequest request = HasInstanceRequest.builder()
                .instanceName(testInstance)
                .build();

        // Act
        Boolean hasInstance = hasInstanceCommand.execute(request);

        // Assert
        assertNotNull(hasInstance, "Result should not be null");
        assertTrue(hasInstance, "Test instance '" + testInstance + "' should exist");
        log.info("HasInstanceCommand: instance '{}' exists: {}", testInstance, hasInstance);
    }

    @Test
    void testHasInstanceCommand_NonExistentInstance() throws ExternalServiceException {
        // Arrange
        HasInstanceRequest request = HasInstanceRequest.builder()
                .instanceName("nonexistent-instance-xyz-12345")
                .build();

        // Act
        Boolean hasInstance = hasInstanceCommand.execute(request);

        // Assert
        assertNotNull(hasInstance, "Result should not be null");
        assertFalse(hasInstance, "Non-existent instance should return false");
        log.info("HasInstanceCommand: non-existent instance check returned: {}", hasInstance);
    }

    // ========================================================================
    // Secret Command Integration Tests
    // ========================================================================

    @Test
    void testSecretExistsCommand_WithDefaultNamespace() throws ExternalServiceException {
        // Verify connection is working first
        assertTrue(openshiftService.validateConnection(testInstance),
                "Cannot connect to OpenShift instance '" + testInstance + "'. Check configuration and connectivity.");

        // Arrange
        SecretExistsRequest request = SecretExistsRequest.builder()
                .instanceName(testInstance)
                .secretName(testSecretName)
                .build();

        // Act
        Boolean exists = secretExistsCommand.execute(request);

        // Assert
        assertNotNull(exists, "Result should not be null");
        assertTrue(exists, "Secret '" + testSecretName + "' should exist in default namespace");
    }

    @Test
    void testSecretExistsCommand_WithNamespace() throws ExternalServiceException {
        // Verify connection is working first
        assertTrue(openshiftService.validateConnection(testInstance),
                "Cannot connect to OpenShift instance '" + testInstance + "'. Check configuration and connectivity.");

        // Arrange
        SecretExistsRequest request = SecretExistsRequest.builder()
                .instanceName(testInstance)
                .secretName(testSecretName)
                .namespace(testNamespace)
                .build();

        // Act
        Boolean exists = secretExistsCommand.execute(request);

        // Assert
        assertNotNull(exists, "Result should not be null");
        assertTrue(exists, "Secret '" + testSecretName + "' should exist in namespace '" + testNamespace + "'");
    }

    @Test
    void testGetSecretCommand_WithDefaultNamespace() throws ExternalServiceException {
        // Verify connection is working
        assertTrue(openshiftService.validateConnection(testInstance),
                "Cannot connect to OpenShift instance '" + testInstance + "'. Check configuration and connectivity.");

        // Arrange
        GetSecretRequest request = GetSecretRequest.builder()
                .instanceName(testInstance)
                .secretName(testSecretName)
                .build();

        // Act - this will throw ExternalServiceException if secret doesn't exist or connection fails
        Map<String, String> secret = getSecretCommand.execute(request);

        // Assert
        assertNotNull(secret, "Secret should not be null");
        assertFalse(secret.isEmpty(), "Secret should not be empty");
        log.info("Successfully retrieved secret '{}' with {} keys", testSecretName, secret.size());
    }

    @Test
    void testGetSecretCommand_NonExistentSecret() {
        // Arrange
        String nonExistentSecret = "nonexistent-secret-xyz-12345";
        GetSecretRequest request = GetSecretRequest.builder()
                .instanceName(testInstance)
                .secretName(nonExistentSecret)
                .build();

        // Act & Assert
        ExternalServiceException exception = assertThrows(ExternalServiceException.class, () ->
                getSecretCommand.execute(request)
        );

        assertTrue(
                exception.getMessage().contains("Failed to retrieve secret"),
                "Exception should indicate failure to retrieve secret"
        );
        log.info("Expected exception for non-existent secret: {}", exception.getMessage());
    }

    @Test
    void testGetSecretValueCommand_NonExistentKey() throws ExternalServiceException {
        // Verify connection is working
        assertTrue(openshiftService.validateConnection(testInstance),
                "Cannot connect to OpenShift instance '" + testInstance + "'. Check configuration and connectivity.");

        // Arrange
        String nonExistentKey = "nonexistent-key-xyz-12345";
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .instanceName(testInstance)
                .secretName(testSecretName)
                .key(nonExistentKey)
                .build();

        // Act & Assert
        ExternalServiceException exception = assertThrows(ExternalServiceException.class, () ->
                getSecretValueCommand.execute(request)
        );

        assertTrue(
                exception.getMessage().contains("Failed to retrieve secret value"),
                "Exception should indicate failure to retrieve secret value"
        );
        log.info("Expected exception for non-existent key: {}", exception.getMessage());
    }

    @Test
    void testGetSecretValueCommand_ExistentKey()  throws ExternalServiceException{
        // Verify connection is working
        assertTrue(openshiftService.validateConnection(testInstance),
                "Cannot connect to OpenShift instance '" + testInstance + "'. Check configuration and connectivity.");

        // Arrange
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .instanceName(testInstance)
                .secretName(testSecretName)
                .key(testSecretKey)
                .build();

        // Act
        String secretValue = getSecretValueCommand.execute(request);

        // Assert
        assertNotNull(secretValue, "Secret value should not be null");
        assertFalse(secretValue.isEmpty(), "Secret value should not be empty");
        log.info("Successfully retrieved secret value for key '{}' from secret '{}'", testSecretKey, testSecretName);
    }

    @Test
    void testInvalidInstance() {
        // Arrange
        String invalidInstance = "invalid-instance";
        GetSecretRequest request = GetSecretRequest.builder()
                .instanceName(invalidInstance)
                .secretName(testSecretName)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                getSecretCommand.validateRequest(request)
        );

        assertTrue(
                exception.getMessage().contains("does not exist"),
                "Exception should indicate instance does not exist"
        );
        log.info("Expected exception for invalid instance: {}", exception.getMessage());
    }
}
