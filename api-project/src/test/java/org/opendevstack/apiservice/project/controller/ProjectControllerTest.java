package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectsFacade projectsFacade;

    private ProjectController sut;

    @BeforeEach
    void setup() {
        sut = new ProjectController(projectsFacade);
    }

    @Test
    void createProject_whenSuccess_thenReturnOk() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest("My Project");
        request.setProjectKey("PROJ01");

        CreateProjectResponse serviceResponse = new CreateProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus("Initiated");
        serviceResponse.setMessage("The project creation process has been successfully initiated.");

        when(projectsFacade.createProject(any(CreateProjectRequest.class)))
                .thenReturn(serviceResponse);

        ResponseEntity<CreateProjectResponse> result = sut.createProject(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProjectKey()).isEqualTo("PROJ01");
        assertThat(result.getBody().getStatus()).isEqualTo("Initiated");
    }

    @Test
    void createProject_whenProjectCreationException_thenReturnConflict() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest("My Project");
        request.setProjectKey("EXISTING");

        when(projectsFacade.createProject(any(CreateProjectRequest.class)))
                .thenThrow(new ProjectCreationException("Project with key 'EXISTING' already exists"));

        ResponseEntity<CreateProjectResponse> result = sut.createProject(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo("CONFLICT");
        assertThat(result.getBody().getErrorKey()).isEqualTo("PROJECT_ALREADY_EXISTS");
        assertThat(result.getBody().getMessage()).contains("already exists");
    }

    @Test
    void createProject_whenProjectKeyGenerationException_thenReturnInternalServerError() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest("My Project");

        when(projectsFacade.createProject(any(CreateProjectRequest.class)))
                .thenThrow(new ProjectKeyGenerationException("Failed to generate unique project key after 10 retries"));

        ResponseEntity<CreateProjectResponse> result = sut.createProject(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo("INTERNAL_ERROR");
        assertThat(result.getBody().getErrorKey()).isEqualTo("PROJECT_KEY_GENERATION_FAILED");
        assertThat(result.getBody().getMessage()).isEqualTo("Failed to generate a unique project key.");
    }

    @Test
    void getProject_whenFound_thenReturnOk() throws Exception {
        CreateProjectResponse serviceResponse = new CreateProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus("Initiated");

        when(projectsFacade.getProject("PROJ01")).thenReturn(serviceResponse);

        ResponseEntity<CreateProjectResponse> result = sut.getProject("PROJ01");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProjectKey()).isEqualTo("PROJ01");
        verify(projectsFacade).getProject("PROJ01");
    }

    @Test
    void getProject_whenNotFound_thenReturnNotFound() throws Exception {
        when(projectsFacade.getProject("UNKNOWN")).thenReturn(null);

        ResponseEntity<CreateProjectResponse> result = sut.getProject("UNKNOWN");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo("NOT_FOUND");
        assertThat(result.getBody().getErrorKey()).isEqualTo("PROJECT_NOT_FOUND");
        assertThat(result.getBody().getMessage()).contains("UNKNOWN");
    }

    @Test
    void getProject_whenServiceThrows_thenReturnInternalServerError() throws Exception {
        when(projectsFacade.getProject(anyString()))
                .thenThrow(new ProjectCreationException("Database error"));

        ResponseEntity<CreateProjectResponse> result = sut.getProject("PROJ01");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo("INTERNAL_ERROR");
        assertThat(result.getBody().getErrorKey()).isEqualTo("INTERNAL_ERROR");
        assertThat(result.getBody().getMessage()).isEqualTo("An error occurred while processing the request.");
    }

}
