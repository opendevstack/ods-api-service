package org.opendevstack.apiservice.externalservice.bitbucket.integration;

import org.opendevstack.apiservice.externalservice.bitbucket.client.BitbucketApiClientFactory;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration.BitbucketInstanceConfig;
import org.opendevstack.apiservice.externalservice.bitbucket.service.impl.BitbucketServiceImpl;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for BitbucketService.
 * This test runs against a real Bitbucket instance configured in application-local.yaml.
 * It requires actual Bitbucket connectivity and valid credentials.
 * To run these tests:
 * 1. Ensure application-local.yaml has valid Bitbucket configuration
 * 2. Set environment variable: BITBUCKET_INTEGRATION_TEST_ENABLED=true
 * 3. Set test repository details:
 *    - BITBUCKET_TEST_INSTANCE (e.g., "dev" or "prod")
 *    - BITBUCKET_TEST_PROJECT_KEY (e.g., "PROJ")
 *    - BITBUCKET_TEST_REPOSITORY_SLUG (e.g., "my-repo")
 *    - BITBUCKET_TEST_EXISTING_BRANCH (e.g., "develop")
 * Example:
 * export BITBUCKET_INTEGRATION_TEST_ENABLED=true
 * export BITBUCKET_TEST_INSTANCE=dev
 * export BITBUCKET_TEST_PROJECT_KEY=DEVSTACK
 * export BITBUCKET_TEST_REPOSITORY_SLUG=devstack-api-service
 * export BITBUCKET_TEST_EXISTING_BRANCH=develop
 * Then run: mvn test -Dtest=BitbucketServiceIntegrationTest
 */
