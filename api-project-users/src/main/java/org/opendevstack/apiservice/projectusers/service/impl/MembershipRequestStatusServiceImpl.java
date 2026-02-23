package org.opendevstack.apiservice.projectusers.service.impl;

import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemResult;
import org.opendevstack.apiservice.externalservice.uipath.service.UiPathOrchestratorService;
import org.opendevstack.apiservice.projectusers.model.MembershipRequestStatusResponse;
import org.opendevstack.apiservice.projectusers.service.MembershipRequestStatusService;
import org.opendevstack.apiservice.projectusers.service.MembershipRequestTokenService;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Implementation of MembershipRequestStatusService that queries both Ansible
 * Automation Platform and UIPath Orchestrator for job status.
 * 
 * This service checks the status of both processes:
 * 1. Ansible Automation Platform workflow job
 * 2. UIPath queue item
 * 
 * The overall status is determined by checking both systems and ensuring both
 * have completed successfully.
 */
@Slf4j
@Service("membershipRequestStatusService")
public class MembershipRequestStatusServiceImpl implements MembershipRequestStatusService {

    private final MembershipRequestTokenService tokenService;
    private final AutomationPlatformService automationPlatformService;
    private final UiPathOrchestratorService uiPathService;

    public MembershipRequestStatusServiceImpl(MembershipRequestTokenService tokenService,
            AutomationPlatformService automationPlatformService,
            UiPathOrchestratorService uiPathService) {
        this.tokenService = tokenService;
        this.automationPlatformService = automationPlatformService;
        this.uiPathService = uiPathService;
    }

