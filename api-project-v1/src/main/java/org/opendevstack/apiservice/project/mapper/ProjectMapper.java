package org.opendevstack.apiservice.project.mapper;

import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.project.client.model.GetProjectsResponseMetadata;
import org.opendevstack.apiservice.project.client.model.ProjectsResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ProjectMapper {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public GetProjectsResponse toApiResponse(List<ProjectSummary> projects, Integer page, Integer size) {
        int effectivePage = page != null ? page : DEFAULT_PAGE;
        int effectiveSize = size != null && size > 0 ? size : DEFAULT_SIZE;

        int totalElements = projects != null ? projects.size() : 0;
        int totalPages = (int) Math.ceil((double) totalElements / effectiveSize);
        boolean isLast = (effectivePage + 1) >= totalPages;

        List<ProjectsResponse> pageContent = projects == null
                ? List.of()
                : projects.stream()
                        .skip((long) effectivePage * effectiveSize)
                        .limit(effectiveSize)
                        .map(this::toProjectsResponse)
                        .toList();

        GetProjectsResponseMetadata metadata = new GetProjectsResponseMetadata()
                .page(effectivePage)
                .size(effectiveSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(isLast);

        return new GetProjectsResponse()
                .projects(pageContent)
                .metadata(metadata);
    }

    private ProjectsResponse toProjectsResponse(ProjectSummary source) {
        return new ProjectsResponse()
                .projectKey(source.getProjectKey())
                .projectName(source.getProjectName())
                .projectFlavor(source.getProjectFlavor())
                .location(source.getLocation())
                .status(source.getStatus() != null ? source.getStatus().name() : null)
                .createdAt(formatDateTime(source.getCreatedAt()))
                .updatedAt(formatDateTime(source.getUpdatedAt()));
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}
