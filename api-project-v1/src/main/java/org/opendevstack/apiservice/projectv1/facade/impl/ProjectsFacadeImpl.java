package org.opendevstack.apiservice.projectv1.facade.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponseMetadata;
import org.opendevstack.apiservice.projectv1.client.model.ProjectsResponse;
import org.opendevstack.apiservice.projectv1.exception.ErrorKey;
import org.opendevstack.apiservice.projectv1.exception.PageNotFoundException;
import org.opendevstack.apiservice.projectv1.facade.ProjectsFacade;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.mapper.ProjectMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("apiProjectFacadeImplV1")
@AllArgsConstructor
@Slf4j
public class ProjectsFacadeImpl implements ProjectsFacade {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final ProjectService projectService;

    private final ProjectMapper projectMapper;

    @Override
    public GetProjectsResponse getProjects(Integer page, Integer size) {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;

        Page<ProjectSummary> projectsPage = projectService.getProjects(page, size);

        List<ProjectsResponse> pageContent = projectMapper.toProjectsResponse(projectsPage.getContent());
        GetProjectsResponseMetadata metadata = buildMetadata(projectsPage);

        return new GetProjectsResponse()
                .projects(pageContent)
                .metadata(metadata);
    }

    private GetProjectsResponseMetadata buildMetadata(Page<ProjectSummary> projectPage) {
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

        return new GetProjectsResponseMetadata()
                .page(effectivePage)
                .size(effectiveSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(isLast);
    }

    private boolean isPageZeroCase(int effectivePage, int totalPages) {
        return effectivePage == 0 && totalPages == 0;
    }
}
