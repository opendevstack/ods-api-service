package org.opendevstack.apiservice.externalservice.uipath.integration;

import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.uipath.command.AddQueueItemCommand;
import org.opendevstack.apiservice.externalservice.uipath.command.AddQueueItemRequest;
import org.opendevstack.apiservice.externalservice.uipath.command.GetQueueItemStatusCommand;
import org.opendevstack.apiservice.externalservice.uipath.command.GetQueueItemStatusRequest;
import org.opendevstack.apiservice.externalservice.uipath.exception.UiPathException;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemRequest;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemResult;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemResult.ResultStatus;
import org.opendevstack.apiservice.externalservice.uipath.service.UiPathOrchestratorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UiPath Orchestrator Service.
 * 
 * This test runs against a real UiPath Orchestrator instance configured in application-local.yaml.
 * It requires actual UiPath connectivity and valid credentials.
 * 
 * Prerequisites:
 * 1. Set environment variable: UIPATH_INTEGRATION_TEST_ENABLED=true
 * 2. Configure UiPath properties via environment variables or application-local.yaml:
 *    - UIPATH_HOST (e.g., "https://orchestrator.example.com")
 *    - UIPATH_CLIENT_ID (client ID for authentication)
 *    - UIPATH_CLIENT_SECRET (client secret for authentication)
 *    - UIPATH_TENANCY_NAME (default: "default")
 *    - UIPATH_ORGANIZATION_UNIT_ID (organization unit ID)
 * 3. Set test-specific environment variables:
 *    - UIPATH_TEST_QUEUE_NAME (default: "Q_EDP_Project_Requests")
 *    - UIPATH_TEST_REFERENCE_PREFIX (default: "TEST-INTEGRATION")
 *    - UIPATH_TEST_EXISTING_QUEUE_ITEM_ID (optional - ID of an existing queue item for retrieval tests)
 *    - UIPATH_TEST_EXISTING_REFERENCE (optional - reference of an existing queue item for query tests)
 * 
 * Example:
 * export UIPATH_INTEGRATION_TEST_ENABLED=true
 * export UIPATH_HOST=https://orchestrator.example.com
 * export UIPATH_CLIENT_ID=your-client-id
 * export UIPATH_CLIENT_SECRET=your-client-secret
 * export UIPATH_TENANCY_NAME=default
 * export UIPATH_ORGANIZATION_UNIT_ID=123456
 * export UIPATH_TEST_QUEUE_NAME=Q_EDP_Project_Requests
 * 
 * Then run: mvn test -Dtest=UiPathServiceIntegrationTest -pl external-service-uipath
 */
@SpringBootTest(classes = UiPathIntegrationTestConfig.class)
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "UIPATH_INTEGRATION_TEST_ENABLED", matches = "true")
@Slf4j
class UiPathServiceIntegrationTest {

    @Autowired
    private UiPathOrchestratorService uiPathService;

    @Autowired
    private AddQueueItemCommand addQueueItemCommand;

    @Autowired
    private GetQueueItemStatusCommand getQueueItemStatusCommand;

    private String testQueueName;
    private String testReferencePrefix;
    private String existingQueueItemId;
    private String existingReference;

    @BeforeEach
    void setUp() {
        // Read test parameters from environment variables
        testQueueName = System.getenv().getOrDefault("UIPATH_TEST_QUEUE_NAME", "TEST_REQUESTS");
        testReferencePrefix = System.getenv().getOrDefault("UIPATH_TEST_REFERENCE_PREFIX", "TEST-INTEGRATION");
        existingQueueItemId = System.getenv().get("UIPATH_TEST_EXISTING_QUEUE_ITEM_ID");
        existingReference = System.getenv().get("UIPATH_TEST_EXISTING_REFERENCE");

        log.info("=".repeat(80));
        log.info("UiPath Integration Test Configuration:");
        log.info("  Service Name: {}", uiPathService.getServiceName());
        log.info("  Test Queue Name: {}", testQueueName);
        log.info("  Test Reference Prefix: {}", testReferencePrefix);
        log.info("  Existing Queue Item ID: {}", existingQueueItemId != null ? existingQueueItemId : "NOT SET");
        log.info("  Existing Reference: {}", existingReference != null ? existingReference : "NOT SET");
        log.info("=".repeat(80));
    }

    // ==================== Connection & Health Tests ====================

