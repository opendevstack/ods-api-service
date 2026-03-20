package org.opendevstack.apiservice.project.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectComponentsApi;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.opendevstack.apiservice.project.service.ComponentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.opendevstack.apiservice.project.controller.ComponentsResponseFactory.toResponseEntity;

@RestController
@AllArgsConstructor
@Slf4j
public class ProjectComponentsController implements ProjectComponentsApi {

    private final ComponentsService componentsService;

    @Override
    public ResponseEntity<CreateComponentResponse> createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        try {
            Component component = componentsService.createProjectComponent(projectId, createComponentRequest);
            if (component == null) {
                log.error("Failed to create component for project '{}'", projectId);
                return toResponseEntity(ComponentsResponseFactory.error(projectId));
            }
            return toResponseEntity(ComponentsResponseFactory.entityCreated(projectId, component.getName()));
        } catch (Exception e) {
            log.error("Error while trying to create component for project '" + projectId + "': " + e.getMessage(), e);
            return toResponseEntity(ComponentsResponseFactory.error(projectId));
        }
    }

    @Override
    public ResponseEntity<Component> getProjectComponent(String projectId, String componentId) {
        try {
            Component component = componentsService.getProjectComponent(projectId, componentId);
            if (component == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(component);
        } catch (Exception e) {
            log.error("Error retrieving component '{}' for project '{}': {}", componentId, projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
