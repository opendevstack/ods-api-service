package org.opendevstack.apiservice.projectv1.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.projectv1.exception.PageNotFoundException;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.model.Status;

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

        GetProjectsResponse result = sut.toApiResponse(List.of(summary), 0, 20);

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
    void to_api_response_returns_empty_list_when_projects_are_null() {
        GetProjectsResponse result = sut.toApiResponse(null, 0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getProjects()).isEmpty();
        assertThat(result.getMetadata().getTotalElements()).isEqualTo(0);
        assertThat(result.getMetadata().getTotalPages()).isEqualTo(0);
    }

    @Test
    void to_api_response_returns_empty_list_when_projects_list_is_empty() {
        GetProjectsResponse result = sut.toApiResponse(List.of(), 0, 20);

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

        GetProjectsResponse result = sut.toApiResponse(summaries, 0, 20);

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
                ProjectSummary.builder().projectKey("P2").build(),
                ProjectSummary.builder().projectKey("P3").build(),
                ProjectSummary.builder().projectKey("P4").build(),
                ProjectSummary.builder().projectKey("P5").build()
        );

        GetProjectsResponse result = sut.toApiResponse(summaries, 0, 2);

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
                ProjectSummary.builder().projectKey("P1").build(),
                ProjectSummary.builder().projectKey("P2").build(),
                ProjectSummary.builder().projectKey("P3").build(),
                ProjectSummary.builder().projectKey("P4").build(),
                ProjectSummary.builder().projectKey("P5").build()
        );

        GetProjectsResponse result = sut.toApiResponse(summaries, 2, 2);

        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getProjectKey()).isEqualTo("P5");
        assertThat(result.getMetadata().getLast()).isTrue();
    }

    @Test
    void to_api_response_uses_default_page_when_null() {
        ProjectSummary summary = ProjectSummary.builder().projectKey("P1").build();

        GetProjectsResponse result = sut.toApiResponse(List.of(summary), null, 20);

        assertThat(result.getMetadata().getPage()).isEqualTo(0);
    }

    @Test
    void to_api_response_uses_default_size_when_null() {
        ProjectSummary summary = ProjectSummary.builder().projectKey("P1").build();

        GetProjectsResponse result = sut.toApiResponse(List.of(summary), 0, null);

        assertThat(result.getMetadata().getSize()).isEqualTo(20);
    }

    @Test
    void to_api_response_throws_exception_when_page_is_greater_than_total_pages() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder().projectKey("P1").build(),
                ProjectSummary.builder().projectKey("P2").build(),
                ProjectSummary.builder().projectKey("P3").build(),
                ProjectSummary.builder().projectKey("P4").build(),
                ProjectSummary.builder().projectKey("P5").build()
        );

        assertThrows(PageNotFoundException.class,
                () -> sut.toApiResponse(summaries, 5, 2));
    }

    @Test
    void to_api_response_maps_null_status_to_null_string() {
        ProjectSummary summary = ProjectSummary.builder()
                .projectKey("P1")
                .status(null)
                .build();

        GetProjectsResponse result = sut.toApiResponse(List.of(summary), 0, 20);

        assertThat(result.getProjects().get(0).getStatus()).isNull();
    }

    @Test
    void to_api_response_maps_null_datetime_fields_to_null() {
        ProjectSummary summary = ProjectSummary.builder()
                .projectKey("P1")
                .createdAt(null)
                .updatedAt(null)
                .build();

        GetProjectsResponse result = sut.toApiResponse(List.of(summary), 0, 20);

        assertThat(result.getProjects().get(0).getCreatedAt()).isNull();
        assertThat(result.getProjects().get(0).getUpdatedAt()).isNull();
    }
}
