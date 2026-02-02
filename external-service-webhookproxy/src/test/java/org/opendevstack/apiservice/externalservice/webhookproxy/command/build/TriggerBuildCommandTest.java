package org.opendevstack.apiservice.externalservice.webhookproxy.command.build;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.EnvPair;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildRequest;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.WebhookProxyService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TriggerBuildCommand}.
 */
@ExtendWith(MockitoExtension.class)
class TriggerBuildCommandTest {
    
    @Mock
    private WebhookProxyService webhookProxyService;
    
    @InjectMocks
    private TriggerBuildCommand command;
    
    private TriggerBuildRequest validRequest;
    private WebhookProxyBuildResponse successResponse;
    private WebhookProxyBuildResponse failureResponse;
    
    @BeforeEach
    void setUp() {
        validRequest = TriggerBuildRequest.builder()
                .clusterName("cluster-a")
                .projectKey("example-project")
                .branch("master")
                .repository("ods-project-quickstarters")
                .project("opendevstack")
                .triggerSecret("my-secret")
                .build();
        
        successResponse = new WebhookProxyBuildResponse();
        successResponse.setStatusCode(200);
        successResponse.setSuccess(true);
        successResponse.setBody("{\"status\": \"triggered\"}");
        
        failureResponse = new WebhookProxyBuildResponse();
        failureResponse.setStatusCode(500);
        failureResponse.setSuccess(false);
        failureResponse.setErrorMessage("Internal server error");
    }
    
    // ========== Successful Execution Tests ==========
    
