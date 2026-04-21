package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
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

    public Component getProjectComponent(String projectId, String componentId) throws MarketplaceException {
        ProjectComponentInfo marketplaceComponent = marketplaceExternalService.getProjectComponent(projectId, componentId);
        if (marketplaceComponent == null) {
            log.info("Marketplace component with id {} not found", componentId);
            return null;
        }
        return marketplaceMapper.mapMarketplaceComponentToV0Component(marketplaceComponent);
    }

    public boolean createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) throws MarketplaceException {
        List<ProvisionActionParameter> createComponentParameterList = marketplaceMapper.mapCreateComponentRequestToCreateComponentParameterList(createComponentRequest);
        boolean success = marketplaceExternalService.provisionProjectComponent(projectId, createComponentParameterList);
        if (!success) {
            log.error("Failed to create component in marketplace for project with id {}", projectId);
        }
        return success;
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
