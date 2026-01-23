package org.opendevstack.apiservice.externalservice.bitbucket.command.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for checking if a branch exists in a Bitbucket repository.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchExistsRequest {
    
    /**
     * The name of the Bitbucket instance to use.
     */
    private String instanceName;
    
    /**
     * The project key in Bitbucket.
     */
    private String projectKey;
    
    /**
     * The repository slug in Bitbucket.
     */
    private String repositorySlug;
    
    /**
     * The branch name to check for existence.
     */
    private String branchName;
}
