package org.opendevstack.apiservice.externalservice.bitbucket.command.branch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.springframework.stereotype.Component;

/**
 * Command to get the default branch of a Bitbucket repository.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetDefaultBranchCommand implements ExternalServiceCommand<GetDefaultBranchRequest, String> {
    
    private final BitbucketService bitbucketService;
    
    @Override
    public String execute(GetDefaultBranchRequest request) throws ExternalServiceException {
        try {
            validateRequest(request);
            log.info("Getting default branch for repository {}/{} on instance '{}'",
                    request.getProjectKey(), request.getRepositorySlug(), request.getInstanceName());
            
            return bitbucketService.getDefaultBranch(
                    request.getInstanceName(),
                    request.getProjectKey(),
                    request.getRepositorySlug()
            );
        } catch (Exception e) {
            log.error("Failed to get default branch for repository {}/{} on instance '{}': {}",
                    request.getProjectKey(), request.getRepositorySlug(), request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to get default branch: " + e.getMessage(),
                    e,
                    "GET_DEFAULT_BRANCH_FAILED",
                    "bitbucket",
                    "getDefaultBranch"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "get-default-branch";
    }
    
    @Override
    public String getServiceName() {
        return "bitbucket";
    }
    
    @Override
    public void validateRequest(GetDefaultBranchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getInstanceName() == null || request.getInstanceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Instance name cannot be null or empty");
        }
        if (!bitbucketService.hasInstance(request.getInstanceName())) {
            throw new IllegalArgumentException("Bitbucket instance '" + request.getInstanceName() + "' does not exist");
        }
        if (request.getProjectKey() == null || request.getProjectKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Project key cannot be null or empty");
        }
        if (request.getRepositorySlug() == null || request.getRepositorySlug().trim().isEmpty()) {
            throw new IllegalArgumentException("Repository slug cannot be null or empty");
        }
    }
}
