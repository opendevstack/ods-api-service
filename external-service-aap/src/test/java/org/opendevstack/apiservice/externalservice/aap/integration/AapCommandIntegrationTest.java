package org.opendevstack.apiservice.externalservice.aap.integration;

import org.opendevstack.apiservice.externalservice.aap.command.ExecuteWorkflowAsyncCommand;
import org.opendevstack.apiservice.externalservice.aap.command.ExecuteWorkflowAsyncRequest;
import org.opendevstack.apiservice.externalservice.aap.command.ExecuteWorkflowCommand;
import org.opendevstack.apiservice.externalservice.aap.command.ExecuteWorkflowRequest;
import org.opendevstack.apiservice.externalservice.aap.command.GetJobStatusCommand;
import org.opendevstack.apiservice.externalservice.aap.command.GetJobStatusRequest;
import org.opendevstack.apiservice.externalservice.aap.command.GetWorkflowJobStatusCommand;
import org.opendevstack.apiservice.externalservice.aap.command.GetWorkflowJobStatusRequest;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
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
 * Integration test for AAP Commands.
 * 
 * This test runs against a real AAP instance configured in application-local.yaml.
 * It requires actual AAP connectivity and valid credentials.
 * 
 * To run these tests:
 * 1. Ensure application-local.yaml has valid AAP configuration
 * 2. Set environment variable: AAP_INTEGRATION_TEST_ENABLED=true
 * 3. Set optional test parameters:
 *    - AAP_TEST_WORKFLOW_NAME (default: "Example Workflow")
 *    - AAP_TEST_JOB_ID (optional, for job status tests)
 * 
 * Example:
 * export AAP_INTEGRATION_TEST_ENABLED=true
 * export AAP_TEST_WORKFLOW_NAME="Example Workflow"
 * 
 * Then run: mvn test -Dtest=AapCommandIntegrationTest -pl external-service-aap
 */
