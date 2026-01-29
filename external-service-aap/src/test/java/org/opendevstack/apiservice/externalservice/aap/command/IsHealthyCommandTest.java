package org.opendevstack.apiservice.externalservice.aap.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IsHealthyCommand Tests")
class IsHealthyCommandTest {

    @Mock
    private AutomationPlatformService automationPlatformService;

    @InjectMocks
    private IsHealthyCommand command;

    @BeforeEach
    void setUp() {
        // No specific setup needed
    }

    @Test
    @DisplayName("Should return true when service is healthy")
    void execute_whenServiceHealthy_shouldReturnTrue() throws Exception {
        when(automationPlatformService.isHealthy()).thenReturn(true);

        Boolean result = command.execute(IsHealthyRequest.builder().build());

        assertTrue(result);
        verify(automationPlatformService).isHealthy();
    }

    @Test
    @DisplayName("Should return false when service is unhealthy")
    void execute_whenServiceUnhealthy_shouldReturnFalse() throws Exception {
        when(automationPlatformService.isHealthy()).thenReturn(false);

        Boolean result = command.execute(IsHealthyRequest.builder().build());

        assertFalse(result);
        verify(automationPlatformService).isHealthy();
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void execute_withNullRequest_shouldStillWork() throws Exception {
        when(automationPlatformService.isHealthy()).thenReturn(true);

        Boolean result = command.execute(null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when service throws exception (health check should not propagate exceptions)")
    void execute_whenServiceThrowsException_shouldReturnFalse() throws Exception {
        when(automationPlatformService.isHealthy()).thenThrow(new RuntimeException("Service unavailable"));

        Boolean result = command.execute(IsHealthyRequest.builder().build());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should include details when requested")
    void execute_withIncludeDetails_shouldLogDetails() throws Exception {
        when(automationPlatformService.isHealthy()).thenReturn(true);
        IsHealthyRequest request = IsHealthyRequest.builder()
                .includeDetails(true)
                .build();

        Boolean result = command.execute(request);

        assertTrue(result);
        verify(automationPlatformService).isHealthy();
    }

    @Test
    @DisplayName("Should return correct command name")
    void getCommandName_shouldReturnCorrectName() {
        assertEquals("is-healthy", command.getCommandName());
    }

    @Test
    @DisplayName("Should return correct service name")
    void getServiceName_shouldReturnCorrectName() {
        assertEquals("aap", command.getServiceName());
    }
}