    /**
     * Test 1: Verify service health and connectivity.
     */
    @Test
    void testServiceHealth() {
        log.info("TEST: Verify service health and connectivity");

        // Check service name
        assertEquals("uipath", uiPathService.getServiceName(), "Service name should be 'uipath'");

        // Check health
        boolean isHealthy = uiPathService.isHealthy();
        
        // Note: Health check may fail if credentials are invalid, so we just log the result
        assertTrue(isHealthy, "Service should be healthy when properly configured");
    }

    /**
     * Test 2: Validate connection to UiPath Orchestrator.
     */
    @Test
    void testValidateConnection() {
        log.info("TEST: Validate connection to UiPath Orchestrator");

        boolean isValid = uiPathService.validateConnection();
        
        assertTrue(isValid, "Connection should be valid with proper credentials");
    }

    // ==================== Authentication Tests ====================

    /**
     * Test 3: Authenticate to UiPath Orchestrator.
     */
    @Test
    void testAuthenticate() throws UiPathException.AuthenticationException {
        log.info("TEST: Authenticate to UiPath Orchestrator");

        String token = uiPathService.authenticate();
        
        assertNotNull(token, "Authentication token should not be null");
        assertFalse(token.isEmpty(), "Authentication token should not be empty");
    }

    // ==================== Queue Item Creation Tests ====================

    /**
     * Test 4: Add a queue item to UiPath Orchestrator.
     * Note: This test creates a real queue item in UiPath, use with caution.
     */
    @Test
    //@EnabledIfEnvironmentVariable(named = "UIPATH_TEST_CREATE_QUEUE_ITEMS", matches = "true")
    void testAddQueueItem() throws UiPathException.QueueItemCreationException {
        log.info("TEST: Add queue item to UiPath Orchestrator");

        // Generate a unique reference for this test
        String reference = testReferencePrefix + "-" + System.currentTimeMillis();
        
        // Build specific content for the queue item
        Map<String, Object> specificContent = new HashMap<>();
        specificContent.put("Project Key", reference);
        specificContent.put("Test Timestamp", System.currentTimeMillis());
        specificContent.put("Test Mode", "Integration Test");

        // Create the request
        UiPathQueueItemRequest request = UiPathQueueItemRequest.builder()
                .queueName(testQueueName)
                .reference(reference)
                .priority("Normal")
                .specificContent(specificContent)
                .build();

        log.info("Creating queue item:");
        log.info("  Queue Name: {}", testQueueName);
        log.info("  Reference: {}", reference);
        log.info("  Specific Content: {}", specificContent);

        // Add the queue item
        UiPathQueueItem result = uiPathService.addQueueItem(request);

        // Assertions
        assertNotNull(result, "Created queue item should not be null");
        assertNotNull(result.getId(), "Queue item ID should not be null");
        assertEquals(reference, result.getReference(), "Reference should match");
        
        log.info("✓ Successfully created queue item:");
        log.info("  ID: {}", result.getId());
        log.info("  Reference: {}", result.getReference());
        log.info("  Status: {}", result.getStatus());
        log.info("  Priority: {}", result.getPriority());
    }

    // ==================== Queue Item Retrieval Tests ====================

    /**
     * Test 5: Get queue item by ID.
     * Requires UIPATH_TEST_EXISTING_QUEUE_ITEM_ID to be set.
     */
    @Test
    void testGetQueueItemById() throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException {
        if (existingQueueItemId == null) {
            log.warn("SKIPPED: UIPATH_TEST_EXISTING_QUEUE_ITEM_ID not set");
            return;
        }

        log.info("TEST: Get queue item by ID: {}", existingQueueItemId);

        Long queueItemId = Long.parseLong(existingQueueItemId);
        UiPathQueueItem queueItem = uiPathService.getQueueItemById(queueItemId);

        assertNotNull(queueItem, "Queue item should not be null");
        assertEquals(queueItemId, queueItem.getId(), "Queue item ID should match");

        log.info("✓ Successfully retrieved queue item:");
        log.info("  ID: {}", queueItem.getId());
        log.info("  Reference: {}", queueItem.getReference());
        log.info("  Status: {}", queueItem.getStatus());
        log.info("  Creation Time: {}", queueItem.getCreationTime());
        log.info("  Is Finalized: {}", queueItem.isFinalized());
        log.info("  Is Successful: {}", queueItem.isSuccessful());
    }

