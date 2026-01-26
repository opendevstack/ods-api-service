package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ValidateConnectionCommand}.
 */
@ExtendWith(MockitoExtension.class)
class ValidateConnectionCommandTest {
    
    @Mock
    private OpenshiftService openshiftService;
    
    @InjectMocks
    private ValidateConnectionCommand command;
    
    private ValidateConnectionRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = ValidateConnectionRequest.builder()
                .instanceName("dev")
                .build();
    }
    
    @Test
    void execute_shouldReturnTrue_whenConnectionIsValid() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.validateConnection("dev")).thenReturn(true);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isTrue();
        verify(openshiftService).validateConnection("dev");
    }
    
    @Test
    void execute_shouldReturnFalse_whenConnectionIsInvalid() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.validateConnection("dev")).thenReturn(false);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isFalse();
        verify(openshiftService).validateConnection("dev");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenServiceFails() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.validateConnection("dev"))
                .thenThrow(new RuntimeException("Connection error"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to validate connection")
                .hasFieldOrPropertyWithValue("errorCode", "VALIDATE_CONNECTION_FAILED")
                .hasFieldOrPropertyWithValue("serviceName", "openshift")
                .hasFieldOrPropertyWithValue("operation", "validateConnection");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> command.validateRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request cannot be null");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenInstanceNameIsNull() {
        validRequest.setInstanceName(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Instance name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenInstanceNameIsEmpty() {
        validRequest.setInstanceName("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Instance name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenInstanceDoesNotExist() {
        when(openshiftService.hasInstance("dev")).thenReturn(false);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OpenShift instance 'dev' does not exist");
    }
    
    @Test
    void validateRequest_shouldPass_whenAllFieldsAreValid() {
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        
        // Should not throw exception
        command.validateRequest(validRequest);
        
        verify(openshiftService).hasInstance("dev");
    }
    
    @Test
    void getCommandName_shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("validate-connection");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("openshift");
    }
}
