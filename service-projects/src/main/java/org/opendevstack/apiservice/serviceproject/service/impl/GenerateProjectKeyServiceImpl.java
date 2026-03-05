package org.opendevstack.apiservice.serviceproject.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Random;
import java.util.Set;

@Service
@Slf4j
public class GenerateProjectKeyServiceImpl implements GenerateProjectKeyService {

    private static final int MAX_RETRIES = 10;

    private final OpenshiftService openshiftService;
    
    private final BitbucketService bitbucketService;
    
    private final JiraService jiraService;
    
    private final Random random;
    
    @Autowired
    public GenerateProjectKeyServiceImpl(BitbucketService bitbucketService, JiraService jiraService, 
                                         OpenshiftService openshiftService) {
        this(bitbucketService, jiraService, openshiftService, new Random());
    }
    
    GenerateProjectKeyServiceImpl(BitbucketService bitbucketService, JiraService jiraService, 
                                  OpenshiftService openshiftService, Random random) {
        this.bitbucketService = bitbucketService;
        this.jiraService = jiraService;
        this.openshiftService = openshiftService;
        this.random = random;
    }

    @Override
    public String generateProjectKey(String projectKeyPattern) throws ProjectKeyGenerationException {
        String pattern = resolveProjectKeyPattern(projectKeyPattern);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            int randomNumber = random.nextInt(1_000_000);
            String projectKey = String.format(pattern, randomNumber);

            if (!isProjectFound(projectKey)) {
                log.debug("Generated unique project key '{}' on attempt {}", projectKey, attempt);
                return projectKey;
            }

            log.debug("Project key '{}' already exists (attempt {}/{})", projectKey, attempt, MAX_RETRIES);
        }

        throw new ProjectKeyGenerationException(
                String.format("Failed to generate unique project key after %d retries", MAX_RETRIES));
    }

    private String resolveProjectKeyPattern(String projectKeyPattern) {
        if (projectKeyPattern == null || projectKeyPattern.isBlank()) {
            return DEFAULT_PROJECT_KEY_PATTERN;
        }
        return projectKeyPattern;
    }
    
    private boolean isProjectFound(String projectKey) throws ProjectKeyGenerationException {
        try {
            if (existsInAnyBitbucketInstance(projectKey)) {
                return true;
            }

            if (existsInAnyJiraInstance(projectKey)) {
                return true;
            }

            if (existsInAnyOpenshift(projectKey)) {
                return true;
            }

            return false;
        } catch (BitbucketException e) {
            throw new ProjectKeyGenerationException(
                    String.format("Failed to check project '%s' in Bitbucket", projectKey), e);
        } catch (JiraException e) {
            throw new ProjectKeyGenerationException(
                    String.format("Failed to check project '%s' in Jira", projectKey), e);
        } catch (OpenshiftException e) {
            throw new ProjectKeyGenerationException(
                    String.format("Failed to check project '%s' in Openshift", projectKey), e);
        }
    }

    private boolean existsInAnyBitbucketInstance(String projectKey) throws BitbucketException {
        Set<String> instances = bitbucketService.getAvailableInstances();

        for (String instanceName : instances.stream().sorted(Comparator.naturalOrder()).toList()) {
            if (bitbucketService.projectExists(instanceName, projectKey)) {
                return true;
            }
        }

        return false;
    }

    private boolean existsInAnyJiraInstance(String projectKey) throws JiraException {
        Set<String> instances = jiraService.getAvailableInstances();

        if (instances == null || instances.isEmpty()) {
            return jiraService.projectExists(projectKey);
        }

        for (String instanceName : instances.stream().sorted(Comparator.naturalOrder()).toList()) {
            if (jiraService.projectExists(instanceName, projectKey)) {
                return true;
            }
        }

        return false;
    }
    
    private boolean existsInAnyOpenshift(String projectKey) throws OpenshiftException {
        Set<String> instances = openshiftService.getAvailableInstances();

        if (instances == null || instances.isEmpty()) {
            return false;
        }

        for (String instanceName : instances.stream().sorted(Comparator.naturalOrder()).toList()) {
            if (openshiftService.projectExists(instanceName, projectKey)) {
                return true;
            }
        }

        return false;
    }
}
