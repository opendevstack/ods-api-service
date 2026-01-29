package org.opendevstack.apiservice.externalservice.aap.integration;

import org.opendevstack.apiservice.externalservice.aap.command.ExecuteWorkflowCommand;
import org.opendevstack.apiservice.externalservice.aap.command.ExecuteWorkflowRequest;
import org.opendevstack.apiservice.externalservice.aap.command.GetJobStatusCommand;
import org.opendevstack.apiservice.externalservice.aap.command.GetJobStatusRequest;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AAP (Ansible Automation Platform) Service.
 * 
 * This test runs against a real AAP instance configured in application-local.yaml.
 * It requires actual AAP connectivity and valid credentials.
 * 
 * Prerequisites:
 * 1. Set environment variable: AAP_INTEGRATION_TEST_ENABLED=true
 * 2. Configure AAP properties via environment variables or application-local.yaml:
 *    - ANSIBLE_BASE_URL (e.g., "http://localhost:8080/api/v2")
 *    - ANSIBLE_USERNAME (username for authentication)
 *    - ANSIBLE_PASSWORD (password for authentication)
 *    - ANSIBLE_TIMEOUT (timeout in milliseconds, default: 30000)
 *    - ANSIBLE_SSL_VERIFY (SSL certificate verification, default: true)
 * 3. Set test-specific environment variables:
 *    - AAP_TEST_WORKFLOW_NAME (default: "Example Workflow")
 *    - AAP_TEST_JOB_ID (optional - ID of an existing job for retrieval tests)
 * 
 * Example:
 * export AAP_INTEGRATION_TEST_ENABLED=true
 * export ANSIBLE_BASE_URL=http://localhost:8080/api/v2
 * export ANSIBLE_USERNAME=admin
 * export ANSIBLE_PASSWORD=password
 * export AAP_TEST_WORKFLOW_NAME="Example Workflow"
 * 
 * Then run: mvn test -Dtest=AapServiceIntegrationTest -pl external-service-aap
 */
