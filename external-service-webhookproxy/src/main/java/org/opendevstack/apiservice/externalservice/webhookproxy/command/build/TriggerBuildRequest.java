package org.opendevstack.apiservice.externalservice.webhookproxy.command.build;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opendevstack.apiservice.externalservice.webhookproxy.dto.EnvPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for triggering a build via the Webhook Proxy.
 * Encapsulates all parameters needed to trigger a release manager build.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerBuildRequest {
    
    /**
     * Name of the target cluster (e.g., "cluster-a", "cluster-b").
     * Required.
     */
    private String clusterName;
    
    /**
     * Project key (e.g., "example-project").
     * Required.
     */
    private String projectKey;
    
    /**
     * Git branch to build (e.g., "master", "develop", "feature/my-feature").
     * Required.
     */
    private String branch;
    
    /**
     * Repository name (e.g., "ods-project-quickstarters").
     * Required.
     */
    private String repository;
    
    /**
     * Bitbucket project key (e.g., "opendevstack").
     * Required.
     */
    private String project;
    
    /**
     * Secret required to authenticate with the webhook proxy.
     * Required.
     */
    private String triggerSecret;
    
    /**
     * Optional custom Jenkinsfile path (uses cluster default if null).
     */
    private String jenkinsfilePath;
    
    /**
     * Optional component name (extracted from repository if null).
     */
    private String component;
    
    /**
     * Environment variables to pass to the build pipeline.
     */
    @Builder.Default
    private List<EnvPair> env = new ArrayList<>();
    
    /**
     * Add an environment variable to the request.
     * 
     * @param name Variable name
     * @param value Variable value
     * @return This request for method chaining
     */
    public TriggerBuildRequest addEnv(String name, String value) {
        if (this.env == null) {
            this.env = new ArrayList<>();
        }
        this.env.add(new EnvPair(name, value));
        return this;
    }
}
