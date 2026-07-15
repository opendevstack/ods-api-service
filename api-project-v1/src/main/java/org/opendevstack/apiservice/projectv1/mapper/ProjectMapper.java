package org.opendevstack.apiservice.projectv1.mapper;

import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponseMetadata;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.projectv1.exception.ErrorKey;
import org.opendevstack.apiservice.projectv1.exception.PageNotFoundException;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component("projectMapperV1")
public class ProjectMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public GetProjectsResponse toApiResponse(Page<ProjectSummary> projectPage) {
        int effectivePage = projectPage.getNumber();
        int effectiveSize = projectPage.getSize();
        int totalElements = (int) projectPage.getTotalElements();
        int totalPages = projectPage.getTotalPages();

        if (!isPageZeroCase(effectivePage, totalPages) && (effectivePage + 1) > totalPages) {
            throw new PageNotFoundException(
                ErrorKey.PAGE_NOT_FOUND,
                "within the " + totalPages + " total pages."
            );
        }

        boolean isLast = isPageZeroCase(effectivePage, totalPages) || (effectivePage + 1) == totalPages;

        List<ProjectsResponse> pageContent = projectPage.getContent().stream()
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

    private boolean isPageZeroCase(int effectivePage, int totalPages) {
        return effectivePage == 0 && totalPages == 0;
    }
}