    /**
     * Test 6: Get queue item by ID - Not Found.
     */
    @Test
    void testGetQueueItemById_NotFound() {
        log.info("TEST: Get queue item by non-existent ID");

        Long nonExistentId = 999999999L;

        UiPathException.QueueItemNotFoundException exception = assertThrows(
                UiPathException.QueueItemNotFoundException.class,
                () -> uiPathService.getQueueItemById(nonExistentId)
        );

        log.info("✓ Correctly threw QueueItemNotFoundException: {}", exception.getMessage());
        assertTrue(exception.getMessage().contains(nonExistentId.toString()),
                "Exception message should contain the queue item ID");
    }

    // ==================== Queue Item Query by Reference Tests ====================

    /**
     * Test 7: Get queue items by reference.
     * Requires UIPATH_TEST_EXISTING_REFERENCE to be set.
     */
    @Test
    void testGetQueueItemsByReference() throws UiPathException.StatusCheckException {
        if (existingReference == null) {
            log.warn("SKIPPED: UIPATH_TEST_EXISTING_REFERENCE not set");
            return;
        }

        log.info("TEST: Get queue items by reference: '{}'", existingReference);

        List<UiPathQueueItem> queueItems = uiPathService.getQueueItemsByReference(existingReference);

        assertNotNull(queueItems, "Queue items list should not be null");
        assertFalse(queueItems.isEmpty(), "Queue items list should not be empty for existing reference");

        log.info("✓ Found {} queue item(s) with reference '{}':", queueItems.size(), existingReference);
        for (UiPathQueueItem item : queueItems) {
            log.info("  - ID: {}, Status: {}, Created: {}", 
                    item.getId(), item.getStatus(), item.getCreationTime());
        }
    }

    /**
     * Test 8: Get queue items by non-existent reference.
     */
    @Test
    void testGetQueueItemsByReference_NotFound() throws UiPathException.StatusCheckException {
        log.info("TEST: Get queue items by non-existent reference");

        String nonExistentReference = "NON-EXISTENT-REF-" + System.currentTimeMillis();
        List<UiPathQueueItem> queueItems = uiPathService.getQueueItemsByReference(nonExistentReference);

        assertNotNull(queueItems, "Queue items list should not be null");
        assertTrue(queueItems.isEmpty(), "Queue items list should be empty for non-existent reference");

        log.info("✓ Correctly returned empty list for non-existent reference");
    }

    /**
     * Test 9: Get latest queue item by reference.
     * Requires UIPATH_TEST_EXISTING_REFERENCE to be set.
     */
    @Test
    void testGetLatestQueueItemByReference() throws UiPathException.StatusCheckException {
        if (existingReference == null) {
            log.warn("SKIPPED: UIPATH_TEST_EXISTING_REFERENCE not set");
            return;
        }

        log.info("TEST: Get latest queue item by reference: '{}'", existingReference);

        Optional<UiPathQueueItem> latestItem = uiPathService.getLatestQueueItemByReference(existingReference);

        assertTrue(latestItem.isPresent(), "Latest queue item should be present for existing reference");

        UiPathQueueItem item = latestItem.get();
        log.info("✓ Found latest queue item:");
        log.info("  ID: {}", item.getId());
        log.info("  Reference: {}", item.getReference());
        log.info("  Status: {}", item.getStatus());
        log.info("  Is Finalized: {}", item.isFinalized());
    }

    /**
     * Test 10: Get latest queue item by non-existent reference.
     */
    @Test
    void testGetLatestQueueItemByReference_NotFound() throws UiPathException.StatusCheckException {
        log.info("TEST: Get latest queue item by non-existent reference");

        String nonExistentReference = "NON-EXISTENT-REF-" + System.currentTimeMillis();
        Optional<UiPathQueueItem> latestItem = uiPathService.getLatestQueueItemByReference(nonExistentReference);

        assertTrue(latestItem.isEmpty(), "Latest queue item should be empty for non-existent reference");
        log.info("✓ Correctly returned empty Optional for non-existent reference");
    }

    // ==================== Queue Item Status Tests ====================

    /**
     * Test 11: Check if queue item has finalized by ID.
     * Requires UIPATH_TEST_EXISTING_QUEUE_ITEM_ID to be set.
     */
    @Test
    void testHasQueueItemFinalizedById() throws UiPathException.QueueItemNotFoundException, 
            UiPathException.StatusCheckException {
        if (existingQueueItemId == null) {
            log.warn("SKIPPED: UIPATH_TEST_EXISTING_QUEUE_ITEM_ID not set");
            return;
        }

        log.info("TEST: Check if queue item has finalized by ID: {}", existingQueueItemId);

        Long queueItemId = Long.parseLong(existingQueueItemId);
        boolean finalized = uiPathService.hasQueueItemFinalizedById(queueItemId);
        assertTrue(finalized, "The queue item is in a final state");
        
        log.info("✓ Queue item {} finalized status: {}", queueItemId, finalized);
    }

