package org.opendevstack.apiservice.project.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.api.ProjectComponentsApi;
import org.opendevstack.apiservice.project.facade.ComponentsFacade;
import org.opendevstack.apiservice.project.mapper.ComponentResponseMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/pub/v0")
public class ProjectComponentsController implements ProjectComponentsApi {

    private final ComponentsFacade componentsFacade;

    private final ComponentResponseMapper componentResponseMapper;

    @Override
    public ResponseEntity<CreateComponentResponse> createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        try {
            boolean success = componentsFacade.createProjectComponent(projectId, createComponentRequest);
            log.info("Created component for project id {} and request {}", projectId, createComponentRequest);
            if (!success) {
                log.error("Failed to create component for project '{}'", projectId);
                return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.error(projectId));
            }
            return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.entityCreated(projectId));
        } catch (Exception e) {
            log.error("Error while trying to create component for project '" + projectId + "': " + e.getMessage(), e);
            return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.error(projectId));
        }
    }

    @Override
    public ResponseEntity<Component> getProjectComponent(String projectId, String componentId) {
        try {
            Component component = componentsFacade.getProjectComponent(projectId, componentId);
            log.info("Retrieved component {} for component id '{}' and project '{}'", component, componentId, projectId);
            if (component == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(component);
        } catch (Exception e) {
            log.error("Error retrieving component '{}' for project '{}': {}", componentId, projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<CreateComponentResponse> registerProjectComponent(String projectId, String componentId) {
        try {
            boolean success = componentsFacade.registerProjectComponent(projectId, componentId);
            log.info("Registered component for project id {} and component id {}", projectId, componentId);
            if (!success) {
                log.error("Failed to register component for project '{}'", projectId);
                return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.error(projectId));
            }
            return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.entityCreated(projectId));
        } catch (Exception e) {
            log.error("Error while trying to register component for project '" + projectId + "': " + e.getMessage(), e);
            return componentResponseMapper.toResponseEntity(ComponentsResponseFactory.error(projectId));
        }
    }

    @Override
    public ResponseEntity<Boolean> deleteProjectComponent(String projectId, String componentId) {
        try {
            Boolean result = componentsFacade.deleteProjectComponent(projectId, componentId);
            log.info("Delete component '{}' for project '{}' with result {}", componentId, projectId, result);
            if (!result) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            log.error("Error deleting component '{}' for project '{}': {}", componentId, projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
