package org.opendevstack.apiservice.serviceproject.service;

import org.opendevstack.apiservice.serviceproject.model.CreateProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.CreateProjectResponse;

public interface ProjectService {

    CreateProjectResponse createProject(CreateProjectRequest request);

    CreateProjectResponse getProject(String projectKey);
}

