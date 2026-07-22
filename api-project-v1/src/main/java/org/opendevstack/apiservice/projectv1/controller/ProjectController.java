package org.opendevstack.apiservice.projectv1.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.projectv1.client.api.ProjectsApi;
import org.opendevstack.apiservice.projectv1.facade.ProjectsFacade;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("projectControllerV1")
@RequestMapping(ProjectController.API_BASE_PATH)
@AllArgsConstructor
@Slf4j
public class ProjectController implements ProjectsApi {

    public static final String API_BASE_PATH = "/api/pub/v1/projects";

    private final ProjectsFacade projectsFacade;

    @GetMapping
    @Override
    public ResponseEntity<GetProjectsResponse> getProjects(Integer page, Integer size) {

        GetProjectsResponse response = projectsFacade.getProjects(page, size);
        response.setLocation(API_BASE_PATH);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
