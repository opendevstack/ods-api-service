package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.opendevstack.apiservice.project.service.ComponentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.opendevstack.apiservice.project.util.TestHelper.*;

@ExtendWith(MockitoExtension.class)
class ProjectComponentsControllerTest {

    @Mock
    private ComponentsService componentsService;

    private ProjectComponentsController projectComponentsController;

    @BeforeEach
    void setup() {
        projectComponentsController = new ProjectComponentsController(componentsService);
    }

    @Test
    void testCreateProjectComponent_whenSuccess_thenReturnOk() throws Exception {
        Component testComponent = buildTestComponent();
        String testProjectId = "testProjectId";
        CreateComponentRequest testCreateComponentRequest = buildTestCreateComponentRequest();
        CreateComponentResponse testServiceResponseSuccess = buildTestCreateComponentResponseSuccess(testComponent.getName(),
                testProjectId);

        when(componentsService.createProjectComponent(anyString(), any(CreateComponentRequest.class)))
                .thenReturn(testComponent);

        ResponseEntity<CreateComponentResponse> response = projectComponentsController.createProjectComponent(testProjectId,
                testCreateComponentRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(testServiceResponseSuccess);
    }

    @Test
    void testCreateProjectComponent_whenFailure_thenReturnErrorResponse() throws Exception {
        CreateComponentRequest testCreateComponentRequest = buildTestCreateComponentRequest();
        String testProjectId = "testProjectId";
        CreateComponentResponse testServiceResponseFailure = buildTestCreateComponentResponseFailure(testProjectId);

        when(componentsService.createProjectComponent(anyString(), any(CreateComponentRequest.class)))
                .thenReturn(null);

        ResponseEntity<CreateComponentResponse> response = projectComponentsController.createProjectComponent(testProjectId,
                testCreateComponentRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(testServiceResponseFailure);
    }

    @Test
    void testGetProjectComponent_whenSuccess_thenReturnOk() throws Exception {
        Component testComponent = buildTestComponent();

        when(componentsService.getProjectComponent(anyString(), anyString()))
                .thenReturn(testComponent);

        ResponseEntity<Component> response = projectComponentsController.getProjectComponent("projectId",
                "testId");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(testComponent);
    }

    @Test
    void testGetProjectComponent_whenFailure_thenReturnErrorResponse() throws Exception {
        when(componentsService.getProjectComponent(anyString(), anyString()))
                .thenReturn(null);

        ResponseEntity<Component> response = projectComponentsController.getProjectComponent("projectId",
                "testId");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}