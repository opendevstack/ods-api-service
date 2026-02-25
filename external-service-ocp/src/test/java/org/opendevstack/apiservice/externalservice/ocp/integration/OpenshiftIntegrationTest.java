package org.opendevstack.apiservice.externalservice.ocp.integration;

import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OpenShift Service
 * 
 * This test runs against a real OpenShift cluster configured in application-local.yaml.
 * It requires actual OpenShift connectivity and valid credentials.
 * 
 * To run these tests:
 * 1. Ensure application-local.yaml has valid OpenShift configuration
 * 2. Set environment variable: OPENSHIFT_INTEGRATION_TEST_ENABLED=true
 * 3. Set test cluster details:
 *    - OPENSHIFT_TEST_CLUSTER_API_URL (e.g., "https://api.example.ocp.cloud.com:6443")
 *    - OPENSHIFT_TEST_CLUSTER_TOKEN (e.g., "sha256~exampletoken1234567890abcdef")
 *    - OPENSHIFT_TEST_DEFAULT_NAMESPACE (e.g., "example-namespace")
 *    - OPENSHIFT_TEST_INSTANCE (e.g., "cluster-1" or "cluster-2")
 *    - OPENSHIFT_TEST_SECRET_NAME (e.g., "example-secret")
 *    - OPENSHIFT_TEST_PROJECT_NAME (e.g., "example-project")
 * 
 * Example:
 * export OPENSHIFT_INTEGRATION_TEST_ENABLED=true
 * export OPENSHIFT_TEST_CLUSTER_API_URL="https://api.example.ocp.cloud.com:6443"
 * export OPENSHIFT_TEST_CLUSTER_TOKEN="sha256~exampletoken1234567890abcdef"
 * export OPENSHIFT_TEST_DEFAULT_NAMESPACE=example-namespace
 * export OPENSHIFT_TEST_INSTANCE=cluster-1
 * export OPENSHIFT_TEST_SECRET_NAME=example-secret
 * export OPENSHIFT_TEST_PROJECT_NAME=example-project
 * 
 * Then run: mvn test -Dtest=OpenshiftIntegrationTest
 */