    @Override
    public MembershipRequestStatusResponse getRequestStatus(String requestId) {
        log.debug("Getting status for request ID: {}", requestId);

        // Decode and validate the request token
        Map<String, Object> tokenData = tokenService.decodeRequestToken(requestId);

        String jobId = (String) tokenData.get("jobId");
        String uipathReference = (String) tokenData.get("uipathReference");
        String projectKey = (String) tokenData.get("projectKey");
        String user = (String) tokenData.get("user");
        String environment = (String) tokenData.get("environment");

        try {
            // Step 1: Check Ansible Automation Platform status
            AutomationJobStatus ansibleStatus = automationPlatformService.getWorkflowJobStatus(jobId);
            log.debug("Ansible job '{}' status: {}", jobId, ansibleStatus.getStatus());

            // If AAP is not completed, return in-progress status
            if (!isAnsibleTerminalStatus(ansibleStatus.getStatus())) {
                log.debug("Ansible workflow still in progress");
                return createResponse(requestId, projectKey, user, environment,
                        MembershipRequestStatusResponse.StatusEnum.IN_PROGRESS,
                        false, false,
                        "Membership request is still being processed",
                        null);
            }

            // Step 2: If AAP has completed but failed, return failure status
            if (ansibleStatus.getStatus() != AutomationJobStatus.Status.SUCCESSFUL) {
                log.info("Ansible workflow completed with failure status: {}", ansibleStatus.getStatus());
                return createResponse(requestId, projectKey, user, environment,
                        MembershipRequestStatusResponse.StatusEnum.COMPLETED,
                        true, false,
                        "Ansible workflow failed: " + ansibleStatus.getStatusMessage(),
                        "Ansible status: " + ansibleStatus.getStatus());
            }

            // Step 3: AAP succeeded, now check UIPath status
            log.debug("Ansible workflow completed successfully, checking UIPath status");
            return checkUiPathAndCreateResponse(requestId, projectKey, user, environment, uipathReference);

        } catch (AutomationPlatformException e) {
            log.error("Failed to get job status for request '{}': {}", requestId, e.getMessage(), e);
            return createErrorResponse(requestId, projectKey, user, environment,
                    "Failed to retrieve request status", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while getting request status for '{}': {}", requestId, e.getMessage(), e);
            return createErrorResponse(requestId, projectKey, user, environment,
                    "Failed to retrieve request status", e.getMessage());
        }
    }

    /**
     * Check UIPath status and create appropriate response using the UIPath service.
     */
    private MembershipRequestStatusResponse checkUiPathAndCreateResponse(String requestId, String projectKey,
            String user, String environment,
            String uipathReference) {
        
        log.debug("Checking UIPath status for reference: '{}'", uipathReference);
        
        // Use the generic service method to check the queue item status
        UiPathQueueItemResult result = uiPathService.checkQueueItemByReference(uipathReference);
        
        // Map the result to the appropriate response
        return switch (result.getResultStatus()) {
            case NO_REFERENCE -> {
                log.debug("No UIPath reference provided, marking request as completed successfully");
                yield createSuccessResponse(requestId, projectKey, user, environment,
                        "Membership request completed");
            }
            case NOT_FOUND -> {
                log.warn("UIPath queue item not found for reference: '{}'", uipathReference);
                yield createErrorResponse(requestId, projectKey, user, environment,
                        result.getMessage(), result.getErrorDetails());
            }
            case IN_PROGRESS -> {
                log.debug("UIPath process is still in progress");
                yield createResponse(requestId, projectKey, user, environment,
                        MembershipRequestStatusResponse.StatusEnum.IN_PROGRESS,
                        false, false,
                        "Membership request is still being processed",
                        null);
            }
            case SUCCESS -> {
                log.debug("UIPath process completed successfully");
                yield createSuccessResponse(requestId, projectKey, user, environment,
                        "Membership request completed");
            }
            case FAILURE -> {
                log.warn("UIPath process failed");
                yield createErrorResponse(requestId, projectKey, user, environment,
                        result.getMessage(), result.getErrorDetails());
            }
            case ERROR -> {
                log.error("Error checking UIPath status: {}", result.getMessage());
                yield createErrorResponse(requestId, projectKey, user, environment,
                        result.getMessage(), result.getErrorDetails());
            }
        };
    }

    /**
     * Create success response.
     */
    private MembershipRequestStatusResponse createSuccessResponse(String requestId, String projectKey,
            String user, String environment, String message) {
        return createResponse(requestId, projectKey, user, environment,
                MembershipRequestStatusResponse.StatusEnum.COMPLETED,
                true, true,
                message,
                null);
    }

    /**
     * Create an error response.
     */
    private MembershipRequestStatusResponse createErrorResponse(String requestId, String projectKey,
            String user, String environment,
            String message, String errorDetails) {
        return createResponse(requestId, projectKey, user, environment,
                MembershipRequestStatusResponse.StatusEnum.COMPLETED,
                true, false,
                message,
                errorDetails);
    }

    /**
     * Generic response builder method that consolidates all response creation
     * logic.
     * This eliminates code duplication across different response types.
     */
    @SuppressWarnings("java:S107")
    private MembershipRequestStatusResponse createResponse(String requestId, String projectKey,
            String user, String environment,
            MembershipRequestStatusResponse.StatusEnum status,
            boolean completed, boolean successful,
            String message, String errorDetails) {
        MembershipRequestStatusResponse response = new MembershipRequestStatusResponse();
        response.setRequestId(requestId);
        response.setProject(projectKey);
        response.setUser(user);
        response.setEnvironment(environment);
        response.setStatus(status);
        response.setCompleted(completed);
        response.setSuccessful(successful);
        response.setMessage(message);

        if (errorDetails != null) {
            response.setErrorDetails(errorDetails);
        }

        return response;
    }

    private boolean isAnsibleTerminalStatus(AutomationJobStatus.Status status) {
        return status == AutomationJobStatus.Status.SUCCESSFUL ||
                status == AutomationJobStatus.Status.FAILED ||
                status == AutomationJobStatus.Status.CANCELLED ||
                status == AutomationJobStatus.Status.ERROR;
    }

    @Override
    public boolean validateRequestToken(String requestId, String projectKey, String user) {
        log.debug("Validating request token for requestId: {}, projectKey: {}, user: {}", requestId, projectKey, user);
        try {
            Map<String, Object> tokenData = tokenService.decodeRequestToken(requestId);
            String tokenProjectKey = (String) tokenData.get("projectKey");
            String tokenUser = (String) tokenData.get("user");

            boolean isValid = projectKey.equals(tokenProjectKey) && user.equals(tokenUser);
            if (!isValid) {
                log.warn("Request token validation failed: projectKey or user does not match");
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error validating request token: {}", e.getMessage(), e);
            return false;
        }
    }

}