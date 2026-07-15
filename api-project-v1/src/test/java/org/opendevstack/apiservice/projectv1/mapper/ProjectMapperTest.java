package org.opendevstack.apiservice.projectv1.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.projectv1.exception.PageNotFoundException;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectMapperTest {

    private ProjectMapper sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectMapper();
    }

    @Test
    void to_api_response_maps_projects_correctly() {
        OffsetDateTime createdAt = OffsetDateTime.of(2024, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime updatedAt = OffsetDateTime.of(2024, 6, 1, 12, 30, 0, 0, ZoneOffset.UTC);

        ProjectSummary summary = ProjectSummary.builder()
                .projectKey("PROJ01")
                .projectName("Project One")
                .projectFlavor("AMP")
                .location("eu")
                .status(Status.RUNNING)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Page<ProjectSummary> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        GetProjectsResponse result = sut.toApiResponse(page);

        assertThat(result).isNotNull();
        assertThat(result.getProjects()).hasSize(1);

        ProjectsResponse project = result.getProjects().get(0);
        assertThat(project.getProjectKey()).isEqualTo("PROJ01");
        assertThat(project.getProjectName()).isEqualTo("Project One");
        assertThat(project.getProjectFlavor()).isEqualTo("AMP");
        assertThat(project.getLocation()).isEqualTo("eu");
        assertThat(project.getStatus()).isEqualTo("RUNNING");
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(project.getUpdatedAt()).isNotNull();
    }

    @Test
    void to_api_response_returns_empty_list_when_projects_page_is_empty() {
        Page<ProjectSummary> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        GetProjectsResponse result = sut.toApiResponse(page);

        assertThat(result).isNotNull();
        assertThat(result.getProjects()).isEmpty();
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(0);
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(0);
        assertThat(result.getMetadata().getLast()).isTrue();
    }

    @Test
    void to_api_response_calculates_metadata_correctly() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder().projectKey("P1").build(),
                ProjectSummary.builder().projectKey("P2").build(),
                ProjectSummary.builder().projectKey("P3").build()
        );

        Page<ProjectSummary> page = new PageImpl<>(summaries, PageRequest.of(0, 20), 3);
        GetProjectsResponse result = sut.toApiResponse(page);

        assertThat(result.getMetadata().getPage()).isEqualTo(0);
        assertThat(result.getMetadata().getSize()).isEqualTo(20);
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(3);
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(1);
        assertThat(result.getMetadata().getLast()).isTrue();
    }

    @Test
    void to_api_response_paginates_correctly_first_page() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder().projectKey("P1").build(),
                ProjectSummary.builder().projectKey("P2").build()
        );

        Page<ProjectSummary> page = new PageImpl<>(summaries, PageRequest.of(0, 2), 5);
        GetProjectsResponse result = sut.toApiResponse(page);

        assertThat(result.getProjects()).hasSize(2);
        assertThat(result.getProjects().get(0).getProjectKey()).isEqualTo("P1");
        assertThat(result.getProjects().get(1).getProjectKey()).isEqualTo("P2");
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(5);
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(3);
        assertThat(result.getMetadata().getLast()).isFalse();
    }

    @Test
    void to_api_response_paginates_correctly_last_page() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder().projectKey("P5").build()
        );

        Page<ProjectSummary> page = new PageImpl<>(summaries, PageRequest.of(2, 2), 5);
        GetProjectsResponse result = sut.toApiResponse(page);

        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getProjectKey()).isEqualTo("P5");
        assertThat(result.getMetadata().getLast()).isTrue();
    }

    @Test
    void to_api_response_throws_exception_when_page_is_greater_than_total_pages() {
        // Page 5 requested but only 3 pages exist (5 elements, size 2)
        Page<ProjectSummary> page = new PageImpl<>(List.of(), PageRequest.of(5, 2), 5);

        assertThrows(PageNotFoundException.class,
                () -> sut.toApiResponse(page));
    }

    @Test
    void to_api_response_maps_null_status_to_null_string() {
        ProjectSummary summary = ProjectSummary.builder()
                .projectKey("P1")
                .status(null)
                .build();

        Page<ProjectSummary> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        GetProjectsResponse result = sut.toApiResponse(page);

        assertThat(result.getProjects().get(0).getStatus()).isNull();
    }

    @Test
    void to_api_response_maps_null_datetime_fields_to_null() {
        ProjectSummary summary = ProjectSummary.builder()
                .projectKey("P1")
                .createdAt(null)
                .updatedAt(null)
                .build();

        Page<ProjectSummary> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        GetProjectsResponse result = sut.toApiResponse(page);

        assertThat(result.getProjects().get(0).getCreatedAt()).isNull();
        assertThat(result.getProjects().get(0).getUpdatedAt()).isNull();
    }
}
