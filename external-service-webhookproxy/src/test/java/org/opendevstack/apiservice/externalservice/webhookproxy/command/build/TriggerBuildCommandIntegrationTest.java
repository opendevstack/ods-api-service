package org.opendevstack.apiservice.externalservice.webhookproxy.command.build;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.WebhookProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TriggerBuildCommand.
 * These tests verify the command pattern integration with the webhook proxy service.
 * 
 * Prerequisites:
 * 1. Set environment variables:
 *    - WEBHOOK_PROXY_TRIGGER_SECRET (required - the trigger secret for authentication)
 *    - WEBHOOK_PROXY_CLUSTER_NAME (default: cluster-a)
 *    - WEBHOOK_PROXY_PROJECT_KEY (default: example-project)
 *    - WEBHOOK_PROXY_TEST_BRANCH (default: master)
 *    - WEBHOOK_PROXY_TEST_REPOSITORY (default: example-project-releasemanager)
 * 
 * 2. Ensure the webhook proxy is accessible at:
 *    https://webhook-proxy-{projectKey}-cd.{clusterBase}
 * 
 * To run these tests:
 * mvn test -Dtest=TriggerBuildCommandIntegrationTest -Dspring.profiles.active=local
 */
@SpringBootTest(classes = TriggerBuildCommandIntegrationTest.TestConfiguration.class)
@ActiveProfiles("local")
@Slf4j
@Disabled("Integration tests require actual webhook proxy access - enable manually when needed")
class TriggerBuildCommandIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "org.opendevstack.apiservice.externalservice.webhookproxy")
    static class TestConfiguration {
    }

    @Autowired
    private WebhookProxyService webhookProxyService;

    private TriggerBuildCommand command;

    // Test configuration from environment variables
    private String clusterName;
    private String projectKey;
    private String triggerSecret;
    private String testBranch;
    private String testRepository;

    @BeforeEach
    void setUp() {
        command = new TriggerBuildCommand(webhookProxyService);
        
        clusterName = System.getenv().getOrDefault("WEBHOOK_PROXY_CLUSTER_NAME", "cluster-a");
        projectKey = System.getenv().getOrDefault("WEBHOOK_PROXY_PROJECT_KEY", "example-project");
        triggerSecret = System.getenv("WEBHOOK_PROXY_TRIGGER_SECRET");
        testBranch = System.getenv().getOrDefault("WEBHOOK_PROXY_TEST_BRANCH", "master");
        testRepository = System.getenv().getOrDefault("WEBHOOK_PROXY_TEST_REPOSITORY", "example-project-releasemanager");

        log.info("=".repeat(80));
        log.info("TriggerBuildCommand Integration Test Configuration:");
        log.info("  Cluster Name: {}", clusterName);
        log.info("  Project Key: {}", projectKey);
        log.info("  Test Branch: {}", testBranch);
        log.info("  Test Repository: {}", testRepository);
        log.info("  Trigger Secret: {}", triggerSecret != null ? "***SET***" : "NOT SET");
        log.info("=".repeat(80));
    }

    // ========== Command Metadata Tests ==========

    @Test
    void getCommandName_shouldReturnCorrectName() {
        assertEquals("trigger-build", command.getCommandName());
        log.info("✓ Command name is correct: {}", command.getCommandName());
    }

    @Test
    void getServiceName_shouldReturnCorrectName() {
        assertEquals("webhookproxy", command.getServiceName());
        log.info("✓ Service name is correct: {}", command.getServiceName());
    }

    // ========== Validation Tests (No Build Triggered) ==========

    @Test
    void validateRequest_shouldThrowException_whenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> command.validateRequest(null));
        log.info("✓ Validation correctly failed for null request");
    }

    @Test
    void validateRequest_shouldThrowException_whenClusterNameIsNull() {
        TriggerBuildRequest request = createValidRequest();
        request.setClusterName(null);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> command.validateRequest(request));
        assertEquals("Cluster name cannot be null or empty", ex.getMessage());
        log.info("✓ Validation correctly failed for null cluster name");
    }

    @Test
    void validateRequest_shouldThrowException_whenProjectKeyIsEmpty() {
        TriggerBuildRequest request = createValidRequest();
        request.setProjectKey("  ");
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> command.validateRequest(request));
        assertEquals("Project key cannot be null or empty", ex.getMessage());
        log.info("✓ Validation correctly failed for empty project key");
    }

    @Test
    void validateRequest_shouldThrowException_whenBranchIsNull() {
        TriggerBuildRequest request = createValidRequest();
        request.setBranch(null);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> command.validateRequest(request));
        assertEquals("Branch cannot be null or empty", ex.getMessage());
        log.info("✓ Validation correctly failed for null branch");
    }

    @Test
    void validateRequest_shouldThrowException_whenRepositoryIsNull() {
        TriggerBuildRequest request = createValidRequest();
        request.setRepository(null);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> command.validateRequest(request));
        assertEquals("Repository cannot be null or empty", ex.getMessage());
        log.info("✓ Validation correctly failed for null repository");
    }

    @Test
    void validateRequest_shouldThrowException_whenProjectIsNull() {
        TriggerBuildRequest request = createValidRequest();
        request.setProject(null);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> command.validateRequest(request));
        assertEquals("Project (Bitbucket project key) cannot be null or empty", ex.getMessage());
        log.info("✓ Validation correctly failed for null project");
    }

    @Test
    void validateRequest_shouldThrowException_whenTriggerSecretIsNull() {
        TriggerBuildRequest request = createValidRequest();
        request.setTriggerSecret(null);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> command.validateRequest(request));
        assertEquals("Trigger secret cannot be null or empty", ex.getMessage());
        log.info("✓ Validation correctly failed for null trigger secret");
    }

    @Test
    void validateRequest_shouldPass_whenAllFieldsAreValid() {
        TriggerBuildRequest request = createValidRequest();
        
        // Should not throw exception
        assertDoesNotThrow(() -> command.validateRequest(request));
        log.info("✓ Validation passed for valid request");
    }

    // ========== Execute Tests (Actual Build Triggered) ==========

    @Test
    @Disabled("Remove this annotation to trigger an actual build - CAUTION: This will trigger a real build!")
    void execute_shouldTriggerBuild_whenRequestIsValid() throws ExternalServiceException {
        log.info("TEST: Execute command to trigger a build");

        assertNotNull(triggerSecret, 
                "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");

        TriggerBuildRequest request = TriggerBuildRequest.builder()
                .clusterName(clusterName)
                .projectKey(projectKey)
                .branch(testBranch)
                .repository(testRepository)
                .project(projectKey)
                .triggerSecret(triggerSecret)
                .build();

        log.info("Executing TriggerBuildCommand:");
        log.info("  Cluster: {}", request.getClusterName());
        log.info("  Project Key: {}", request.getProjectKey());
        log.info("  Branch: {}", request.getBranch());
        log.info("  Repository: {}", request.getRepository());
        log.info("  Project (Bitbucket): {}", request.getProject());

        WebhookProxyBuildResponse response = command.execute(request);

        assertNotNull(response, "Response should not be null");
        log.info("✓ Received response");

        log.info("Response details:");
        log.info("  Status Code: {}", response.getStatusCode());
        log.info("  Success: {}", response.isSuccess());
        log.info("  Body: {}", response.getBody());

        if (!response.isSuccess()) {
            log.warn("  Error Message: {}", response.getErrorMessage());
        }

        assertTrue(response.isSuccess(), 
                "Build trigger should be successful. Error: " + response.getErrorMessage());
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300,
                "Status code should be 2xx");

        log.info("✓ Build triggered successfully via TriggerBuildCommand!");
    }

    @Test
    @Disabled("Remove this annotation to trigger a build with env vars - CAUTION: This will trigger a real build!")
    void execute_shouldTriggerBuildWithEnvironmentVariables() throws ExternalServiceException {
        log.info("TEST: Execute command to trigger a build with environment variables");

        assertNotNull(triggerSecret, 
                "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");

        TriggerBuildRequest request = TriggerBuildRequest.builder()
                .clusterName(clusterName)
                .projectKey(projectKey)
                .branch(testBranch)
                .repository(testRepository)
                .project(projectKey)
                .triggerSecret(triggerSecret)
                .build();

        // Add environment variables
        request.addEnv("TEST_MODE", "true");
        request.addEnv("INTEGRATION_TEST", "TriggerBuildCommand");
        request.addEnv("TRIGGERED_BY", "TriggerBuildCommandIntegrationTest");

        log.info("Executing TriggerBuildCommand with environment variables:");
        request.getEnv().forEach(env -> 
            log.info("  {}: {}", env.getName(), env.getValue()));

        WebhookProxyBuildResponse response = command.execute(request);

        assertNotNull(response);
        assertTrue(response.isSuccess(), 
                "Build trigger should be successful. Error: " + response.getErrorMessage());

        log.info("✓ Build with environment variables triggered successfully!");
        log.info("  Status: {}", response.getStatusCode());
    }

    @Test
    @Disabled("Remove this annotation to trigger a build with custom Jenkinsfile - CAUTION: This will trigger a real build!")
    void execute_shouldTriggerBuildWithCustomJenkinsfile() throws ExternalServiceException {
        log.info("TEST: Execute command to trigger a build with custom Jenkinsfile");

        assertNotNull(triggerSecret, 
                "WEBHOOK_PROXY_TRIGGER_SECRET environment variable must be set");

        String customJenkinsfilePath = "release-manager.Jenkinsfile";
        String componentName = "release-manager";

        TriggerBuildRequest request = TriggerBuildRequest.builder()
                .clusterName(clusterName)
                .projectKey(projectKey)
                .branch(testBranch)
                .repository(testRepository)
                .project(projectKey)
                .triggerSecret(triggerSecret)
                .jenkinsfilePath(customJenkinsfilePath)
                .component(componentName)
                .build();

        log.info("Executing TriggerBuildCommand with custom parameters:");
        log.info("  Jenkinsfile Path: {}", request.getJenkinsfilePath());
        log.info("  Component: {}", request.getComponent());

        WebhookProxyBuildResponse response = command.execute(request);

        assertNotNull(response);
        assertTrue(response.isSuccess(), 
                "Build trigger should be successful. Error: " + response.getErrorMessage());

        log.info("✓ Build with custom Jenkinsfile triggered successfully!");
    }

    @Test
    @Disabled("Remove this annotation to test authentication failure")
    void execute_shouldThrowException_whenTriggerSecretIsInvalid() {
        log.info("TEST: Execute command with invalid trigger secret");

        String invalidSecret = "invalid-secret-12345";

        TriggerBuildRequest request = TriggerBuildRequest.builder()
                .clusterName(clusterName)
                .projectKey(projectKey)
                .branch(testBranch)
                .repository(testRepository)
                .project(projectKey)
                .triggerSecret(invalidSecret)
                .build();

        log.info("Attempting to trigger build with invalid trigger secret");

        ExternalServiceException ex = assertThrows(ExternalServiceException.class, 
                () -> command.execute(request));

        assertEquals("webhookproxy", ex.getServiceName());
        assertEquals("trigger-build", ex.getOperation());
        log.info("✓ Command correctly threw exception for invalid trigger secret");
        log.info("  Error Code: {}", ex.getErrorCode());
        log.info("  Message: {}", ex.getMessage());
    }

    @Test
    @Disabled("Remove this annotation to test non-existent cluster")
    void execute_shouldThrowException_whenClusterDoesNotExist() {
        log.info("TEST: Execute command with non-existent cluster");

        TriggerBuildRequest request = TriggerBuildRequest.builder()
                .clusterName("non-existent-cluster")
                .projectKey(projectKey)
                .branch(testBranch)
                .repository(testRepository)
                .project(projectKey)
                .triggerSecret(triggerSecret != null ? triggerSecret : "dummy-secret")
                .build();

        log.info("Attempting to trigger build on non-existent cluster");

        ExternalServiceException ex = assertThrows(ExternalServiceException.class, 
                () -> command.execute(request));

        assertEquals("webhookproxy", ex.getServiceName());
        assertEquals("trigger-build", ex.getOperation());
        log.info("✓ Command correctly threw exception for non-existent cluster");
        log.info("  Error Code: {}", ex.getErrorCode());
        log.info("  Message: {}", ex.getMessage());
    }

    // ========== Helper Methods ==========

    private TriggerBuildRequest createValidRequest() {
        return TriggerBuildRequest.builder()
                .clusterName(clusterName)
                .projectKey(projectKey)
                .branch(testBranch)
                .repository(testRepository)
                .project(projectKey)
                .triggerSecret(triggerSecret != null ? triggerSecret : "test-secret")
                .build();
    }
}
