package org.opendevstack.apiservice.externalservice.bitbucket.command.branch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.springframework.stereotype.Component;

/**
 * Command to check if a branch exists in a Bitbucket repository.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BranchExistsCommand implements ExternalServiceCommand<BranchExistsRequest, Boolean> {
    
    private final BitbucketService bitbucketService;
    
    @Override
    public Boolean execute(BranchExistsRequest request) throws ExternalServiceException {
        try {
            validateRequest(request);
            log.info("Checking if branch '{}' exists in repository {}/{} on instance '{}'",
                    request.getBranchName(), request.getProjectKey(), request.getRepositorySlug(), request.getInstanceName());
            
            return bitbucketService.branchExists(
                    request.getInstanceName(),
                    request.getProjectKey(),
                    request.getRepositorySlug(),
                    request.getBranchName()
            );
        } catch (Exception e) {
            log.error("Failed to check if branch '{}' exists in repository {}/{} on instance '{}': {}",
                    request.getBranchName(), request.getProjectKey(), request.getRepositorySlug(), 
                    request.getInstanceName(), e.getMessage());
            throw new ExternalServiceException(
                    "Failed to check branch existence: " + e.getMessage(),
                    e,
                    "BRANCH_EXISTS_CHECK_FAILED",
                    "bitbucket",
                    "branchExists"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "branch-exists";
    }
    
    @Override
    public String getServiceName() {
        return "bitbucket";
    }
    
    @Override
    public void validateRequest(BranchExistsRequest request) {
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
        if (request.getBranchName() == null || request.getBranchName().trim().isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be null or empty");
        }
    }
}
