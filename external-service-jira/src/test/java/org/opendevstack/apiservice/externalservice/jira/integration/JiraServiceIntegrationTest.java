package org.opendevstack.apiservice.externalservice.jira.integration;

import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link JiraService}.
 *
 * <p>This test runs against a real Jira instance configured in {@code application-local.yaml}.
 * It requires actual Jira connectivity and valid credentials.
 *
 * <p>To run these tests:
 * <ol>
 *   <li>Ensure {@code application-local.yaml} has valid Jira configuration under
 *       {@code externalservices.jira.instances}.</li>
 *   <li>Set environment variable: {@code JIRA_INTEGRATION_TEST_ENABLED=true}</li>
 *   <li>Set test parameters via environment variables:
 *     <ul>
 *       <li>{@code JIRA_TEST_INSTANCE} – logical instance name (e.g. {@code dev})</li>
 *       <li>{@code JIRA_TEST_PROJECT_KEY} – an existing project key (e.g. {@code DEVSTACK})</li>
 *       <li>{@code JIRA_TEST_NONEXISTENT_PROJECT_KEY} – a key that should NOT exist (e.g. {@code ZZZNOPE})</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p>Example:
 * <pre>
 * export JIRA_INTEGRATION_TEST_ENABLED=true
 * export JIRA_TEST_INSTANCE=dev
 * export JIRA_TEST_PROJECT_KEY=DEVSTACK
 * export JIRA_TEST_NONEXISTENT_PROJECT_KEY=ZZZNOPE
 * mvn test -Dtest=JiraServiceIntegrationTest -pl external-service-jira
 * </pre>
 */
@SpringBootTest(classes = JiraIntegrationTestConfig.class)
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "JIRA_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class JiraServiceIntegrationTest {

    @Autowired
    private JiraService jiraService;

    private String testInstance;
    private String testProjectKey;
    private String testNonExistentProjectKey;

    @BeforeEach
    void setUp() {
        testInstance = System.getenv().getOrDefault("JIRA_TEST_INSTANCE", "dev");
        testProjectKey = System.getenv().getOrDefault("JIRA_TEST_PROJECT_KEY", "EXAMPLE");
        testNonExistentProjectKey = System.getenv().getOrDefault("JIRA_TEST_NONEXISTENT_PROJECT_KEY", "ZZZNOPE");

        log.info("Running integration tests against Jira instance: {}", testInstance);
        log.info("Test project key (existing): {}", testProjectKey);
        log.info("Test project key (nonexistent): {}", testNonExistentProjectKey);
    }

    @Test
    void testGetAvailableInstances() {
        Set<String> instances = jiraService.getAvailableInstances();

        assertNotNull(instances, "Available instances should not be null");
        assertFalse(instances.isEmpty(), "Available instances should not be empty");
        log.info("Available Jira instances: {}", instances);
    }

    @Test
    void testHasInstance() {
        assertTrue(jiraService.hasInstance(testInstance),
                "Test instance '" + testInstance + "' should be configured");
    }

    @Test
    void testHasInstance_NonExistent() {
        assertFalse(jiraService.hasInstance("nonexistent-instance-xyz"),
                "Non-existent instance should return false");
    }

    @Test
    void testIsHealthy() {
        boolean healthy = jiraService.isHealthy();
        assertTrue(healthy, "Jira service should be healthy (serverInfo reachable)");
        log.info("Jira isHealthy() returned: {}", healthy);
    }

    @Test
    void testProjectExists_ExistingProject() throws JiraException {
        boolean exists = jiraService.projectExists(testInstance, testProjectKey);

        assertTrue(exists,
                "Project '" + testProjectKey + "' should exist on instance '" + testInstance + "'");
        log.info("Project '{}' exists on instance '{}': {}", testProjectKey, testInstance, exists);
    }

    @Test
    void testProjectExists_NonExistentProject() throws JiraException {
        boolean exists = jiraService.projectExists(testInstance, testNonExistentProjectKey);

        assertFalse(exists,
                "Project '" + testNonExistentProjectKey + "' should NOT exist");
        log.info("Project '{}' exists on instance '{}': {}", testNonExistentProjectKey, testInstance, exists);
    }

    @Test
    void testProjectExists_nullInstanceName() {
      // This should throw a JiraException
        assertThrows(JiraException.class, () -> jiraService.projectExists(null, testProjectKey),
                "Calling projectExists with null instance name should throw JiraException");
        assertThrows(JiraException.class, () -> jiraService.projectExists("", testProjectKey),
                "Calling projectExists with blank instance name should throw JiraException");
        assertThrows(JiraException.class, () -> jiraService.projectExists("   ", testProjectKey),   
                "Calling projectExists with whitespace instance name should throw JiraException");

    }

    // -------------------------------------------------------------------------
    // Default-instance tests
    // -------------------------------------------------------------------------

    @Test
    void testGetDefaultInstance_ReturnsNonNullInstanceName() throws JiraException {
        String defaultInstance = jiraService.getDefaultInstance();

        assertNotNull(defaultInstance, "Default instance name should not be null");
        assertFalse(defaultInstance.isBlank(), "Default instance name should not be blank");
        assertTrue(jiraService.hasInstance(defaultInstance),
                "Default instance '" + defaultInstance + "' should be a configured instance");
        log.info("Default Jira instance resolved to: '{}'", defaultInstance);
    }

    @Test
    void testProjectExists_UsingDefaultInstance_ExistingProject() throws JiraException {
        // Calls the no-instance-name overload – should route to the configured default
        boolean exists = jiraService.projectExists(testProjectKey);

        assertTrue(exists,
                "Project '" + testProjectKey + "' should exist via the default instance");
        log.info("projectExists('{}') via default instance returned: {}", testProjectKey, exists);
    }

    @Test
    void testProjectExists_UsingDefaultInstance_NonExistentProject() throws JiraException {
        boolean exists = jiraService.projectExists(testNonExistentProjectKey);

        assertFalse(exists,
                "Project '" + testNonExistentProjectKey + "' should NOT exist via the default instance");
        log.info("projectExists('{}') via default instance returned: {}", testNonExistentProjectKey, exists);
    }

}
