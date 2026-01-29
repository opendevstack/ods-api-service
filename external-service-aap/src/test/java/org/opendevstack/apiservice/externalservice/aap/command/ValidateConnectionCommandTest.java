package org.opendevstack.apiservice.externalservice.aap.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidateConnectionCommand Tests")
class ValidateConnectionCommandTest {

    @Mock
    private AutomationPlatformService automationPlatformService;

    @InjectMocks
    private ValidateConnectionCommand command;

    @BeforeEach
    void setUp() {
        // No specific setup needed
    }

    @Test
    @DisplayName("Should return true when connection is valid")
    void execute_whenConnectionValid_shouldReturnTrue() throws Exception {
        when(automationPlatformService.validateConnection()).thenReturn(true);

        Boolean result = command.execute(ValidateConnectionRequest.builder().build());

        assertTrue(result);
        verify(automationPlatformService).validateConnection();
    }

    @Test
    @DisplayName("Should return false when connection is invalid")
    void execute_whenConnectionInvalid_shouldReturnFalse() throws Exception {
        when(automationPlatformService.validateConnection()).thenReturn(false);

        Boolean result = command.execute(ValidateConnectionRequest.builder().build());

        assertFalse(result);
        verify(automationPlatformService).validateConnection();
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void execute_withNullRequest_shouldStillWork() throws Exception {
        when(automationPlatformService.validateConnection()).thenReturn(true);

        Boolean result = command.execute(null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should wrap exception when service throws")
    void execute_whenServiceThrowsException_shouldWrapException() {
        when(automationPlatformService.validateConnection())
                .thenThrow(new RuntimeException("Connection failed"));

        ExternalServiceException exception = assertThrows(ExternalServiceException.class, 
                () -> command.execute(ValidateConnectionRequest.builder().build()));
        
        assertEquals("CONNECTION_VALIDATION_FAILED", exception.getErrorCode());
        assertEquals("aap", exception.getServiceName());
    }

    @Test
    @DisplayName("Should return correct command name")
    void getCommandName_shouldReturnCorrectName() {
        assertEquals("validate-connection", command.getCommandName());
    }

    @Test
    @DisplayName("Should return correct service name")
    void getServiceName_shouldReturnCorrectName() {
        assertEquals("aap", command.getServiceName());
    }
}
