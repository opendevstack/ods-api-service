package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.project.client.model.GetProjectsResponseMetadata;
import org.opendevstack.apiservice.project.client.model.ProjectsResponse;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectControllerTest {

    @Mock
    private ProjectsFacade projectsFacade;

    private ProjectController sut;
    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectController(projectsFacade);

        Jwt jwtToken = Jwt.withTokenValue("dummy-token")
                .claim("appid", UUID.randomUUID().toString())
                .claim("sub", "test-user")
                .header("alg", "none")
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwtToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void get_projects_returns_ok_with_projects_list() {
        ProjectsResponse project = new ProjectsResponse()
                .projectKey("PROJ01")
                .projectName("Project One")
                .projectFlavor("AMP")
                .location("eu")
                .status("RUNNING");

        GetProjectsResponseMetadata metadata = new GetProjectsResponseMetadata()
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .last(true);

        GetProjectsResponse facadeResponse = new GetProjectsResponse()
                .projects(List.of(project))
                .metadata(metadata);

        when(projectsFacade.getProjects(0, 20)).thenReturn(facadeResponse);

        ResponseEntity<GetProjectsResponse> result = sut.getProjects(0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProjects()).hasSize(1);
        assertThat(result.getBody().getProjects().get(0).getProjectKey()).isEqualTo("PROJ01");
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
        assertThat(result.getBody().getMetadata()).isNotNull();
        assertThat(result.getBody().getMetadata().getTotalElements()).isEqualTo(1);
        verify(projectsFacade).getProjects(0, 20);
    }

    @Test
    void get_projects_returns_ok_with_empty_list_when_no_projects_exist() {
        GetProjectsResponseMetadata metadata = new GetProjectsResponseMetadata()
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .last(true);

        GetProjectsResponse facadeResponse = new GetProjectsResponse()
                .projects(List.of())
                .metadata(metadata);

        when(projectsFacade.getProjects(0, 20)).thenReturn(facadeResponse);

        ResponseEntity<GetProjectsResponse> result = sut.getProjects(0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProjects()).isEmpty();
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
        verify(projectsFacade).getProjects(0, 20);
    }
}
