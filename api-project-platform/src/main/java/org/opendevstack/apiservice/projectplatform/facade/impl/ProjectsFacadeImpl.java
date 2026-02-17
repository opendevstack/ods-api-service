package org.opendevstack.apiservice.projectplatform.facade.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.opendevstack.apiservice.projectplatform.exception.ProjectPlatformsException;
import org.opendevstack.apiservice.projectplatform.facade.ProjectsFacade;
import org.opendevstack.apiservice.projectplatform.mapper.ProjectPlatformsMapper;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

@Component
@Slf4j
public class ProjectsFacadeImpl implements ProjectsFacade {

    private final ProjectsInfoService projectsInfoService;
    private final ProjectPlatformsMapper mapper;

    public ProjectsFacadeImpl(ProjectsInfoService projectsInfoService, ProjectPlatformsMapper mapper) {
        this.projectsInfoService = projectsInfoService;
        this.mapper = mapper;
    }

    @Override
    public ProjectPlatforms getProjectPlatforms(String projectKey) throws ProjectPlatformsException {
        try {
            var idToken = getIdToken();

            Platforms externalPlatforms =
                    projectsInfoService.getProjectPlatforms(projectKey, idToken);
            return mapper.toApiModel(externalPlatforms);
        } catch (ProjectsInfoServiceException e) {
            throw new ProjectPlatformsException("Failed to retrieve project platforms", e);
        }
    }

    protected String getIdToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Authenticated user '{}'", auth.getName());

        var token = "INVALID token";

        if (auth instanceof BearerTokenAuthentication bearer) {
            token = bearer.getToken().getTokenValue();
        }

        log.debug("Token extracted: {}", token);

        return token;
    }

}
