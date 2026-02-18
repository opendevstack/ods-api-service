package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.ApiClient;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.api.ProjectsApi;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.mapper.PlatformsMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectsInfoServiceImpl implements ProjectsInfoService {

    private final ApiClient apiClient;

    private final ProjectsApi projectsApi;

    private final PlatformsMapper platformsMapper;

    @Value("${externalservices.projects-info-service.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProjectsInfoServiceImpl(ApiClient apiClient, ProjectsApi projectsApi, PlatformsMapper platformsMapper) {
        this.apiClient = apiClient;
        this.projectsApi = projectsApi;
        this.platformsMapper = platformsMapper;
    }

    @PostConstruct
    public void configureApiClient() {
        this.apiClient.setBasePath(baseUrl);
    }

    @Override
    public Platforms getProjectPlatforms(String projectKey) throws ProjectsInfoServiceException {
        var projectPlatforms = projectsApi.getProjectPlatforms(projectKey);

        return platformsMapper.asPlatforms(projectPlatforms);
    }

    @Override
    public boolean validateConnection() {
        return true;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

}
