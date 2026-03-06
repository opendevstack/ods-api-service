package org.opendevstack.apiservice.project.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectsApi;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v0/projects")
@AllArgsConstructor
@Slf4j
public class ProjectController implements ProjectsApi {

    private final ProjectsFacade projectsFacade;
    
    @PostMapping
    @Override
    public ResponseEntity<CreateProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest) {
        try {
            return ResponseEntity.ok(projectsFacade.createProject(createProjectRequest));
        } catch (ProjectCreationException e) {
            log.error("Project creation conflict: {}", e.getMessage());
            CreateProjectResponse errorResponse = new CreateProjectResponse();
            errorResponse.setError("CONFLICT");
            errorResponse.setErrorKey("PROJECT_ALREADY_EXISTS");
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (ProjectKeyGenerationException e) {
            log.error("Failed to generate project key: {}", e.getMessage(), e);
            CreateProjectResponse errorResponse = new CreateProjectResponse();
            errorResponse.setError("INTERNAL_ERROR");
            errorResponse.setErrorKey("PROJECT_KEY_GENERATION_FAILED");
            errorResponse.setMessage("Failed to generate a unique project key.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{projectKey}")
    @Override
    public ResponseEntity<CreateProjectResponse> getProject(@PathVariable String projectKey) {
        try {
            CreateProjectResponse response = projectsFacade.getProject(projectKey);
            if (response == null) {
                CreateProjectResponse notFoundResponse = new CreateProjectResponse();
                notFoundResponse.setError("NOT_FOUND");
                notFoundResponse.setErrorKey("PROJECT_NOT_FOUND");
                notFoundResponse.setMessage(String.format("Project with key '%s' not found", projectKey));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }
            return ResponseEntity.ok(response);
        } catch (ProjectCreationException e) {
            log.error("Error retrieving project '{}': {}", projectKey, e.getMessage(), e);
            CreateProjectResponse errorResponse = new CreateProjectResponse();
            errorResponse.setError("INTERNAL_ERROR");
            errorResponse.setErrorKey("INTERNAL_ERROR");
            errorResponse.setMessage("An error occurred while processing the request.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
