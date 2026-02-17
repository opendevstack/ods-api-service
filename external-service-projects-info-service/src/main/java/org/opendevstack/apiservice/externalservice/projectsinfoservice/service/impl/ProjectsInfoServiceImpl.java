package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.api.ProjectsApi;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ProjectsInfoServiceImpl implements ProjectsInfoService {

    private ProjectsApi projectsApi;

    @Override
    public Platforms getProjectPlatforms(String projectKey) throws ProjectsInfoServiceException {
        return null;
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