@SpringBootTest(classes = AapIntegrationTestConfig.class)
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "AAP_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class AapServiceIntegrationTest {

    @Autowired
    private AutomationPlatformService aapService;

    @Autowired
    private ExecuteWorkflowCommand executeWorkflowCommand;

    @Autowired
    private GetJobStatusCommand getJobStatusCommand;

    private String testWorkflowName;
    private String testJobId;

    @BeforeEach
    void setUp() {
        // Read test parameters from environment variables
        testWorkflowName = System.getenv().getOrDefault("AAP_TEST_WORKFLOW_NAME", "Example Workflow");
        testJobId = System.getenv().get("AAP_TEST_JOB_ID");

        log.info("=".repeat(80));
        log.info("AAP Integration Test Configuration:");
        log.info("  Service Name: {}", aapService.getServiceName());
        log.info("  Test Workflow Name: {}", testWorkflowName);
        log.info("  Test Job ID: {}", testJobId != null ? testJobId : "NOT SET");
        log.info("=".repeat(80));
    }

    // ==================== Connection & Health Tests ====================

    /**
     * Test 1: Verify service name and metadata
     */
    @Test
    void testServiceName() {
        log.info("TEST: Verify service metadata");

        String serviceName = aapService.getServiceName();
        assertNotNull(serviceName, "Service name should not be null");
        assertFalse(serviceName.isBlank(), "Service name should not be empty");
        
        log.info("✓ Service name: {}", serviceName);
    }

    /**
     * Test 2: Validate connection to AAP instance
     */
    @Test
    void testValidateConnection_Success() {
        log.info("TEST: Validate connection to AAP instance");

        boolean isConnected = aapService.validateConnection();
        assertTrue(isConnected, "Should be able to connect to AAP instance");
        
        log.info("✓ Successfully validated connection to AAP");
    }

    /**
     * Test 3: Check AAP service health
     */
    @Test
    void testIsHealthy_Healthy() {
        log.info("TEST: Check AAP service health");

        boolean isHealthy = aapService.isHealthy();
        assertTrue(isHealthy, "AAP service should be healthy");
        
        log.info("✓ AAP service is healthy");
    }

    // ==================== Workflow Execution Tests ====================

    /**
     * Test 4: Execute workflow with default parameters
     */
    @Test
    void testExecuteWorkflow_Success() {
        log.info("TEST: Execute workflow with default parameters");

        try {
            ExecuteWorkflowRequest request = new ExecuteWorkflowRequest();
            request.setWorkflowName(testWorkflowName);

            AutomationExecutionResult result = executeWorkflowCommand.execute(request);

            assertNotNull(result, "Result should not be null");
            assertNotNull(result.getJobId(), "Job ID should not be null");
            assertFalse(result.getJobId().isBlank(), "Job ID should not be empty");
            
            log.info("✓ Successfully executed workflow");
            log.info("  Job ID: {}", result.getJobId());
        } catch (ExternalServiceException e) {
            log.error("Failed to execute workflow", e);
            fail("Should be able to execute workflow: " + e.getMessage());
        }
    }

    /**
     * Test 5: Execute workflow with custom parameters
     */
    @Test
    void testExecuteWorkflow_WithCustomParameters() {
        log.info("TEST: Execute workflow with custom parameters");

        try {
            ExecuteWorkflowRequest request = new ExecuteWorkflowRequest();
            request.setWorkflowName(testWorkflowName);

            // Add custom parameters
            Map<String, Object> params = new HashMap<>();
            params.put("project_name", "test-project");
            params.put("environment", "staging");
            request.setParameters(params);

            AutomationExecutionResult result = executeWorkflowCommand.execute(request);

            assertNotNull(result, "Result should not be null");
            assertNotNull(result.getJobId(), "Job ID should not be null");
            
            log.info("✓ Successfully executed workflow with custom parameters");
            log.info("  Job ID: {}", result.getJobId());
        } catch (ExternalServiceException e) {
            log.error("Failed to execute workflow with parameters", e);
            fail("Should be able to execute workflow: " + e.getMessage());
        }
    }

    // ==================== Job Status Tests ====================

    /**
     * Test 6: Retrieve job status for existing job
     * Requires: AAP_TEST_JOB_ID environment variable
     */
    @Test
    void testGetJobStatus_ExistingJob() {
        if (testJobId == null) {
            log.warn("Skipping test - AAP_TEST_JOB_ID not set");
            return;
        }

        log.info("TEST: Retrieve job status for existing job");

        try {
            GetJobStatusRequest request = new GetJobStatusRequest();
            request.setJobId(testJobId);

            AutomationJobStatus status = getJobStatusCommand.execute(request);

            assertNotNull(status, "Status should not be null");
            assertEquals(testJobId, status.getJobId(), "Job ID should match request");
            assertNotNull(status.getStatus(), "Status should not be null");
            
            log.info("✓ Successfully retrieved job status");
            log.info("  Job ID: {}", status.getJobId());
            log.info("  Status: {}", status.getStatus());
            log.info("  Progress: {}%", status.getProgress());
        } catch (ExternalServiceException e) {
            log.error("Failed to retrieve job status", e);
            fail("Should be able to retrieve job status: " + e.getMessage());
        }
    }

    /**
     * Test 7: Execute workflow and poll for completion
     */
    @Test
    void testExecuteWorkflow_AndPollStatus() {
        log.info("TEST: Execute workflow and poll for job status");

        try {
            // Execute workflow
            ExecuteWorkflowRequest execRequest = new ExecuteWorkflowRequest();
            execRequest.setWorkflowName(testWorkflowName);

            AutomationExecutionResult execResult = executeWorkflowCommand.execute(execRequest);
            String jobId = String.valueOf(execResult.getJobId());

            assertNotNull(jobId, "Job ID should not be null");
            log.info("  Executed workflow, Job ID: {}", jobId);

            // Poll for status (with timeout)
            GetJobStatusRequest statusRequest = new GetJobStatusRequest();
            statusRequest.setJobId(jobId);

            AutomationJobStatus status = getJobStatusCommand.execute(statusRequest);

            assertNotNull(status, "Status should not be null");
            assertEquals(jobId, status.getJobId(), "Job ID should match");
            
            log.info("✓ Successfully executed workflow and retrieved status");
            log.info("  Job Status: {}", status.getStatus());
        } catch (ExternalServiceException e) {
            log.error("Failed to execute and poll workflow", e);
            fail("Should be able to execute and poll workflow: " + e.getMessage());
        }
    }

    // ==================== Error Handling Tests ====================

    /**
     * Test 8: Handle invalid workflow name
     */
    @Test
    void testExecuteWorkflow_InvalidWorkflow() {
        log.info("TEST: Execute workflow with invalid name");

        ExecuteWorkflowRequest request = new ExecuteWorkflowRequest();
        request.setWorkflowName("NON_EXISTENT_WORKFLOW_" + System.currentTimeMillis());

        assertThrows(ExternalServiceException.class, 
            () -> executeWorkflowCommand.execute(request),
            "Should throw exception for invalid workflow");
        
        log.info("✓ Correctly rejected invalid workflow");
    }

    /**
     * Test 9: Handle request validation errors
     */
    @Test
    void testExecuteWorkflow_NullWorkflowName() {
        log.info("TEST: Execute workflow with null workflow name");

        ExecuteWorkflowRequest request = new ExecuteWorkflowRequest();
        request.setWorkflowName(null);

        assertThrows(ExternalServiceException.class,
            () -> executeWorkflowCommand.execute(request),
            "Should throw exception for null workflow name");
        
        log.info("✓ Correctly rejected null workflow name");
    }

    /**
     * Test 10: Handle invalid job ID
     */
    @Test
    void testGetJobStatus_InvalidJobId() {
        log.info("TEST: Retrieve status for invalid job ID");

        GetJobStatusRequest request = new GetJobStatusRequest();
        request.setJobId("INVALID_JOB_ID_" + System.currentTimeMillis());

        assertThrows(ExternalServiceException.class,
            () -> getJobStatusCommand.execute(request),
            "Should throw exception for invalid job ID");
        
        log.info("✓ Correctly rejected invalid job ID");
    }

    /**
     * Test 11: Verify command registration
     */
    @Test
    void testCommandRegistration() {
        log.info("TEST: Verify command registration in registry");

        assertNotNull(executeWorkflowCommand, "ExecuteWorkflowCommand should be registered");
        assertNotNull(getJobStatusCommand, "GetJobStatusCommand should be registered");
        
        log.info("✓ All AAP commands are properly registered");
    }
}