    /**
     * Test 12: Check if queue item has finalized by reference.
     * Requires UIPATH_TEST_EXISTING_REFERENCE to be set.
     */
    @Test
    void testHasQueueItemFinalized() throws UiPathException.QueueItemNotFoundException, 
            UiPathException.StatusCheckException {
        if (existingReference == null) {
            log.warn("SKIPPED: UIPATH_TEST_EXISTING_REFERENCE not set");
            return;
        }

        log.info("TEST: Check if queue item has finalized by reference: '{}'", existingReference);

        boolean finalized = uiPathService.hasQueueItemFinalized(existingReference);
        assertTrue(finalized, "The queue item is in a final state");

        log.info("✓ Queue item with reference '{}' finalized status: {}", existingReference, finalized);
    }

    /**
     * Test 13: Check queue item status for non-existent reference - should throw exception.
     */
    @Test
    void testHasQueueItemFinalized_NotFound() {
        log.info("TEST: Check if queue item has finalized for non-existent reference");

        String nonExistentReference = "NON-EXISTENT-REF-" + System.currentTimeMillis();

        UiPathException.QueueItemNotFoundException exception = assertThrows(
                UiPathException.QueueItemNotFoundException.class,
                () -> uiPathService.hasQueueItemFinalized(nonExistentReference)
        );

        log.info("✓ Correctly threw QueueItemNotFoundException: {}", exception.getMessage());
    }

    // ==================== Check Queue Item Result Tests ====================

    /**
     * Test 14: Check queue item by null reference.
     */
    @Test
    void testCheckQueueItemByReference_NullReference() {
        log.info("TEST: Check queue item by null reference");

        UiPathQueueItemResult result = uiPathService.checkQueueItemByReference(null);

        assertNotNull(result, "Result should not be null");
        assertEquals(ResultStatus.NO_REFERENCE, result.getResultStatus(), 
                "Result should indicate no reference");
        
        log.info("✓ Correctly handled null reference:");
        log.info("  Result Status: {}", result.getResultStatus());
        log.info("  Is Success (no reference is success): {}", result.isSuccess());
    }

    /**
     * Test 15: Check queue item by empty reference.
     */
    @Test
    void testCheckQueueItemByReference_EmptyReference() {
        log.info("TEST: Check queue item by empty reference");

        UiPathQueueItemResult result = uiPathService.checkQueueItemByReference("");

        assertNotNull(result, "Result should not be null");
        assertEquals(ResultStatus.NO_REFERENCE, result.getResultStatus(), 
                "Result should indicate no reference");
        
        log.info("✓ Correctly handled empty reference:");
        log.info("  Result Status: {}", result.getResultStatus());
        log.info("  Is Success (no reference is success): {}", result.isSuccess());
    }

    /**
     * Test 16: Check queue item by non-existent reference.
     */
    @Test
    void testCheckQueueItemByReference_NotFound() {
        log.info("TEST: Check queue item by non-existent reference");

        String nonExistentReference = "NON-EXISTENT-REF-" + System.currentTimeMillis();
        UiPathQueueItemResult result = uiPathService.checkQueueItemByReference(nonExistentReference);

        assertNotNull(result, "Result should not be null");
        assertEquals(ResultStatus.NOT_FOUND, result.getResultStatus(), 
                "Result should indicate not found");
        
        log.info("✓ Correctly indicated not found:");
        log.info("  Result Status: {}", result.getResultStatus());
        log.info("  Is Failure (not found is failure): {}", result.isFailure());
        log.info("  Message: {}", result.getMessage());
    }


    // ==================== Command Integration Tests ====================

    /**
     * Test 17: Verify AddQueueItemCommand metadata.
     */
    @Test
    void testAddQueueItemCommand_Metadata() {
        log.info("TEST: Verify AddQueueItemCommand metadata");

        assertEquals("add-queue-item", addQueueItemCommand.getCommandName(),
                "Command name should be 'add-queue-item'");
        assertEquals("uipath", addQueueItemCommand.getServiceName(),
                "Service name should be 'uipath'");

        log.info("✓ AddQueueItemCommand metadata:");
        log.info("  Command Name: {}", addQueueItemCommand.getCommandName());
        log.info("  Service Name: {}", addQueueItemCommand.getServiceName());
    }

