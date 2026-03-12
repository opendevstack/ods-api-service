package org.opendevstack.apiservice.serviceproject.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.serviceproject.model.CreateProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final OpenshiftService openshiftService;
    
    private final BitbucketService bitbucketService;
    
    private final JiraService jiraService;
    
    private final GenerateProjectKeyService generateProjectKeyService;

    @Override
    public CreateProjectResponse createProject(CreateProjectRequest request) {
        return CreateProjectResponse.builder().build();
    }

    @Override
    public CreateProjectResponse getProject(String projectKey) {
        return CreateProjectResponse.builder().build();
    }
}

