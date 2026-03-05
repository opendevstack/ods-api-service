package org.opendevstack.apiservice.serviceproject.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.serviceproject.model.CreateProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final OpenshiftService openshiftService;
    
    private final BitbucketService bitbucketService;
    
    private final JiraService jiraService;
    
    private final GenerateProjectKeyService generateProjectKeyService;
    
    @Autowired
    public ProjectServiceImpl(BitbucketService bitbucketService, JiraService jiraService, 
                              OpenshiftService openshiftService,
                              GenerateProjectKeyService generateProjectKeyService) {
        this.bitbucketService = bitbucketService;
        this.jiraService = jiraService;
        this.openshiftService = openshiftService;
        this.generateProjectKeyService = generateProjectKeyService;
    }

    @Override
    public CreateProjectResponse createProject(CreateProjectRequest request) {
        // TODO Implement project creation against external systems.
        return null;
    }

    @Override
    public CreateProjectResponse getProject(String projectKey) {
        // TODO Implement project retrieval by key from external systems.
        return null;
    }
}

