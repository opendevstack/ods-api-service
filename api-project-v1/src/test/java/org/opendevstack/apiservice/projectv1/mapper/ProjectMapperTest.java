package org.opendevstack.apiservice.projectv1.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.model.Status;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        projectMapper = Mappers.getMapper(ProjectMapper.class);
    }

    @Test
    void to_projects_response_maps_single_project_correctly() {
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

        ProjectsResponse result = projectMapper.toProjectsResponse(summary);

        assertThat(result).isNotNull();
        assertThat(result.getProjectKey()).isEqualTo("PROJ01");
        assertThat(result.getProjectName()).isEqualTo("Project One");
        assertThat(result.getProjectFlavor()).isEqualTo("AMP");
        assertThat(result.getLocation()).isEqualTo("eu");
        assertThat(result.getStatus()).isEqualTo("RUNNING");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void to_projects_response_maps_list_correctly() {
        List<ProjectSummary> summaries = List.of(
                ProjectSummary.builder().projectKey("P1").status(Status.RUNNING).build(),
                ProjectSummary.builder().projectKey("P2").status(Status.PENDING).build()
        );

        List<ProjectsResponse> result = projectMapper.toProjectsResponse(summaries);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProjectKey()).isEqualTo("P1");
        assertThat(result.get(0).getStatus()).isEqualTo("RUNNING");
        assertThat(result.get(1).getProjectKey()).isEqualTo("P2");
        assertThat(result.get(1).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void to_projects_response_returns_empty_list_for_empty_input() {
        List<ProjectsResponse> result = projectMapper.toProjectsResponse(List.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void to_projects_response_maps_null_status_to_null_string() {
        ProjectSummary summary = ProjectSummary.builder()
                .projectKey("P1")
                .status(null)
                .build();

        ProjectsResponse result = projectMapper.toProjectsResponse(summary);

        assertThat(result.getStatus()).isNull();
    }

    @Test
    void to_projects_response_maps_null_datetime_fields_to_null() {
        ProjectSummary summary = ProjectSummary.builder()
                .projectKey("P1")
                .createdAt(null)
                .updatedAt(null)
                .build();

        ProjectsResponse result = projectMapper.toProjectsResponse(summary);

        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }
}
