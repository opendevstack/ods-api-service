package org.opendevstack.apiservice.externalservice.ocp.command.secret;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SecretExistsCommand}.
 */
@ExtendWith(MockitoExtension.class)
class SecretExistsCommandTest {
    
    @Mock
    private OpenshiftService openshiftService;
    
    @InjectMocks
    private SecretExistsCommand command;
    
    private SecretExistsRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = SecretExistsRequest.builder()
                .instanceName("dev")
                .secretName("my-secret")
                .build();
    }
    
    @Test
    void execute_shouldReturnTrue_whenSecretExistsWithoutNamespace() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.secretExists("dev", "my-secret")).thenReturn(true);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isTrue();
        verify(openshiftService).secretExists("dev", "my-secret");
    }
    
    @Test
    void execute_shouldReturnFalse_whenSecretDoesNotExistWithoutNamespace() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.secretExists("dev", "my-secret")).thenReturn(false);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isFalse();
        verify(openshiftService).secretExists("dev", "my-secret");
    }
    
    @Test
    void execute_shouldReturnTrue_whenSecretExistsWithNamespace() throws Exception {
        // Given
        validRequest.setNamespace("custom-ns");
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.secretExists("dev", "my-secret", "custom-ns")).thenReturn(true);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isTrue();
        verify(openshiftService).secretExists("dev", "my-secret", "custom-ns");
    }
    
    @Test
    void execute_shouldReturnFalse_whenSecretDoesNotExistWithNamespace() throws Exception {
        // Given
        validRequest.setNamespace("custom-ns");
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.secretExists("dev", "my-secret", "custom-ns")).thenReturn(false);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isFalse();
        verify(openshiftService).secretExists("dev", "my-secret", "custom-ns");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenUnexpectedErrorOccurs() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.secretExists("dev", "my-secret"))
                .thenThrow(new RuntimeException("Network error"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to check secret existence");
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
    void validateRequest_shouldThrowException_whenSecretNameIsNull() {
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        validRequest.setSecretName(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Secret name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenSecretNameIsEmpty() {
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        validRequest.setSecretName("");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Secret name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldPass_whenAllFieldsAreValid() {
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        
        // Should not throw exception
        command.validateRequest(validRequest);
        
        verify(openshiftService).hasInstance("dev");
    }
    
    @Test
    void validateRequest_shouldPass_whenNamespaceIsProvided() {
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        validRequest.setNamespace("custom-ns");
        
        // Should not throw exception
        command.validateRequest(validRequest);
        
        verify(openshiftService).hasInstance("dev");
    }
    
    @Test
    void getCommandName_shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("secret-exists");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("openshift");
    }
}