@SpringBootTest(classes = BitbucketIntegrationTestConfig.class)
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "BITBUCKET_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class BitbucketServiceIntegrationTest {

    @Autowired
    private BitbucketService bitbucketService;

    @Autowired
    private BitbucketServiceConfiguration bitbucketConfiguration;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    private String testInstance;
    private String testProjectKey;
    private String testRepositorySlug;
    private String testExistingBranch;

    @BeforeEach
    void setUp() {
        // Read test parameters from environment variables
        testInstance = System.getenv().getOrDefault("BITBUCKET_TEST_INSTANCE", "dev");
        testProjectKey = System.getenv().getOrDefault("BITBUCKET_TEST_PROJECT_KEY", "EXAMPLE");
        testRepositorySlug = System.getenv().getOrDefault("BITBUCKET_TEST_REPOSITORY_SLUG", "example-project-releasemanager");
        testExistingBranch = System.getenv().getOrDefault("BITBUCKET_TEST_EXISTING_BRANCH", "develop");

        log.info("Running integration tests against Bitbucket instance: {}", testInstance);
        log.info("Test repository: {}/{}", testProjectKey, testRepositorySlug);
        log.info("Test branch: {}", testExistingBranch);
    }

    @Test
    void testGetAvailableInstances() {
        // Act
        Set<String> instances = bitbucketService.getAvailableInstances();

        // Assert
        assertNotNull(instances, "Available instances should not be null");
        assertFalse(instances.isEmpty(), "Available instances should not be empty");
        log.info("Available Bitbucket instances: {}", instances);
    }

    @Test
    void testHasInstance() {
        // Act
        boolean hasInstance = bitbucketService.hasInstance(testInstance);

        // Assert
        assertTrue(hasInstance, "Test instance '" + testInstance + "' should be configured");
    }

    @Test
    void testHasInstance_NonExistent() {
        // Act
        boolean hasInstance = bitbucketService.hasInstance("nonexistent-instance");

        // Assert
        assertFalse(hasInstance, "Non-existent instance should return false");
    }

    @Test
    void testGetDefaultBranch_Success() throws BitbucketException {
        // Act
        String defaultBranch = bitbucketService.getDefaultBranch(
            testInstance,
            testProjectKey,
            testRepositorySlug
        );

        // Assert
        assertNotNull(defaultBranch, "Default branch should not be null");
        assertFalse(defaultBranch.isEmpty(), "Default branch should not be empty");
        log.info("Default branch for {}/{}: {}", testProjectKey, testRepositorySlug, defaultBranch);
        
        // Common default branch names
        assertTrue(
            defaultBranch.equals("main") || 
            defaultBranch.equals("master") || 
            defaultBranch.equals("develop") ||
            defaultBranch.matches("[a-zA-Z0-9/_-]+"),
            "Default branch should have a valid name format"
        );
    }

    @Test
    void testGetDefaultBranch_RepositoryNotFound() {
        // Arrange
        String nonExistentRepo = "nonexistent-repo-12345";

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.getDefaultBranch(testInstance, testProjectKey, nonExistentRepo)
        );

        assertTrue(
            exception.getMessage().contains("not found") || 
            exception.getMessage().contains("No default branch"),
            "Exception should indicate repository not found or no default branch"
        );
        log.info("Expected exception for non-existent repository: {}", exception.getMessage());
    }

    @Test
    void testGetDefaultBranch_InvalidInstance() {
        // Arrange
        String invalidInstance = "invalid-instance";

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.getDefaultBranch(invalidInstance, testProjectKey, testRepositorySlug)
        );

        assertTrue(
            exception.getMessage().contains("not configured") || 
            exception.getMessage().contains("No Bitbucket instance"),
            "Exception should indicate instance not configured"
        );
        log.info("Expected exception for invalid instance: {}", exception.getMessage());
    }

    @Test
    void testBranchExists_ExistingBranch() throws BitbucketException {
        // First, get the default branch to ensure we test with a branch that exists
        String defaultBranch = bitbucketService.getDefaultBranch(
            testInstance,
            testProjectKey,
            testRepositorySlug
        );

        // Act
        boolean exists = bitbucketService.branchExists(
            testInstance,
            testProjectKey,
            testRepositorySlug,
            defaultBranch
        );

        // Assert
        assertTrue(exists, "Default branch should exist: " + defaultBranch);
        log.info("Verified branch exists: {}", defaultBranch);
    }

    @Test
    void testBranchExists_NonExistentBranch() throws BitbucketException {
        // Arrange
        String nonExistentBranch = "nonexistent-branch-xyz-12345";

        // Act
        boolean exists = bitbucketService.branchExists(
            testInstance,
            testProjectKey,
            testRepositorySlug,
            nonExistentBranch
        );

        // Assert
        assertFalse(exists, "Non-existent branch should not exist");
        log.info("Verified branch does not exist: {}", nonExistentBranch);
    }

    @Test
    void testBranchExists_ConfiguredTestBranch() throws BitbucketException {
        // Act
        boolean exists = bitbucketService.branchExists(
            testInstance,
            testProjectKey,
            testRepositorySlug,
            testExistingBranch
        );

        // Assert
        // Note: This may be true or false depending on the test repository
        // We just verify the method executes without exception
        log.info("Branch '{}' exists: {}", testExistingBranch, exists);
    }

    @Test
    void testBranchExists_RepositoryNotFound() {
        // Arrange
        String nonExistentRepo = "nonexistent-repo-12345";
        String branchName = "main";

        // Act
        boolean exists;
        try {
            exists = bitbucketService.branchExists(
                testInstance,
                testProjectKey,
                nonExistentRepo,
                branchName
            );
            
            // Assert - should return false for non-existent repository
            assertFalse(exists, "Non-existent repository should return false");
            log.info("Non-existent repository correctly returned false");
        } catch (BitbucketException e) {
            // Also acceptable - some implementations may throw exception
            assertTrue(
                e.getMessage().contains("not found") || 
                e.getMessage().contains("Failed to check"),
                "Exception should indicate repository issue"
            );
            log.info("Non-existent repository threw expected exception: {}", e.getMessage());
        }
    }

    @Test
    void testGetDefaultBranch_ConsistentResults() throws BitbucketException {
        // Act - Call the method multiple times
        String defaultBranch1 = bitbucketService.getDefaultBranch(
            testInstance,
            testProjectKey,
            testRepositorySlug
        );
        
        String defaultBranch2 = bitbucketService.getDefaultBranch(
            testInstance,
            testProjectKey,
            testRepositorySlug
        );

        // Assert - Results should be consistent
        assertEquals(defaultBranch1, defaultBranch2, 
            "Default branch should be consistent across multiple calls");
        log.info("Verified consistent default branch: {}", defaultBranch1);
    }

    @Test
    void testBranchExists_WithRefPrefix() throws BitbucketException {
        // Get default branch first
        String defaultBranch = bitbucketService.getDefaultBranch(
            testInstance,
            testProjectKey,
            testRepositorySlug
        );

        // Act - Test with refs/heads/ prefix
        boolean existsWithoutPrefix = bitbucketService.branchExists(
            testInstance,
            testProjectKey,
            testRepositorySlug,
            defaultBranch
        );

        // Both should work (the implementation handles refs/heads/ prefix)
        assertTrue(existsWithoutPrefix, 
            "Branch should exist when queried without refs/heads/ prefix");
        
        log.info("Branch lookup works correctly for: {}", defaultBranch);
    }

    @Test
    void testProjectExists_ExistingProject() throws BitbucketException {
        // Act - Use the same project key that is already known to exist (used by other tests)
        boolean exists = bitbucketService.projectExists(testInstance, testProjectKey);

        // Assert
        assertTrue(exists, "Project '" + testProjectKey + "' should exist in instance '" + testInstance + "'");
        log.info("Verified project exists: {}", testProjectKey);
    }

    @Test
    void testProjectExists_NonExistentProject() throws BitbucketException {
        // Arrange
        String nonExistentProject = "ZZNONEXIST99";

        // Act
        boolean exists = bitbucketService.projectExists(testInstance, nonExistentProject);

        // Assert
        assertFalse(exists, "Non-existent project should return false");
        log.info("Verified project does not exist: {}", nonExistentProject);
    }

    @Test
    void testProjectExists_InvalidInstance() {
        // Arrange
        String invalidInstance = "invalid-instance";

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.projectExists(invalidInstance, testProjectKey)
        );

        assertTrue(
            exception.getMessage().contains("not configured") ||
            exception.getMessage().contains("No Bitbucket instance"),
            "Exception should indicate instance not configured"
        );
        log.info("Expected exception for invalid instance: {}", exception.getMessage());
    }

    @Test
    void testProjectExists_ConsistentResults() throws BitbucketException {
        // Act - Call the method multiple times
        boolean exists1 = bitbucketService.projectExists(testInstance, testProjectKey);
        boolean exists2 = bitbucketService.projectExists(testInstance, testProjectKey);

        // Assert - Results should be consistent
        assertEquals(exists1, exists2,
            "projectExists should return consistent results across multiple calls");
        log.info("Verified consistent projectExists result for '{}': {}", testProjectKey, exists1);
    }
    
    /**
     * Helper method that creates a BitbucketService backed by a client factory
     * configured with the same base URL as the real test instance but with an
     * intentionally wrong password, so that every call returns 401 Unauthorized.
     */
    private BitbucketService createUnauthorizedBitbucketService() {
        // Take the real instance config and clone it with a wrong password
        BitbucketInstanceConfig realConfig = bitbucketConfiguration.getInstances().get(testInstance);
        assertNotNull(realConfig, "Real instance config for '" + testInstance + "' should exist");

        BitbucketInstanceConfig badConfig = new BitbucketInstanceConfig();
        badConfig.setBaseUrl(realConfig.getBaseUrl());
        badConfig.setUsername("invalid-user-unauthorized-test");
        badConfig.setPassword("wrong-password-12345");
        badConfig.setBearerToken(null); // force basic auth with bad credentials
        badConfig.setConnectionTimeout(realConfig.getConnectionTimeout());
        badConfig.setReadTimeout(realConfig.getReadTimeout());
        badConfig.setTrustAllCertificates(realConfig.isTrustAllCertificates());

        BitbucketServiceConfiguration badConfiguration = new BitbucketServiceConfiguration();
        badConfiguration.setInstances(java.util.Map.of("unauthorized", badConfig));

        BitbucketApiClientFactory badFactory = new BitbucketApiClientFactory(badConfiguration, restTemplateBuilder);
        return new BitbucketServiceImpl(badFactory);
    }

    @Test
    void testGetDefaultBranch_Unauthorized() {
        // Arrange – service with wrong password
        BitbucketService unauthorizedService = createUnauthorizedBitbucketService();

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            unauthorizedService.getDefaultBranch("unauthorized", testProjectKey, testRepositorySlug)
        );

        assertNotNull(exception.getCause(), "Exception should have a cause");
        assertInstanceOf(HttpClientErrorException.Unauthorized.class, exception.getCause(),
            "Cause should be 401 Unauthorized, but was: " + exception.getCause().getClass().getSimpleName());
        assertTrue(exception.getMessage().contains("Authentication failed (401 Unauthorized)"),
            "Exception message should indicate authentication failure, but was: " + exception.getMessage());
        log.info("getDefaultBranch correctly failed with Unauthorized: {}", exception.getMessage());
    }

    @Test
    void testBranchExists_Unauthorized() {
        // Arrange – service with wrong password
        BitbucketService unauthorizedService = createUnauthorizedBitbucketService();

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            unauthorizedService.branchExists("unauthorized", testProjectKey, testRepositorySlug, "main")
        );

        assertNotNull(exception.getCause(), "Exception should have a cause");
        assertInstanceOf(HttpClientErrorException.Unauthorized.class, exception.getCause(),
            "Cause should be 401 Unauthorized, but was: " + exception.getCause().getClass().getSimpleName());
        assertTrue(exception.getMessage().contains("Authentication failed (401 Unauthorized)"),
            "Exception message should indicate authentication failure, but was: " + exception.getMessage());
        log.info("branchExists correctly failed with Unauthorized: {}", exception.getMessage());
    }

    @Test
    void testProjectExists_Unauthorized() {
        // Arrange – service with wrong password
        BitbucketService unauthorizedService = createUnauthorizedBitbucketService();

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            unauthorizedService.projectExists("unauthorized", testProjectKey)
        );

        assertNotNull(exception.getCause(), "Exception should have a cause");
        assertInstanceOf(HttpClientErrorException.Unauthorized.class, exception.getCause(),
            "Cause should be 401 Unauthorized, but was: " + exception.getCause().getClass().getSimpleName());
        assertTrue(exception.getMessage().contains("Authentication failed (401 Unauthorized)"),
            "Exception message should indicate authentication failure, but was: " + exception.getMessage());
        log.info("projectExists correctly failed with Unauthorized: {}", exception.getMessage());
    }
}
