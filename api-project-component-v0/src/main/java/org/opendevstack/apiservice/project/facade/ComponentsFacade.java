package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.project.mapper.MarketplaceMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentsFacade {

    private final MarketplaceService marketplaceExternalService;

    private final MarketplaceMapper marketplaceMapper;

    public Component getProjectComponent(String projectId, String componentId) {
        ProjectComponent marketplaceComponent = marketplaceExternalService.getProjectComponent(projectId, componentId);
        if (marketplaceComponent == null) {
            log.info("Marketplace component with id {} not found", componentId);
            throw new ComponentNotFoundException(
                    String.format("Component '%s' not found for project '%s'", componentId, projectId)
            );
        }
        return marketplaceMapper.mapMarketplaceComponentToV0Component(marketplaceComponent);
    }

    public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        List<CreateComponentParameter> createComponentParameterList = marketplaceMapper.mapCreateComponentRequestToCreateComponentParameterList(createComponentRequest);
        ProjectComponent marketplaceComponent = marketplaceExternalService.createProjectComponent(projectId, createComponentParameterList);
        if (marketplaceComponent == null) {
            log.error("Failed to create component in marketplace for project with id {}", projectId);
            throw new ComponentCreationException(
                    String.format("Failed to create component for project '%s'", projectId)
            );
        }
        return marketplaceMapper.mapMarketplaceComponentToV0Component(marketplaceComponent);
    }

    public Boolean deleteProjectComponent(String projectId, String componentId) {
        try {
            return marketplaceExternalService.deleteProjectComponent(projectId, componentId);
        } catch (MarketplaceException e) {
            log.error("Failed to delete component with id {} for project with id {}", componentId, projectId, e);
            return false;
        }
    }

    public boolean registerProjectComponent(String projectId, String componentId) {
        try {
            marketplaceExternalService.registerProjectComponent(projectId, componentId);
            return true;
        } catch (MarketplaceException e) {
            log.error("Failed to register component in marketplace for project with id {}", projectId, e);
            return false;
        }
    }
}
