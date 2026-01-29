package org.opendevstack.apiservice.externalservice.aap.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorkflowJobStatusCommand Tests")
class GetWorkflowJobStatusCommandTest {

    @Mock
    private AutomationPlatformService automationPlatformService;

    @InjectMocks
    private GetWorkflowJobStatusCommand command;

    private GetWorkflowJobStatusRequest validRequest;
    private AutomationJobStatus expectedStatus;

    @BeforeEach
    void setUp() {
        validRequest = GetWorkflowJobStatusRequest.builder()
                .workflowId("workflow-123")
                .build();

        expectedStatus = new AutomationJobStatus();
        expectedStatus.setJobId("workflow-123");
        expectedStatus.setStatus(AutomationJobStatus.Status.RUNNING);
    }

    @Test
    @DisplayName("Should return workflow job status successfully")
    void execute_withValidRequest_shouldReturnStatus() throws Exception {
        when(automationPlatformService.getWorkflowJobStatus("workflow-123")).thenReturn(expectedStatus);

        AutomationJobStatus result = command.execute(validRequest);

        assertNotNull(result);
        assertEquals("workflow-123", result.getJobId());
        assertEquals(AutomationJobStatus.Status.RUNNING, result.getStatus());
        verify(automationPlatformService).getWorkflowJobStatus("workflow-123");
    }

    @Test
    @DisplayName("Should throw exception when request is null")
    void execute_withNullRequest_shouldThrowException() {
        assertThrows(ExternalServiceException.class, () -> command.execute(null));
    }

    @Test
    @DisplayName("Should throw exception when workflow ID is null")
    void execute_withNullWorkflowId_shouldThrowException() {
        GetWorkflowJobStatusRequest request = GetWorkflowJobStatusRequest.builder()
                .workflowId(null)
                .build();

        assertThrows(ExternalServiceException.class, () -> command.execute(request));
    }

    @Test
    @DisplayName("Should throw exception when workflow ID is empty")
    void execute_withEmptyWorkflowId_shouldThrowException() {
        GetWorkflowJobStatusRequest request = GetWorkflowJobStatusRequest.builder()
                .workflowId("")
                .build();

        assertThrows(ExternalServiceException.class, () -> command.execute(request));
    }

    @Test
    @DisplayName("Should wrap service exception in ExternalServiceException")
    void execute_whenServiceThrowsException_shouldWrapException() throws Exception {
        when(automationPlatformService.getWorkflowJobStatus("workflow-123"))
                .thenThrow(new AutomationPlatformException("Service error"));

        ExternalServiceException exception = assertThrows(ExternalServiceException.class, 
                () -> command.execute(validRequest));
        
        assertEquals("WORKFLOW_JOB_STATUS_CHECK_FAILED", exception.getErrorCode());
        assertEquals("aap", exception.getServiceName());
    }

    @Test
    @DisplayName("Should return correct command name")
    void getCommandName_shouldReturnCorrectName() {
        assertEquals("get-workflow-job-status", command.getCommandName());
    }

    @Test
    @DisplayName("Should return correct service name")
    void getServiceName_shouldReturnCorrectName() {
        assertEquals("aap", command.getServiceName());
    }
}
