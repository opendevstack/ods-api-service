package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GetAvailableInstancesCommand}.
 */
@ExtendWith(MockitoExtension.class)
class GetAvailableInstancesCommandTest {
    
    @Mock
    private OpenshiftService openshiftService;
    
    @InjectMocks
    private GetAvailableInstancesCommand command;
    
    private GetAvailableInstancesRequest validRequest;
    private Set<String> sampleInstances;
    
    @BeforeEach
    void setUp() {
        validRequest = GetAvailableInstancesRequest.builder().build();
        
        sampleInstances = new HashSet<>();
        sampleInstances.add("dev");
        sampleInstances.add("staging");
        sampleInstances.add("prod");
    }
    
    @Test
    void execute_shouldReturnAllInstances_whenSuccessful() throws Exception {
        // Given
        when(openshiftService.getAvailableInstances()).thenReturn(sampleInstances);
        
        // When
        Set<String> result = command.execute(validRequest);
        
        // Then
        assertThat(result).isEqualTo(sampleInstances);
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder("dev", "staging", "prod");
        verify(openshiftService).getAvailableInstances();
    }
    
    @Test
    void execute_shouldReturnEmptySet_whenNoInstancesConfigured() throws Exception {
        // Given
        when(openshiftService.getAvailableInstances()).thenReturn(new HashSet<>());
        
        // When
        Set<String> result = command.execute(validRequest);
        
        // Then
        assertThat(result).isEmpty();
        verify(openshiftService).getAvailableInstances();
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenServiceFails() throws Exception {
        // Given
        when(openshiftService.getAvailableInstances())
                .thenThrow(new RuntimeException("Configuration error"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to retrieve available instances")
                .hasFieldOrPropertyWithValue("errorCode", "GET_AVAILABLE_INSTANCES_FAILED")
                .hasFieldOrPropertyWithValue("serviceName", "openshift")
                .hasFieldOrPropertyWithValue("operation", "getAvailableInstances");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> command.validateRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request cannot be null");
    }
    
    @Test
    void validateRequest_shouldPass_whenRequestIsValid() {
        // Should not throw exception
        command.validateRequest(validRequest);
    }
    
    @Test
    void getCommandName_shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("get-available-instances");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("openshift");
    }
}
