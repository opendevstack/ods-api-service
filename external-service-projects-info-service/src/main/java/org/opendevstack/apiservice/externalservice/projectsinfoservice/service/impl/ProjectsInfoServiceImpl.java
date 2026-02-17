package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectsInfoServiceImpl implements ProjectsInfoService {
    @Override
    public Platforms getProjectPlatforms(String projectKey) throws ProjectsInfoServiceException {
        return null;
    }

    @Override
    public boolean validateConnection() {
        return false;
    }

    @Override
    public boolean isHealthy() {
        return false;
    }
}
