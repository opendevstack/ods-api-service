package org.opendevstack.apiservice.projectv1.facade;

import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;

public interface ProjectsFacade {

    GetProjectsResponse getProjects(Integer page, Integer size);
}
