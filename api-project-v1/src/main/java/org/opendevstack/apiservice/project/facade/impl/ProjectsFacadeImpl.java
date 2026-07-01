package org.opendevstack.apiservice.project.facade.impl;

import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;

import java.util.List;

public class ProjectsFacadeImpl implements ProjectsFacade {

    @Override
    public List<GetProjectsResponse> getProjects(Integer page, Integer size) {
        return List.of();
    }
}
