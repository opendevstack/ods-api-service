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
 * Unit tests for {@link HasInstanceCommand}.
 */
@ExtendWith(MockitoExtension.class)
class HasInstanceCommandTest {
    
    @Mock
    private OpenshiftService openshiftService;
    
    @InjectMocks
    private HasInstanceCommand command;
    
    private HasInstanceRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = HasInstanceRequest.builder()
                .instanceName("dev")
                .build();
    }
    
    @Test
    void execute_shouldReturnTrue_whenInstanceExists() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isTrue();
        verify(openshiftService).hasInstance("dev");
    }
    
    @Test
    void execute_shouldReturnFalse_whenInstanceDoesNotExist() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(false);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isFalse();
        verify(openshiftService).hasInstance("dev");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenServiceFails() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev"))
                .thenThrow(new RuntimeException("Configuration error"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to check instance existence")
                .hasFieldOrPropertyWithValue("errorCode", "HAS_INSTANCE_FAILED")
                .hasFieldOrPropertyWithValue("serviceName", "openshift")
                .hasFieldOrPropertyWithValue("operation", "hasInstance");
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
    void validateRequest_shouldPass_whenAllFieldsAreValid() {
        // Should not throw exception
        command.validateRequest(validRequest);
    }
    
    @Test
    void getCommandName_shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("has-instance");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("openshift");
    }
}
