package org.opendevstack.apiservice.externalservice.webhookproxy.command.build;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildRequest;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.WebhookProxyBuildResponse;
import org.opendevstack.apiservice.externalservice.webhookproxy.exception.WebhookProxyException;
import org.opendevstack.apiservice.externalservice.webhookproxy.service.WebhookProxyService;
import org.springframework.stereotype.Component;

/**
 * Command for triggering a build via the Webhook Proxy.
 * Wraps the WebhookProxyService.triggerBuild() operation following the command pattern.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerBuildCommand implements ExternalServiceCommand<TriggerBuildRequest, WebhookProxyBuildResponse> {
    
    private final WebhookProxyService webhookProxyService;
    
    @Override
    public WebhookProxyBuildResponse execute(TriggerBuildRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            log.info("Executing trigger build command: cluster='{}', project='{}', branch='{}', repository='{}'",
                    request.getClusterName(), request.getProjectKey(), request.getBranch(), request.getRepository());
            
            // Build the WebhookProxyBuildRequest from the command request
            WebhookProxyBuildRequest buildRequest = WebhookProxyBuildRequest.builder()
                    .branch(request.getBranch())
                    .repository(request.getRepository())
                    .project(request.getProject())
                    .env(request.getEnv())
                    .build();
            
            // Trigger the build
            WebhookProxyBuildResponse response = webhookProxyService.triggerBuild(
                    request.getClusterName(),
                    request.getProjectKey(),
                    buildRequest,
                    request.getTriggerSecret(),
                    request.getJenkinsfilePath(),
                    request.getComponent()
            );
            
            if (response.isSuccess()) {
                log.info("Build triggered successfully for project '{}' on cluster '{}'",
                        request.getProjectKey(), request.getClusterName());
            } else {
                log.warn("Build trigger returned non-success status for project '{}' on cluster '{}': {}",
                        request.getProjectKey(), request.getClusterName(), response.getStatusCode());
            }
            
            return response;
            
        } catch (WebhookProxyException e) {
            log.error("Failed to trigger build for project '{}' on cluster '{}': {}",
                    request.getProjectKey(), request.getClusterName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to trigger build: " + e.getMessage(),
                    e,
                    "TRIGGER_BUILD_FAILED",
                    getServiceName(),
                    getCommandName()
            );
        } catch (Exception e) {
            log.error("Unexpected error triggering build for project '{}' on cluster '{}': {}",
                    request.getProjectKey(), request.getClusterName(), e.getMessage(), e);
            throw new ExternalServiceException(
                    "Unexpected error triggering build: " + e.getMessage(),
                    e,
                    "TRIGGER_BUILD_ERROR",
                    getServiceName(),
                    getCommandName()
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "trigger-build";
    }
    
    @Override
    public String getServiceName() {
        return "webhookproxy";
    }
    
    @Override
    public void validateRequest(TriggerBuildRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getClusterName() == null || request.getClusterName().trim().isEmpty()) {
            throw new IllegalArgumentException("Cluster name cannot be null or empty");
        }
        if (request.getProjectKey() == null || request.getProjectKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Project key cannot be null or empty");
        }
        if (request.getBranch() == null || request.getBranch().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch cannot be null or empty");
        }
        if (request.getRepository() == null || request.getRepository().trim().isEmpty()) {
            throw new IllegalArgumentException("Repository cannot be null or empty");
        }
        if (request.getProject() == null || request.getProject().trim().isEmpty()) {
            throw new IllegalArgumentException("Project (Bitbucket project key) cannot be null or empty");
        }
        if (request.getTriggerSecret() == null || request.getTriggerSecret().trim().isEmpty()) {
            throw new IllegalArgumentException("Trigger secret cannot be null or empty");
        }
    }
}