    /**
     * Test 18: Verify GetQueueItemStatusCommand metadata.
     */
    @Test
    void testGetQueueItemStatusCommand_Metadata() {
        log.info("TEST: Verify GetQueueItemStatusCommand metadata");

        assertEquals("get-queue-item-status", getQueueItemStatusCommand.getCommandName(),
                "Command name should be 'get-queue-item-status'");
        assertEquals("uipath", getQueueItemStatusCommand.getServiceName(),
                "Service name should be 'uipath'");

        log.info("✓ GetQueueItemStatusCommand metadata:");
        log.info("  Command Name: {}", getQueueItemStatusCommand.getCommandName());
        log.info("  Service Name: {}", getQueueItemStatusCommand.getServiceName());
    }

    /**
     * Test 19: Verify AddQueueItemCommand validation - null request.
     */
    @Test
    void testAddQueueItemCommand_ValidationNullRequest() {
        log.info("TEST: Verify AddQueueItemCommand validation - null request");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> addQueueItemCommand.validateRequest(null)
        );

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception should indicate request cannot be null");

        log.info("✓ AddQueueItemCommand correctly rejects null request: {}", exception.getMessage());
    }

    /**
     * Test 20: Verify AddQueueItemCommand validation - null queue item request.
     */
    @Test
    void testAddQueueItemCommand_ValidationNullQueueItemRequest() {
        log.info("TEST: Verify AddQueueItemCommand validation - null queue item request");

        AddQueueItemRequest request = AddQueueItemRequest.builder()
                .queueItemRequest(null)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> addQueueItemCommand.validateRequest(request)
        );

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception should indicate queue item request cannot be null");

        log.info("✓ AddQueueItemCommand correctly rejects null queue item request: {}", exception.getMessage());
    }

    /**
     * Test 21: Verify GetQueueItemStatusCommand validation - null request.
     */
    @Test
    void testGetQueueItemStatusCommand_ValidationNullRequest() {
        log.info("TEST: Verify GetQueueItemStatusCommand validation - null request");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getQueueItemStatusCommand.validateRequest(null)
        );

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception should indicate request cannot be null");

        log.info("✓ GetQueueItemStatusCommand correctly rejects null request: {}", exception.getMessage());
    }

    /**
     * Test 22: Verify GetQueueItemStatusCommand validation - null queue item ID.
     */
    @Test
    void testGetQueueItemStatusCommand_ValidationNullQueueItemId() {
        log.info("TEST: Verify GetQueueItemStatusCommand validation - null queue item ID");

        GetQueueItemStatusRequest request = GetQueueItemStatusRequest.builder()
                .queueItemId(null)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getQueueItemStatusCommand.validateRequest(request)
        );

        assertTrue(exception.getMessage().contains("cannot be null"),
                "Exception should indicate queue item ID cannot be null");

        log.info("✓ GetQueueItemStatusCommand correctly rejects null queue item ID: {}", exception.getMessage());
    }

    /**
     * Test 23: Execute GetQueueItemStatusCommand with existing queue item ID.
     * Requires UIPATH_TEST_EXISTING_QUEUE_ITEM_ID to be set.
     */
    @Test
    void testGetQueueItemStatusCommand_Execute() throws ExternalServiceException {
        if (existingQueueItemId == null) {
            log.warn("SKIPPED: UIPATH_TEST_EXISTING_QUEUE_ITEM_ID not set");
            return;
        }

        log.info("TEST: Execute GetQueueItemStatusCommand with ID: {}", existingQueueItemId);

        Long queueItemId = Long.parseLong(existingQueueItemId);
        GetQueueItemStatusRequest request = GetQueueItemStatusRequest.builder()
                .queueItemId(queueItemId)
                .build();

        UiPathQueueItem result = getQueueItemStatusCommand.execute(request);

        assertNotNull(result, "Queue item should not be null");
        assertEquals(queueItemId, result.getId(), "Queue item ID should match");

        log.info("✓ GetQueueItemStatusCommand successfully retrieved queue item:");
        log.info("  ID: {}", result.getId());
        log.info("  Reference: {}", result.getReference());
        log.info("  Status: {}", result.getStatus());
        log.info("  Is Finalized: {}", result.isFinalized());
        log.info("  Is Successful: {}", result.isSuccessful());
    }

    /**
     * Test 24: Execute GetQueueItemStatusCommand with non-existent queue item ID.
     */
    @Test
    void testGetQueueItemStatusCommand_Execute_NotFound() {
        log.info("TEST: Execute GetQueueItemStatusCommand with non-existent ID");

        Long nonExistentId = 999999999L;
        GetQueueItemStatusRequest request = GetQueueItemStatusRequest.builder()
                .queueItemId(nonExistentId)
                .build();

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> getQueueItemStatusCommand.execute(request)
        );

        assertEquals("QUEUE_ITEM_STATUS_CHECK_FAILED", exception.getErrorCode(),
                "Error code should be QUEUE_ITEM_STATUS_CHECK_FAILED");
        assertEquals("uipath", exception.getServiceName(),
                "Service name should be 'uipath'");

        log.info("✓ GetQueueItemStatusCommand correctly throws exception for non-existent ID:");
        log.info("  Error Code: {}", exception.getErrorCode());
        log.info("  Service Name: {}", exception.getServiceName());
        log.info("  Message: {}", exception.getMessage());
    }

    /**
     * Test 25: Execute AddQueueItemCommand to create a queue item.
     * Note: This test creates a real queue item in UiPath, use with caution.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "UIPATH_TEST_CREATE_QUEUE_ITEMS", matches = "true")
    void testAddQueueItemCommand_Execute() throws ExternalServiceException {
        log.info("TEST: Execute AddQueueItemCommand to create a queue item");

        // Generate a unique reference for this test
        String reference = testReferencePrefix + "-CMD-" + System.currentTimeMillis();

        // Build specific content for the queue item
        Map<String, Object> specificContent = new HashMap<>();
        specificContent.put("Project Key", reference);
        specificContent.put("Test Timestamp", System.currentTimeMillis());
        specificContent.put("Test Mode", "Command Integration Test");

        // Create the UiPath queue item request
        UiPathQueueItemRequest queueItemRequest = UiPathQueueItemRequest.builder()
                .queueName(testQueueName)
                .reference(reference)
                .priority("Normal")
                .specificContent(specificContent)
                .build();

        // Create the command request
        AddQueueItemRequest request = AddQueueItemRequest.builder()
                .queueItemRequest(queueItemRequest)
                .async(false)
                .build();

        log.info("Creating queue item via command:");
        log.info("  Queue Name: {}", testQueueName);
        log.info("  Reference: {}", reference);

        // Execute the command
        UiPathQueueItem result = addQueueItemCommand.execute(request);

        // Assertions
        assertNotNull(result, "Created queue item should not be null");
        assertNotNull(result.getId(), "Queue item ID should not be null");
        assertEquals(reference, result.getReference(), "Reference should match");

        log.info("✓ AddQueueItemCommand successfully created queue item:");
        log.info("  ID: {}", result.getId());
        log.info("  Reference: {}", result.getReference());
        log.info("  Status: {}", result.getStatus());
    }

    /**
     * Test 26: Verify AddQueueItemCommand handles service errors correctly.
     */
    @Test
    void testAddQueueItemCommand_Execute_ServiceError() {
        log.info("TEST: Verify AddQueueItemCommand handles service errors correctly");

        // Create a valid request that will fail due to invalid credentials/connectivity
        Map<String, Object> specificContent = new HashMap<>();
        specificContent.put("Test", "Error handling test");

        UiPathQueueItemRequest queueItemRequest = UiPathQueueItemRequest.builder()
                .queueName("NonExistentQueue")
                .reference("TEST-ERROR-" + System.currentTimeMillis())
                .specificContent(specificContent)
                .build();

        AddQueueItemRequest request = AddQueueItemRequest.builder()
                .queueItemRequest(queueItemRequest)
                .build();

        // This should throw an ExternalServiceException when it fails to connect
        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> addQueueItemCommand.execute(request)
        );

        assertEquals("QUEUE_ITEM_CREATION_FAILED", exception.getErrorCode(),
                "Error code should be QUEUE_ITEM_CREATION_FAILED");
        assertEquals("uipath", exception.getServiceName(),
                "Service name should be 'uipath'");
        assertEquals("addQueueItem", exception.getOperation(),
                "Operation should be 'addQueueItem'");

        log.info("✓ AddQueueItemCommand correctly handles service errors:");
        log.info("  Error Code: {}", exception.getErrorCode());
        log.info("  Service Name: {}", exception.getServiceName());
        log.info("  Operation: {}", exception.getOperation());
    }
}
