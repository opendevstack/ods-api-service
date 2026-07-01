package org.opendevstack.apiservice.project.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.client.api.ProjectsApi;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ProjectController.API_BASE_PATH)
@AllArgsConstructor
@Slf4j
public class ProjectController implements ProjectsApi {

    public static final String API_BASE_PATH = "/api/pub/v1/projects";

    private static final String HTTP_HEADER_LOCATION = "Location";

    private final ProjectsFacade projectsFacade;

    // private final ProjectRequestValidator projectRequestValidator;

    @GetMapping
    @Override
    public ResponseEntity<List<GetProjectsResponse>> getProjects(@Valid @RequestParam Integer page,
                                                                 @Valid @RequestParam Integer size) {
        return null;
    }
}
