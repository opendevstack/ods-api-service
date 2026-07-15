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

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final ProjectService projectService;

    private final ProjectMapper projectMapper;

    @Override
    public GetProjectsResponse getProjects(Integer page, Integer size) {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;
        return projectMapper.toApiResponse(projectService.getProjects(page, size));
    }
}
