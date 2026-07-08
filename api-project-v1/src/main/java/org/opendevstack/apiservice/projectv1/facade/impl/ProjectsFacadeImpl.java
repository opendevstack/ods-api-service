package org.opendevstack.apiservice.projectv1.facade.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.projectv1.facade.ProjectsFacade;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.mapper.ProjectMapper;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.stereotype.Component;

@Component("apiProjectFacadeImplV1")
@AllArgsConstructor
@Slf4j
public class ProjectsFacadeImpl implements ProjectsFacade {

    private final ProjectService projectService;

    private final ProjectMapper projectMapper;

    @Override
    public GetProjectsResponse getProjects(Integer page, Integer size) {
        return projectMapper.toApiResponse(projectService.getProjects(), page, size);
    }
}
