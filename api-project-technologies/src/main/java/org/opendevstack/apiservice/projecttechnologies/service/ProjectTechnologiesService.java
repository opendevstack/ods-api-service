package org.opendevstack.apiservice.projecttechnologies.service;

import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceNotFoundException;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.projecttechnologies.model.ProjectTechnology;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectTechnologiesService {

    private final MarketplaceService marketplaceService;

    public List<ProjectTechnology> getProjectTechnologies(String projectKey) {
        try {
            return marketplaceService.getProjectTechnologies(projectKey).stream()
                    .map(technology -> ProjectTechnology.builder()
                            .slug(technology.getSlug())
                            .name(technology.getName())
                            .build())
                    .toList();
        } catch (MarketplaceNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                exception.getMessage(), exception);
        } catch (MarketplaceException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to retrieve project technologies from Marketplace.", exception);
        }
    }
}