package org.opendevstack.apiservice.project.facade;

import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;

public interface ProjectsFacade {

    GetProjectsResponse getProjects(Integer page, Integer size);
}
