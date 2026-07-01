package org.opendevstack.apiservice.project.facade.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.project.mapper.ProjectMapper;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.stereotype.Component;

@Component("apiProjectFacadeImpl")
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
