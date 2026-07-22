package org.opendevstack.apiservice.projectv1.facade.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.projectv1.exception.PageNotFoundException;
import org.opendevstack.apiservice.projectv1.mapper.ProjectMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    void get_projects_returns_response_built_from_mapper_and_metadata() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder()
                        .projectKey("PROJ01")
                        .projectName("Project One")
                        .projectFlavor("AMP")
                        .location("eu")
                        .status(Status.RUNNING)
                        .build()
        );
        Page<ProjectSummary> summaryPage = new PageImpl<>(summaries, PageRequest.of(0, 20), 1);
        List<ProjectsResponse> mappedProjects = List.of(new ProjectsResponse().projectKey("PROJ01"));

        when(projectService.getProjects(0, 20)).thenReturn(summaryPage);
        when(projectMapper.toProjectsResponse(summaries)).thenReturn(mappedProjects);

        GetProjectsResponse result = sut.getProjects(0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getProjectKey()).isEqualTo("PROJ01");
        assertThat(result.getMetadata().getPage()).isEqualTo(0);
        assertThat(result.getMetadata().getSize()).isEqualTo(20);
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(1);
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(1);
        assertThat(result.getMetadata().getLast()).isTrue();
        verify(projectService).getProjects(0, 20);
        verify(projectMapper).toProjectsResponse(summaries);
    }

    @Test
    void get_projects_returns_empty_list_when_no_projects() {
        Page<ProjectSummary> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(projectService.getProjects(0, 20)).thenReturn(emptyPage);
        when(projectMapper.toProjectsResponse(List.of())).thenReturn(List.of());

        GetProjectsResponse result = sut.getProjects(0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getProjects()).isEmpty();
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(0);
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(0);
        assertThat(result.getMetadata().getLast()).isTrue();
        verify(projectService).getProjects(0, 20);
        verify(projectMapper).toProjectsResponse(List.of());
    }

    @Test
    void get_projects_propagates_service_exception() {
        when(projectService.getProjects(0, 20)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> sut.getProjects(0, 20));

        verify(projectService).getProjects(0, 20);
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
        Page<ProjectSummary> summaryPage = new PageImpl<>(summaries, PageRequest.of(0, 20), 2);
        List<ProjectsResponse> mappedProjects = List.of(
                new ProjectsResponse().projectKey("PROJ01").status("RUNNING"),
                new ProjectsResponse().projectKey("PROJ02").status("PENDING")
        );

        when(projectService.getProjects(0, 20)).thenReturn(summaryPage);
        when(projectMapper.toProjectsResponse(summaries)).thenReturn(mappedProjects);

        GetProjectsResponse result = sut.getProjects(0, 20);

        assertThat(result.getProjects()).hasSize(2);
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(2);
        assertThat(result.getMetadata().getLast()).isTrue();
    }

    @Test
    void get_projects_uses_default_page_when_null() {
        Page<ProjectSummary> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(projectService.getProjects(0, 20)).thenReturn(page);
        when(projectMapper.toProjectsResponse(List.of())).thenReturn(List.of());

        GetProjectsResponse result = sut.getProjects(null, 20);

        assertThat(result).isNotNull();
        verify(projectService).getProjects(0, 20);
    }

    @Test
    void get_projects_uses_default_size_when_null() {
        Page<ProjectSummary> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(projectService.getProjects(0, 20)).thenReturn(page);
        when(projectMapper.toProjectsResponse(List.of())).thenReturn(List.of());

        GetProjectsResponse result = sut.getProjects(0, null);

        assertThat(result).isNotNull();
        verify(projectService).getProjects(0, 20);
    }

    @Test
    void get_projects_throws_page_not_found_when_page_exceeds_total_pages() {
        // Page 5 requested but only 3 pages exist (5 elements, size 2)
        Page<ProjectSummary> page = new PageImpl<>(List.of(), PageRequest.of(5, 2), 5);

        when(projectService.getProjects(5, 2)).thenReturn(page);
        when(projectMapper.toProjectsResponse(List.of())).thenReturn(List.of());

        assertThrows(PageNotFoundException.class,
                () -> sut.getProjects(5, 2));

        verify(projectService).getProjects(5, 2);
    }

    @Test
    void get_projects_metadata_is_not_last_for_first_page_when_multiple_pages_exist() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder().projectKey("P1").build(),
                ProjectSummary.builder().projectKey("P2").build()
        );
        Page<ProjectSummary> summaryPage = new PageImpl<>(summaries, PageRequest.of(0, 2), 5);
        List<ProjectsResponse> mappedProjects = List.of(
                new ProjectsResponse().projectKey("P1"),
                new ProjectsResponse().projectKey("P2")
        );

        when(projectService.getProjects(0, 2)).thenReturn(summaryPage);
        when(projectMapper.toProjectsResponse(summaries)).thenReturn(mappedProjects);

        GetProjectsResponse result = sut.getProjects(0, 2);

        assertThat(result.getMetadata().getLast()).isFalse();
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(3);
    }
}
