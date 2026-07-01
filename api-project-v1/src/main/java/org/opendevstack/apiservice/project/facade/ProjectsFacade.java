package org.opendevstack.apiservice.project.facade;

import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;
import java.util.List;

public interface ProjectsFacade {

    List<GetProjectsResponse> getProjects(Integer page, Integer size);
}