    @Test
    void execute_shouldReturnSuccessResponse_whenBuildTriggeredSuccessfully() throws Exception {
        // Given
        when(webhookProxyService.triggerBuild(
                eq("cluster-a"),
                eq("example-project"),
                any(WebhookProxyBuildRequest.class),
                eq("my-secret"),
                isNull(),
                isNull()
        )).thenReturn(successResponse);
        
        // When
        WebhookProxyBuildResponse result = command.execute(validRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(200);
        
        verify(webhookProxyService).triggerBuild(
                eq("cluster-a"),
                eq("example-project"),
                any(WebhookProxyBuildRequest.class),
                eq("my-secret"),
                isNull(),
                isNull()
        );
    }
    
    @Test
    void execute_shouldPassJenkinsfilePathAndComponent_whenProvided() throws Exception {
        // Given
        validRequest.setJenkinsfilePath("custom/Jenkinsfile");
        validRequest.setComponent("my-component");
        
        when(webhookProxyService.triggerBuild(
                eq("cluster-a"),
                eq("example-project"),
                any(WebhookProxyBuildRequest.class),
                eq("my-secret"),
                eq("custom/Jenkinsfile"),
                eq("my-component")
        )).thenReturn(successResponse);
        
        // When
        WebhookProxyBuildResponse result = command.execute(validRequest);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        verify(webhookProxyService).triggerBuild(
                eq("cluster-a"),
                eq("example-project"),
                any(WebhookProxyBuildRequest.class),
                eq("my-secret"),
                eq("custom/Jenkinsfile"),
                eq("my-component")
        );
    }
    
    @Test
    void execute_shouldPassEnvironmentVariables_whenProvided() throws Exception {
        // Given
        List<EnvPair> envVars = new ArrayList<>();
        envVars.add(new EnvPair("RELEASE_VERSION", "1.0.0"));
        envVars.add(new EnvPair("ENVIRONMENT", "DEV"));
        validRequest.setEnv(envVars);
        
        when(webhookProxyService.triggerBuild(
                anyString(),
                anyString(),
                argThat(req -> req.getEnv() != null && req.getEnv().size() == 2),
                anyString(),
                any(),
                any()
        )).thenReturn(successResponse);
        
        // When
        WebhookProxyBuildResponse result = command.execute(validRequest);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
    }
    
    @Test
    void execute_shouldReturnFailureResponse_whenBuildTriggerFails() throws Exception {
        // Given
        when(webhookProxyService.triggerBuild(
                anyString(),
                anyString(),
                any(WebhookProxyBuildRequest.class),
                anyString(),
                any(),
                any()
        )).thenReturn(failureResponse);
        
        // When
        WebhookProxyBuildResponse result = command.execute(validRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getStatusCode()).isEqualTo(500);
    }
    
    // ========== Exception Handling Tests ==========
    
    @Test
    void execute_shouldThrowExternalServiceException_whenWebhookProxyExceptionOccurs() throws Exception {
        // Given
        when(webhookProxyService.triggerBuild(
                anyString(),
                anyString(),
                any(WebhookProxyBuildRequest.class),
                anyString(),
                any(),
                any()
        )).thenThrow(new WebhookProxyException("Connection failed"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Failed to trigger build")
                .hasMessageContaining("Connection failed")
                .hasFieldOrPropertyWithValue("errorCode", "TRIGGER_BUILD_FAILED")
                .hasFieldOrPropertyWithValue("serviceName", "webhookproxy")
                .hasFieldOrPropertyWithValue("operation", "trigger-build");
    }
    
    @Test
    void execute_shouldThrowExternalServiceException_whenUnexpectedExceptionOccurs() throws Exception {
        // Given
        when(webhookProxyService.triggerBuild(
                anyString(),
                anyString(),
                any(WebhookProxyBuildRequest.class),
                anyString(),
                any(),
                any()
        )).thenThrow(new RuntimeException("Unexpected error"));
        
        // When/Then
        assertThatThrownBy(() -> command.execute(validRequest))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Unexpected error triggering build")
                .hasFieldOrPropertyWithValue("errorCode", "TRIGGER_BUILD_ERROR")
                .hasFieldOrPropertyWithValue("serviceName", "webhookproxy")
                .hasFieldOrPropertyWithValue("operation", "trigger-build");
    }
    
    // ========== Validation Tests ==========
    
    @Test
    void validateRequest_shouldThrowException_whenRequestIsNull() {
        assertThatThrownBy(() -> command.validateRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request cannot be null");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenClusterNameIsNull() {
        validRequest.setClusterName(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cluster name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenClusterNameIsEmpty() {
        validRequest.setClusterName("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cluster name cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenProjectKeyIsNull() {
        validRequest.setProjectKey(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project key cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenProjectKeyIsEmpty() {
        validRequest.setProjectKey("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project key cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenBranchIsNull() {
        validRequest.setBranch(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenBranchIsEmpty() {
        validRequest.setBranch("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Branch cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenRepositoryIsNull() {
        validRequest.setRepository(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Repository cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenRepositoryIsEmpty() {
        validRequest.setRepository("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Repository cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenProjectIsNull() {
        validRequest.setProject(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project (Bitbucket project key) cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenProjectIsEmpty() {
        validRequest.setProject("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project (Bitbucket project key) cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenTriggerSecretIsNull() {
        validRequest.setTriggerSecret(null);
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trigger secret cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldThrowException_whenTriggerSecretIsEmpty() {
        validRequest.setTriggerSecret("  ");
        
        assertThatThrownBy(() -> command.validateRequest(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trigger secret cannot be null or empty");
    }
    
    @Test
    void validateRequest_shouldPass_whenAllRequiredFieldsAreValid() {
        // Should not throw exception
        command.validateRequest(validRequest);
    }
    
    @Test
    void validateRequest_shouldPass_whenOptionalFieldsAreNull() {
        // Jenkinsfile path and component are optional
        validRequest.setJenkinsfilePath(null);
        validRequest.setComponent(null);
        
        // Should not throw exception
        command.validateRequest(validRequest);
    }
    
    // ========== Command Metadata Tests ==========
    
    @Test
    void getCommandName_shouldReturnCorrectCommandName() {
        assertThat(command.getCommandName()).isEqualTo("trigger-build");
    }
    
    @Test
    void getServiceName_shouldReturnCorrectServiceName() {
        assertThat(command.getServiceName()).isEqualTo("webhookproxy");
    }
    
    // ========== Request DTO Tests ==========
    
    @Test
    void triggerBuildRequest_addEnv_shouldAddEnvironmentVariable() {
        TriggerBuildRequest request = TriggerBuildRequest.builder()
                .clusterName("cluster-a")
                .projectKey("test-project")
                .branch("master")
                .repository("test-repo")
                .project("test")
                .triggerSecret("secret")
                .build();
        
        request.addEnv("KEY1", "value1");
        request.addEnv("KEY2", "value2");
        
        assertThat(request.getEnv()).hasSize(2);
        assertThat(request.getEnv().get(0).getName()).isEqualTo("KEY1");
        assertThat(request.getEnv().get(0).getValue()).isEqualTo("value1");
        assertThat(request.getEnv().get(1).getName()).isEqualTo("KEY2");
        assertThat(request.getEnv().get(1).getValue()).isEqualTo("value2");
    }
    
    @Test
    void triggerBuildRequest_addEnv_shouldInitializeEnvListIfNull() {
        TriggerBuildRequest request = new TriggerBuildRequest();
        request.setEnv(null);
        
        request.addEnv("KEY", "value");
        
        assertThat(request.getEnv()).hasSize(1);
    }
}
