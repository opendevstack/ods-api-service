package org.opendevstack.apiservice.projecttechnologies.service;

import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.projecttechnologies.model.ProjectTechnology;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectTechnologiesService {

    private final MarketplaceService marketplaceService;

    public ProjectTechnologiesService(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    public List<ProjectTechnology> getProjectTechnologies(String projectKey) {
        try {
            return marketplaceService.getProjectTechnologies(projectKey).stream()
                    .map(technology -> new ProjectTechnology()
                            .slug(technology.getSlug())
                            .name(technology.getName()))
                    .toList();
        } catch (MarketplaceException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to retrieve project technologies from Marketplace.", exception);
        }
    }
}