@SpringBootTest(classes = AapIntegrationTestConfig.class)
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "AAP_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class AapCommandIntegrationTest {

    @Autowired
    private ExecuteWorkflowCommand executeWorkflowCommand;

    @Autowired
    private ExecuteWorkflowAsyncCommand executeWorkflowAsyncCommand;

    @Autowired
    private GetJobStatusCommand getJobStatusCommand;

    @Autowired
    private GetWorkflowJobStatusCommand getWorkflowJobStatusCommand;

    private String testWorkflowName;
    private String testJobId;

    @BeforeEach
    void setUp() {
        testWorkflowName = System.getenv().getOrDefault("AAP_TEST_WORKFLOW_NAME", "Example Workflow");
        testJobId = System.getenv().get("AAP_TEST_JOB_ID");

        log.info("=".repeat(80));
        log.info("AAP Command Integration Test Configuration:");
        log.info("  Test Workflow Name: {}", testWorkflowName);
        log.info("  Test Job ID: {}", testJobId != null ? testJobId : "NOT SET");
        log.info("=".repeat(80));
    }

    // ==================== ExecuteWorkflowCommand Tests ====================

    /**
     * Test 1: Execute workflow command with valid workflow name
     */
    @Test
    void testExecuteWorkflowCommand_Success() {
        log.info("TEST: ExecuteWorkflowCommand with valid workflow name");

        ExecuteWorkflowRequest request = new ExecuteWorkflowRequest();
        request.setWorkflowName(testWorkflowName);

        assertDoesNotThrow(() -> {
            AutomationExecutionResult result = executeWorkflowCommand.execute(request);
            assertNotNull(result, "Result should not be null");
            assertNotNull(result.getJobId(), "Job ID should not be null");
            assertFalse(result.getJobId().isBlank(), "Job ID should not be empty");
            log.info("✓ Successfully executed workflow, Job ID: {}", result.getJobId());
        });
    }

    /**
     * Test 2: ExecuteWorkflowCommand request validation - null workflow name
     */
    @Test
    void testExecuteWorkflowCommand_RequestValidation_NullWorkflow() {
        log.info("TEST: ExecuteWorkflowCommand validation - null workflow name");

        ExecuteWorkflowRequest request = new ExecuteWorkflowRequest();
        request.setWorkflowName(null);

        assertThrows(ExternalServiceException.class,
            () -> executeWorkflowCommand.execute(request),
            "Should throw exception for null workflow name");
        
        log.info("✓ Correctly validated null workflow name");
    }

    /**
     * Test 3: ExecuteWorkflowCommand request validation - empty workflow name
     */
    @Test
    void testExecuteWorkflowCommand_RequestValidation_EmptyWorkflow() {
        log.info("TEST: ExecuteWorkflowCommand validation - empty workflow name");

        ExecuteWorkflowRequest request = new ExecuteWorkflowRequest();
        request.setWorkflowName("");

        assertThrows(ExternalServiceException.class,
            () -> executeWorkflowCommand.execute(request),
            "Should throw exception for empty workflow name");
        
        log.info("✓ Correctly validated empty workflow name");
    }

    /**
     * Test 4: Verify ExecuteWorkflowCommand metadata
     */
    @Test
    void testExecuteWorkflowCommand_Metadata() {
        log.info("TEST: Verify ExecuteWorkflowCommand metadata");

        assertNotNull(executeWorkflowCommand, "ExecuteWorkflowCommand should be autowired");
        log.info("✓ ExecuteWorkflowCommand is properly registered");
    }

    // ==================== GetJobStatusCommand Tests ====================

    /**
     * Test 5: GetJobStatusCommand with valid job ID
     * Requires: AAP_TEST_JOB_ID environment variable
     */
    @Test
    void testGetJobStatusCommand_Success() {
        if (testJobId == null) {
            log.warn("Skipping test - AAP_TEST_JOB_ID not set");
            return;
        }

        log.info("TEST: GetJobStatusCommand with valid job ID");

        GetJobStatusRequest request = new GetJobStatusRequest();
        request.setJobId(testJobId);

        assertDoesNotThrow(() -> {
            AutomationJobStatus result = getJobStatusCommand.execute(request);
            assertNotNull(result, "Result should not be null");
            assertEquals(testJobId, result.getJobId(), "Job ID should match");
            assertNotNull(result.getStatus(), "Status should not be null");
            log.info("✓ Successfully retrieved job status: {}", result.getStatus());
        });
    }

    /**
     * Test 6: GetJobStatusCommand request validation - null job ID
     */
    @Test
    void testGetJobStatusCommand_RequestValidation_NullJobId() {
        log.info("TEST: GetJobStatusCommand validation - null job ID");

        GetJobStatusRequest request = new GetJobStatusRequest();
        request.setJobId(null);

        assertThrows(ExternalServiceException.class,
            () -> getJobStatusCommand.execute(request),
            "Should throw exception for null job ID");
        
        log.info("✓ Correctly validated null job ID");
    }

    /**
     * Test 7: GetJobStatusCommand request validation - empty job ID
     */
    @Test
    void testGetJobStatusCommand_RequestValidation_EmptyJobId() {
        log.info("TEST: GetJobStatusCommand validation - empty job ID");

        GetJobStatusRequest request = new GetJobStatusRequest();
        request.setJobId("");

        assertThrows(ExternalServiceException.class,
            () -> getJobStatusCommand.execute(request),
            "Should throw exception for empty job ID");
        
        log.info("✓ Correctly validated empty job ID");
    }

    /**
     * Test 8: GetJobStatusCommand with invalid job ID
     */
    @Test
    void testGetJobStatusCommand_InvalidJobId() {
        log.info("TEST: GetJobStatusCommand with invalid job ID");

        GetJobStatusRequest request = new GetJobStatusRequest();
        request.setJobId("INVALID_JOB_" + System.currentTimeMillis());

        assertThrows(ExternalServiceException.class,
            () -> getJobStatusCommand.execute(request),
            "Should throw exception for invalid job ID");
        
        log.info("✓ Correctly rejected invalid job ID");
    }

    /**
     * Test 9: Verify GetJobStatusCommand metadata
     */
    @Test
    void testGetJobStatusCommand_Metadata() {
        log.info("TEST: Verify GetJobStatusCommand metadata");

        assertNotNull(getJobStatusCommand, "GetJobStatusCommand should be autowired");
        log.info("✓ GetJobStatusCommand is properly registered");
    }

    /**
     * Test 10: End-to-end workflow execution and status retrieval
     */
    @Test
    void testEndToEnd_ExecuteAndRetrieveStatus() {
        log.info("TEST: End-to-end workflow execution and status retrieval");

        try {
            // Step 1: Execute workflow
            ExecuteWorkflowRequest execRequest = new ExecuteWorkflowRequest();
            execRequest.setWorkflowName(testWorkflowName);

            AutomationExecutionResult execResult = executeWorkflowCommand.execute(execRequest);
            String workflowJobId = String.valueOf(execResult.getJobId());
            
            assertNotNull(workflowJobId, "Workflow Job ID should not be null");
            log.info("✓ Executed workflow, Workflow Job ID: {}", workflowJobId);

            // Step 2: Retrieve workflow job status (not regular job status)
            GetWorkflowJobStatusRequest statusRequest = new GetWorkflowJobStatusRequest();
            statusRequest.setWorkflowId(workflowJobId);

            AutomationJobStatus status = getWorkflowJobStatusCommand.execute(statusRequest);
            
            assertNotNull(status, "Status should not be null");
            assertEquals(workflowJobId, status.getJobId(), "Workflow Job ID should match");
            assertNotNull(status.getStatus(), "Status should not be null");
            
            log.info("✓ Retrieved workflow job status: {}", status.getStatus());
            log.info("  Progress: {}%", status.getProgress());

        } catch (ExternalServiceException e) {
            log.error("Failed in end-to-end test", e);
            fail("End-to-end workflow should succeed: " + e.getMessage());
        }
    }

    // ==================== ExecuteWorkflowAsyncCommand Tests ====================

    /**
     * Test 11: Execute workflow async command with valid workflow name
     */
    @Test
    void testExecuteWorkflowAsyncCommand_Success() {
        log.info("TEST: ExecuteWorkflowAsyncCommand with valid workflow name");

        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName(testWorkflowName)
                .build();

        assertDoesNotThrow(() -> {
            AutomationExecutionResult result = executeWorkflowAsyncCommand.execute(request);
            assertNotNull(result, "Result should not be null");
            assertNotNull(result.getJobId(), "Job ID should not be null");
            assertFalse(result.getJobId().isBlank(), "Job ID should not be empty");
            log.info("✓ Successfully executed async workflow, Job ID: {}", result.getJobId());
        });
    }

    /**
     * Test 12: Execute workflow async command with parameters
     */
    @Test
    void testExecuteWorkflowAsyncCommand_WithParameters() {
        log.info("TEST: ExecuteWorkflowAsyncCommand with parameters");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test_param", "test_value");
        parameters.put("env", "integration-test");

        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName(testWorkflowName)
                .parameters(parameters)
                .build();

        assertDoesNotThrow(() -> {
            AutomationExecutionResult result = executeWorkflowAsyncCommand.execute(request);
            assertNotNull(result, "Result should not be null");
            assertNotNull(result.getJobId(), "Job ID should not be null");
            assertNotEquals("FAILED", result.getStatus(), "Parameters should not match");
            assertEquals("pending", result.getStatus(), "Parameters should match");
            log.info("✓ Successfully executed async workflow with parameters, Job ID: {}", result.getJobId());
        });
    }

    /**
     * Test 13: ExecuteWorkflowAsyncCommand request validation - null workflow name
     */
    @Test
    void testExecuteWorkflowAsyncCommand_RequestValidation_NullWorkflow() {
        log.info("TEST: ExecuteWorkflowAsyncCommand validation - null workflow name");

        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName(null)
                .build();

        assertThrows(ExternalServiceException.class,
            () -> executeWorkflowAsyncCommand.execute(request),
            "Should throw exception for null workflow name");
        
        log.info("✓ Correctly validated null workflow name");
    }

    /**
     * Test 14: ExecuteWorkflowAsyncCommand request validation - empty workflow name
     */
    @Test
    void testExecuteWorkflowAsyncCommand_RequestValidation_EmptyWorkflow() {
        log.info("TEST: ExecuteWorkflowAsyncCommand validation - empty workflow name");

        ExecuteWorkflowAsyncRequest request = ExecuteWorkflowAsyncRequest.builder()
                .workflowName("")
                .build();

        assertThrows(ExternalServiceException.class,
            () -> executeWorkflowAsyncCommand.execute(request),
            "Should throw exception for empty workflow name");
        
        log.info("✓ Correctly validated empty workflow name");
    }

    /**
     * Test 15: Verify ExecuteWorkflowAsyncCommand metadata
     */
    @Test
    void testExecuteWorkflowAsyncCommand_Metadata() {
        log.info("TEST: Verify ExecuteWorkflowAsyncCommand metadata");

        assertNotNull(executeWorkflowAsyncCommand, "ExecuteWorkflowAsyncCommand should be autowired");
        assertEquals("execute-workflow-async", executeWorkflowAsyncCommand.getCommandName(), 
                "Command name should be 'execute-workflow-async'");
        assertEquals("aap", executeWorkflowAsyncCommand.getServiceName(), 
                "Service name should be 'aap'");
        log.info("✓ ExecuteWorkflowAsyncCommand is properly registered with correct metadata");
    }

    /**
     * Test 16: End-to-end async workflow execution and status retrieval
     */
    @Test
    void testEndToEnd_AsyncExecuteAndRetrieveStatus() {
        log.info("TEST: End-to-end async workflow execution and status retrieval");

        try {
            // Step 1: Execute workflow asynchronously
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("triggered_by", "integration-test");
            
            ExecuteWorkflowAsyncRequest execRequest = ExecuteWorkflowAsyncRequest.builder()
                    .workflowName(testWorkflowName)
                    .parameters(parameters)
                    .build();

            AutomationExecutionResult execResult = executeWorkflowAsyncCommand.execute(execRequest);
            String workflowJobId = String.valueOf(execResult.getJobId());
            
            assertNotNull(workflowJobId, "Workflow Job ID should not be null");
            log.info("✓ Async executed workflow, Workflow Job ID: {}", workflowJobId);

            // Step 2: Retrieve workflow job status
            GetWorkflowJobStatusRequest statusRequest = new GetWorkflowJobStatusRequest();
            statusRequest.setWorkflowId(workflowJobId);

            AutomationJobStatus status = getWorkflowJobStatusCommand.execute(statusRequest);
            
            assertNotNull(status, "Status should not be null");
            assertEquals(workflowJobId, status.getJobId(), "Workflow Job ID should match");
            assertNotNull(status.getStatus(), "Status should not be null");
            
            log.info("✓ Retrieved workflow job status: {}", status.getStatus());
            log.info("  Progress: {}%", status.getProgress());

        } catch (ExternalServiceException e) {
            log.error("Failed in async end-to-end test", e);
            fail("Async end-to-end workflow should succeed: " + e.getMessage());
        }
    }

    /**
     * Test 17: Compare sync vs async workflow execution
     */
    @Test
    void testCompare_SyncVsAsyncExecution() {
        log.info("TEST: Compare sync vs async workflow execution");

        try {
            // Execute sync workflow
            ExecuteWorkflowRequest syncRequest = new ExecuteWorkflowRequest();
            syncRequest.setWorkflowName(testWorkflowName);

            long syncStartTime = System.currentTimeMillis();
            AutomationExecutionResult syncResult = executeWorkflowCommand.execute(syncRequest);
            long syncDuration = System.currentTimeMillis() - syncStartTime;
            
            assertNotNull(syncResult.getJobId(), "Sync Job ID should not be null");
            log.info("✓ Sync execution completed in {}ms, Job ID: {}", syncDuration, syncResult.getJobId());

            // Execute async workflow
            ExecuteWorkflowAsyncRequest asyncRequest = ExecuteWorkflowAsyncRequest.builder()
                    .workflowName(testWorkflowName)
                    .build();

            long asyncStartTime = System.currentTimeMillis();
            AutomationExecutionResult asyncResult = executeWorkflowAsyncCommand.execute(asyncRequest);
            long asyncDuration = System.currentTimeMillis() - asyncStartTime;
            
            assertNotNull(asyncResult.getJobId(), "Async Job ID should not be null");
            log.info("✓ Async execution completed in {}ms, Job ID: {}", asyncDuration, asyncResult.getJobId());

            // Both should return valid job IDs
            assertNotEquals(syncResult.getJobId(), asyncResult.getJobId(), 
                    "Sync and Async should create different jobs");
            
            log.info("✓ Both sync and async methods work correctly");
            log.info("  Sync execution: {}ms", syncDuration);
            log.info("  Async execution: {}ms", asyncDuration);

        } catch (ExternalServiceException e) {
            log.error("Failed in sync vs async comparison test", e);
            fail("Sync vs Async comparison should succeed: " + e.getMessage());
        }
    }
}
