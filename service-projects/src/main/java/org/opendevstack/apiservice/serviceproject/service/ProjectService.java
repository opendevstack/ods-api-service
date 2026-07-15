package org.opendevstack.apiservice.serviceproject.service;

import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProjectService {

    ProjectResponse saveProject(ProjectRequest request);

    ProjectResponse getProject(String projectKey);

    void updateProjectStatus(String projectKey, String status);

    List<ProjectResponse> findProjectsByName(String projectName);

    Page<ProjectSummary> getProjects(Integer page, Integer size);
}