@SpringBootTest(classes = OpenshiftIntegrationTestConfig.class)
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "OPENSHIFT_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class OpenshiftIntegrationTest {


    @Autowired
    private OpenshiftService openshiftService;

    private String testInstance;
    private String testNamespace;
    private String testSecretName;
    private String testProjectName;

    @BeforeEach
    void setUp() {
        // Read test parameters from environment variables
        testInstance = System.getenv().getOrDefault("OPENSHIFT_TEST_INSTANCE", "cluster-a");
        testNamespace = System.getenv().getOrDefault("OPENSHIFT_TEST_DEFAULT_NAMESPACE", "example-project-cd");
        testSecretName = System.getenv().getOrDefault("OPENSHIFT_TEST_SECRET_NAME", "webhook-proxy");
        testProjectName = System.getenv().getOrDefault("OPENSHIFT_TEST_PROJECT_NAME", "example-project");

        log.info("Running integration tests against OpenShift instance: {}", testInstance);
        log.info("Test namespace: {}", testNamespace);
        log.info("Test secret: {}", testSecretName);
        log.info("Test project: {}", testProjectName);
    }

    @Test
    void testGetAvailableInstances() {
        // Act
        var instances = openshiftService.getAvailableInstances();

        // Assert
        assertNotNull(instances, "Available instances should not be null");
        assertFalse(instances.isEmpty(), "Should have at least one configured instance");
        
        log.info("Available instances: {}", instances);
        instances.forEach(instance -> {
            log.info("  - {}", instance);
        });
    }

    @Test
    void testHasInstance() {
        // Act
        boolean hasInstance = openshiftService.hasInstance(testInstance);

        // Assert
        assertTrue(hasInstance, "Test instance '" + testInstance + "' should be configured");
        log.info("✓ Instance '{}' is configured", testInstance);
    }

    @Test
    void testHasInstance_NonExistent() {
        // Act
        boolean hasInstance = openshiftService.hasInstance("nonexistent-instance-xyz");

        // Assert
        assertFalse(hasInstance, "Non-existent instance should return false");
        log.info("✓ Non-existent instance correctly returned false");
    }

    @Test
    void testHealthCheck() {
        // Act
        boolean isHealthy = openshiftService.isHealthy();

        // Assert
        assertTrue(isHealthy, "OpenShift service should be healthy");
        log.info("✓ OpenShift service is healthy");
    }

    // -------------------------------------------------------------------------
    // Test secret existence.
    // -------------------------------------------------------------------------

    @Test
    void testSecretExists_ExistingSecret() throws OpenshiftException {
        // Act
        boolean exists = openshiftService.secretExists(testInstance, testSecretName, testNamespace);

        // Assert
        // Note: This may be true or false depending on the test environment
        // We just verify the method executes without exception
        log.info("Secret '{}' exists in namespace '{}': {}", testSecretName, testNamespace, exists);
    }

    @Test
    void testSecretExists_NonExistentSecret() throws OpenshiftException {
        // Arrange
        String nonExistentSecret = "nonexistent-secret-xyz-12345";

        // Act
        boolean exists = openshiftService.secretExists(testInstance, nonExistentSecret, testNamespace);

        // Assert
        assertFalse(exists, "Non-existent secret should return false");
        log.info("✓ Verified that secret '{}' does not exist", nonExistentSecret);
    }

    @Test
    void testSecretExists_NamespaceNotProvided() throws OpenshiftException {
        // Act
        boolean exists = openshiftService.secretExists(testInstance, testSecretName);

        // Assert
        // Note: This may be true or false depending on the default namespace configuration
        // We just verify the method executes without exception
        log.info("Secret '{}' exists in default namespace: {}", testSecretName, exists);
    }

        @Test
    void testSecretExists_WithConsistentBehavior() throws OpenshiftException {
        // Act - Check with and without namespace
        boolean existsWithNamespace = openshiftService.secretExists(testInstance, testSecretName, testNamespace);
        boolean existsWithoutNamespace = openshiftService.secretExists(testInstance, testSecretName);

        // Assert - Both should return consistent results (either both true or both false)
        // Note: They may differ if the default namespace is different from the test namespace
        log.info("Secret '{}' exists with namespace: {}", testSecretName, existsWithNamespace);
        log.info("Secret '{}' exists without namespace: {}", testSecretName, existsWithoutNamespace);
    }

    // -------------------------------------------------------------------------
    // Test secret retrieval.
    // -------------------------------------------------------------------------

    @Test
    void testGetSecret_Success() throws OpenshiftException {
        // Act
        Map<String, String> secret = openshiftService.getSecret(testInstance, testSecretName, testNamespace);

        // Assert
        assertNotNull(secret, "Secret should not be null");
        log.info("✓ Successfully retrieved secret with {} keys", secret.size());
        
        // Log secret keys (not values for security)
        log.info("Secret keys: {}", secret.keySet());
    }

    @Test
    void testGetSecret_NonExistentSecret() {
        // Arrange
        String nonExistentSecret = "nonexistent-secret-xyz-12345";

        // Act & Assert
        OpenshiftException exception = assertThrows(OpenshiftException.class, () ->
            openshiftService.getSecret(testInstance, nonExistentSecret, testNamespace)
        );

        assertTrue(
            exception.getMessage().contains("Failed to retrieve") || 
            exception.getMessage().contains("not found"),
            "Exception should indicate secret not found or retrieval failed"
        );
        log.info("Expected exception for non-existent secret: {}", exception.getMessage());
    }

    // -------------------------------------------------------------------------
    // Test secret value retrieval.
    // -------------------------------------------------------------------------

    @Test
    void testGetSecretValue_Success() throws OpenshiftException {
        // First get the secret to find available keys
        Map<String, String> secret = openshiftService.getSecret(testInstance, testSecretName, testNamespace);

        if (!secret.isEmpty()) {
            // Use the first available key
            String key = secret.keySet().iterator().next();

            // Act
            String value = openshiftService.getSecretValue(testInstance, testSecretName, key, testNamespace);

            // Assert
            assertNotNull(value, "Secret value should not be null");
            log.info("✓ Successfully retrieved value for key '{}'", key);
        } else {
            log.warn("Secret is empty, skipping value retrieval test");
        }
    }

    @Test
    void testGetSecretValue_NonExistentKey() {
        // Arrange
        String nonExistentKey = "nonexistent-key-xyz";

        // Act & Assert
        OpenshiftException exception = assertThrows(OpenshiftException.class, () ->
            openshiftService.getSecretValue(testInstance, testSecretName, nonExistentKey, testNamespace)
        );

        assertTrue(
            exception.getMessage().contains("not found") || 
            exception.getMessage().contains("Failed"),
            "Exception should indicate key not found"
        );
        log.info("Expected exception for non-existent key: {}", exception.getMessage());
    }

    // -------------------------------------------------------------------------
    // Test project existence.
    // -------------------------------------------------------------------------


    @Test
    void testProjectExists_ExistingProject() throws OpenshiftException {
        // Act
        boolean exists = openshiftService.projectExists(testInstance, testProjectName);

        // Assert
        assertTrue(exists, "Project '" + testProjectName + "' should exist in instance '" + testInstance + "'");
        log.info("✓ Project '{}' exists in instance '{}'", testProjectName, testInstance);
    }

    @Test
    void testProjectExists_NonExistentProject() throws OpenshiftException {
        // Arrange
        String nonExistentProject = "nonexistent-project-xyz-12345";

        // Act
        boolean exists = openshiftService.projectExists(testInstance, nonExistentProject);

        // Assert
        assertFalse(exists, "Non-existent project should return false");
        log.info("✓ Verified that project '{}' does not exist", nonExistentProject);
    }

}
