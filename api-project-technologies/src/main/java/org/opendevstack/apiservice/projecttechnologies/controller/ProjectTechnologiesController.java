package org.opendevstack.apiservice.projecttechnologies.controller;

import org.opendevstack.apiservice.projecttechnologies.api.ProjectTechnologiesApi;
import org.opendevstack.apiservice.projecttechnologies.model.ProjectTechnology;
import org.opendevstack.apiservice.projecttechnologies.service.ProjectTechnologiesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProjectTechnologiesController implements ProjectTechnologiesApi {

    private final ProjectTechnologiesService projectTechnologiesService;

    public ProjectTechnologiesController(ProjectTechnologiesService projectTechnologiesService) {
        this.projectTechnologiesService = projectTechnologiesService;
    }

    @Override
    public ResponseEntity<List<ProjectTechnology>> getProjectTechnologies(String projectKey) {
        return ResponseEntity.ok(projectTechnologiesService.getProjectTechnologies(projectKey));
    }
}