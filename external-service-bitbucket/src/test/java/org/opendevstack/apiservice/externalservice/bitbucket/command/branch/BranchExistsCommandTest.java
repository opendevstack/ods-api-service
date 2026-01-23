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
 * Unit tests for {@link BranchExistsCommand}.
 */
@ExtendWith(MockitoExtension.class)
class BranchExistsCommandTest {
    
    @Mock
    private BitbucketService bitbucketService;
    
    @InjectMocks
    private BranchExistsCommand command;
    
    private BranchExistsRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = BranchExistsRequest.builder()
                .instanceName("dev")
                .projectKey("PROJ")
                .repositorySlug("my-repo")
                .branchName("feature/test-branch")
                .build();
    }
    
    @Test
    void execute_shouldReturnTrue_whenBranchExists() throws Exception {
        // Given
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        when(bitbucketService.branchExists("dev", "PROJ", "my-repo", "feature/test-branch")).thenReturn(true);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isTrue();
        verify(bitbucketService).branchExists("dev", "PROJ", "my-repo", "feature/test-branch");
    }
    
    @Test
    void execute_shouldReturnFalse_whenBranchDoesNotExist() throws Exception {
        // Given
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        when(bitbucketService.branchExists("dev", "PROJ", "my-repo", "feature/test-branch")).thenReturn(false);
        
        // When
        Boolean result = command.execute(validRequest);
        
        // Then
        assertThat(result).isFalse();
        verify(bitbucketService).branchExists("dev", "PROJ", "my-repo", "feature/test-branch");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenBitbucketServiceFails() throws Exception {
        // Given
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        when(bitbucketService.branchExists("dev", "PROJ", "my-repo", "feature/test-branch"))
                .thenThrow(new BitbucketException("Connection failed"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to check branch existence")
                .hasFieldOrPropertyWithValue("errorCode", "BRANCH_EXISTS_CHECK_FAILED")
                .hasFieldOrPropertyWithValue("serviceName", "bitbucket")
                .hasFieldOrPropertyWithValue("operation", "branchExists");
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
    void validateRequest_shouldThrowException_whenBranchNameIsNull() {
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        validRequest.setBranchName(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenBranchNameIsEmpty() {
        when(bitbucketService.hasInstance("dev")).thenReturn(true);
        validRequest.setBranchName("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch name cannot be null or empty");
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
        assertThat(command.getCommandName()).isEqualTo("branch-exists");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("bitbucket");
    }
}
