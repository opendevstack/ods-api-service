package org.opendevstack.apiservice.externalservice.bitbucket.command.branch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GetDefaultBranchCommand}.
 */
@ExtendWith(MockitoExtension.class)
class GetDefaultBranchCommandTest {
    
    @Mock
    private BitbucketService bitbucketService;
    
    @InjectMocks
    private GetDefaultBranchCommand command;
    
    private GetDefaultBranchRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = GetDefaultBranchRequest.builder()
                .instanceName("dev")
                .projectKey("PROJ")
                .repositorySlug("my-repo")
                .build();
    }
    
    @Test
    void execute_shouldReturnDefaultBranch_whenSuccessful() throws Exception {
        // Given
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        when(bitbucketService.getDefaultBranch("dev", "PROJ", "my-repo")).thenReturn("main");
        
        // When
        String result = command.execute(validRequest);
        
        // Then
        assertThat(result).isEqualTo("main");
        verify(bitbucketService).getDefaultBranch("dev", "PROJ", "my-repo");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenBitbucketServiceFails() throws Exception {
        // Given
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        when(bitbucketService.getDefaultBranch("dev", "PROJ", "my-repo"))
                .thenThrow(new BitbucketException("Connection failed"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to get default branch")
                .hasFieldOrPropertyWithValue("errorCode", "GET_DEFAULT_BRANCH_FAILED")
                .hasFieldOrPropertyWithValue("serviceName", "bitbucket")
                .hasFieldOrPropertyWithValue("operation", "getDefaultBranch");
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
        validRequest.setInstanceName("");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Instance name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenInstanceDoesNotExist() {
        when(bitbucketService.hasInstance("dev")).thenReturn(false);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bitbucket instance 'dev' does not exist");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenProjectKeyIsNull() {
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        validRequest.setProjectKey(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project key cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenProjectKeyIsEmpty() {
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        validRequest.setProjectKey("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project key cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenRepositorySlugIsNull() {
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        validRequest.setRepositorySlug(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Repository slug cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenRepositorySlugIsEmpty() {
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        validRequest.setRepositorySlug("");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Repository slug cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldPass_whenAllFieldsAreValid() {
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        
        // Should not throw exception
        command.validateRequest(validRequest);
        
        verify(bitbucketService).hasInstance("dev");
    }
    
    @Test
    void getCommandName_shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("get-default-branch");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("bitbucket");
    }
}
