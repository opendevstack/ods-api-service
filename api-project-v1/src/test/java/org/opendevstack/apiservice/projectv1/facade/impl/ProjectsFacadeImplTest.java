package org.opendevstack.apiservice.projectv1.facade.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponseMetadata;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.projectv1.mapper.ProjectMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectsFacadeImplTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectMapper projectMapper;

    private ProjectsFacadeImpl sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new ProjectsFacadeImpl(projectService, projectMapper);
    }

    @Test
    void get_projects_returns_response_from_mapper() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder()
                        .projectKey("PROJ01")
                        .projectName("Project One")
                        .projectFlavor("AMP")
                        .location("eu")
                        .status(Status.RUNNING)
                        .build()
        );

        GetProjectsResponse expectedResponse = new GetProjectsResponse()
                .projects(List.of(new ProjectsResponse().projectKey("PROJ01")))
                .metadata(new GetProjectsResponseMetadata().page(0).size(20).totalElements(1).totalPages(1).last(true));

        when(projectService.getProjects()).thenReturn(summaries);
        when(projectMapper.toApiResponse(summaries, 0, 20)).thenReturn(expectedResponse);

        GetProjectsResponse result = sut.getProjects(0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getProjectKey()).isEqualTo("PROJ01");
        verify(projectService).getProjects();
        verify(projectMapper).toApiResponse(summaries, 0, 20);
    }

    @Test
    void get_projects_returns_empty_list_when_no_projects() {
        List<ProjectSummary> emptySummaries = List.of();

        GetProjectsResponse emptyResponse = new GetProjectsResponse()
                .projects(List.of())
                .metadata(new GetProjectsResponseMetadata().page(0).size(20).totalElements(0).totalPages(0).last(true));

        when(projectService.getProjects()).thenReturn(emptySummaries);
        when(projectMapper.toApiResponse(emptySummaries, 0, 20)).thenReturn(emptyResponse);

        GetProjectsResponse result = sut.getProjects(0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getProjects()).isEmpty();
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(0);
        verify(projectService).getProjects();
        verify(projectMapper).toApiResponse(emptySummaries, 0, 20);
    }

    @Test
    void get_projects_propagates_service_exception() {
        when(projectService.getProjects()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> sut.getProjects(0, 20));

        verify(projectService).getProjects();
    }

    @Test
    void get_projects_returns_multiple_projects_with_all_fields() {
        OffsetDateTime now = OffsetDateTime.now();

        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder()
                        .projectKey("PROJ01")
                        .projectName("Project One")
                        .projectFlavor("AMP")
                        .location("eu")
                        .status(Status.RUNNING)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                ProjectSummary.builder()
                        .projectKey("PROJ02")
                        .projectName("Project Two")
                        .projectFlavor("DEFAULT")
                        .location("us")
                        .status(Status.PENDING)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        GetProjectsResponse expectedResponse = new GetProjectsResponse()
                .projects(List.of(
                        new ProjectsResponse().projectKey("PROJ01").status("RUNNING"),
                        new ProjectsResponse().projectKey("PROJ02").status("PENDING")
                ))
                .metadata(new GetProjectsResponseMetadata().page(0).size(20).totalElements(2).totalPages(1).last(true));

        when(projectService.getProjects()).thenReturn(summaries);
        when(projectMapper.toApiResponse(summaries, 0, 20)).thenReturn(expectedResponse);

        GetProjectsResponse result = sut.getProjects(0, 20);

        assertThat(result.getProjects()).hasSize(2);
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(2);
        assertThat(result.getMetadata().getLast()).isTrue();
    }
}
