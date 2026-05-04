package org.opendevstack.apiservice.externalservice.aap.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of AutomationPlatformService for Ansible Automation Platform.
 * Provides integration with Ansible AWX/Tower for executing workflows and modules.
 */
@Service("automationPlatformService")
@Slf4j
public class AnsibleAutomationPlatformService implements AutomationPlatformService {

    private final RestClient restClient;

    @Value("${automation.platform.ansible.base-url:http://localhost:8080/api/v2}")
    private String baseUrl;

    @Value("${automation.platform.ansible.username:admin}")
    private String username;

    @Value("${automation.platform.ansible.password:password}")
    private String password;

    public AnsibleAutomationPlatformService(RestClient aapRestClient) {
        this.restClient = aapRestClient;
    }

    @Override
    public AutomationExecutionResult executeWorkflow(String workflowName, Map<String, Object> parameters)
            throws AutomationPlatformException {
        log.info("Executing workflow '{}' with parameters: {}", workflowName, parameters);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("extra_vars", parameters);

            String encodedWorkflowName = UriUtils.encodePath(workflowName, StandardCharsets.UTF_8);
            String url = baseUrl + "/workflow_job_templates/" + encodedWorkflowName + "/launch/";

            Map responseBody = restClient.post()
                    .uri(url)
                    .headers(this::applyAuthHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (responseBody != null) {
                String jobId = String.valueOf(responseBody.get("id"));
                String status = String.valueOf(responseBody.get("status"));

                AutomationExecutionResult result =
                        new AutomationExecutionResult(jobId, status, true, "Workflow executed successfully");
                result.setMetadata(responseBody);

                log.info("Workflow '{}' executed successfully with job ID: {}", workflowName, jobId);
                return result;
            } else {
                throw new AutomationPlatformException.WorkflowExecutionException(
                        workflowName, "Empty response body");
            }

        } catch (RestClientException e) {
            log.error("Failed to execute workflow '{}': {}", workflowName, e.getMessage(), e);
            throw new AutomationPlatformException.WorkflowExecutionException(workflowName, e);
        }
    }

    @Override
    @Async
    public CompletableFuture<AutomationExecutionResult> executeWorkflowAsync(
            String workflowName, Map<String, Object> parameters) {
        try {
            AutomationExecutionResult result = executeWorkflow(workflowName, parameters);
            return CompletableFuture.completedFuture(result);
        } catch (AutomationPlatformException e) {
            log.error("Async workflow execution failed: {}", e.getMessage(), e);
            AutomationExecutionResult errorResult = AutomationExecutionResult.failure(
                    UUID.randomUUID().toString(),
                    "Async execution failed: " + e.getMessage(),
                    e.getErrorCode());
            return CompletableFuture.completedFuture(errorResult);
        }
    }

    @Override
    public AutomationJobStatus getJobStatus(String jobId) throws AutomationPlatformException {
        log.debug("Checking status for job ID: {}", jobId);
        String encodedJobId = UriUtils.encodePath(jobId, StandardCharsets.UTF_8);
        String url = baseUrl + "/jobs/" + encodedJobId + "/";
        return fetchJobStatus(jobId, url);
    }

    @Override
    public AutomationJobStatus getWorkflowJobStatus(String workflowId) throws AutomationPlatformException {
        log.debug("Checking workflow status for job ID: {}", workflowId);
        String encodedWorkflowId = UriUtils.encodePath(workflowId, StandardCharsets.UTF_8);
        String url = baseUrl + "/workflow_jobs/" + encodedWorkflowId + "/";
        return fetchJobStatus(workflowId, url);
    }

    private AutomationJobStatus fetchJobStatus(String jobId, String url) throws AutomationPlatformException {
        try {
            Map responseBody = restClient.get()
                    .uri(url)
                    .headers(this::applyAuthHeaders)
                    .retrieve()
                    .body(Map.class);

            if (responseBody != null) {
                AutomationJobStatus status = new AutomationJobStatus();
                status.setJobId(jobId);
                status.setStatus(parseJobStatus(String.valueOf(responseBody.get("status"))));
                status.setStatusMessage(String.valueOf(responseBody.get("result_traceback")));
                status.setResult(responseBody);
                return status;
            } else {
                throw new AutomationPlatformException.JobNotFoundException(jobId);
            }
        } catch (RestClientException e) {
            log.debug("Job not found at {}: {}", url, e.getMessage());
            throw new AutomationPlatformException.JobNotFoundException(jobId);
        }
    }

    @Override
    public boolean validateConnection() {
        try {
            restClient.get()
                    .uri(baseUrl + "/ping/")
                    .headers(this::applyAuthHeaders)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Connection validation: successful");
            return true;
        } catch (Exception e) {
            log.warn("Connection validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return validateConnection();
        } catch (Exception e) {
            log.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    private void applyAuthHeaders(HttpHeaders headers) {
        headers.setBasicAuth(username, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private AutomationJobStatus.Status parseJobStatus(String status) {
        if (status == null) {
            return AutomationJobStatus.Status.PENDING;
        }
        return switch (status.toLowerCase()) {
            case "pending"              -> AutomationJobStatus.Status.PENDING;
            case "running"              -> AutomationJobStatus.Status.RUNNING;
            case "successful"           -> AutomationJobStatus.Status.SUCCESSFUL;
            case "failed"               -> AutomationJobStatus.Status.FAILED;
            case "canceled", "cancelled"-> AutomationJobStatus.Status.CANCELLED;
            default                     -> AutomationJobStatus.Status.ERROR;
        };
    }
}
