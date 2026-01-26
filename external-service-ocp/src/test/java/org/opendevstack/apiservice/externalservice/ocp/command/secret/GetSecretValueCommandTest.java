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
 * Unit tests for {@link GetSecretValueCommand}.
 */
@ExtendWith(MockitoExtension.class)
class GetSecretValueCommandTest {
    
    @Mock
    private OpenshiftService openshiftService;
    
    @InjectMocks
    private GetSecretValueCommand command;
    
    private GetSecretValueRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = GetSecretValueRequest.builder()
                .instanceName("dev")
                .secretName("my-secret")
                .key("password")
                .build();
    }
    
    @Test
    void execute_shouldReturnSecretValue_whenSuccessfulWithoutNamespace() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.getSecretValue("dev", "my-secret", "password"))
                .thenReturn("secret123");
        
        // When
        String result = command.execute(validRequest);
        
        // Then
        assertThat(result).isEqualTo("secret123");
        verify(openshiftService).getSecretValue("dev", "my-secret", "password");
    }
    
    @Test
    void execute_shouldReturnSecretValue_whenSuccessfulWithNamespace() throws Exception {
        // Given
        validRequest.setNamespace("custom-ns");
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.getSecretValue("dev", "my-secret", "password", "custom-ns"))
                .thenReturn("secret123");
        
        // When
        String result = command.execute(validRequest);
        
        // Then
        assertThat(result).isEqualTo("secret123");
        verify(openshiftService).getSecretValue("dev", "my-secret", "password", "custom-ns");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenOpenshiftServiceFails() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.getSecretValue("dev", "my-secret", "password"))
                .thenThrow(new OpenshiftException("Key not found in secret"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to retrieve secret value")
                .hasFieldOrPropertyWithValue("errorCode", "GET_SECRET_VALUE_FAILED")
                .hasFieldOrPropertyWithValue("serviceName", "openshift")
                .hasFieldOrPropertyWithValue("operation", "getSecretValue");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenUnexpectedErrorOccurs() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.getSecretValue("dev", "my-secret", "password"))
                .thenThrow(new RuntimeException("Network error"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to retrieve secret value");
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
    void validateRequest_shouldThrowException_whenKeyIsNull() {
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        validRequest.setKey(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenKeyIsEmpty() {
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        validRequest.setKey("   ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key cannot be null or empty");
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
        assertThat(command.getCommandName()).isEqualTo("get-secret-value");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("openshift");
    }
}
