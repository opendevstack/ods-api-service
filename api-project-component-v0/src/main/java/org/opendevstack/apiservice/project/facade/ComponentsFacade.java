package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentExtendedInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.service.CatalogItemOperations;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.project.exception.CatalogItemNotFoundException;
import org.opendevstack.apiservice.project.exception.ComponentAlreadyExistsException;
import org.opendevstack.apiservice.project.exception.ComponentCreationException;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.exception.ComponentRetrievalException;
import org.opendevstack.apiservice.project.mapper.MarketplaceMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentsFacade {

    private final MarketplaceService marketplaceExternalService;

    private final MarketplaceMapper marketplaceMapper;

    public Component getProjectComponent(String projectId, String componentId) {
        try {
            ProjectComponentExtendedInfo marketplaceComponent = marketplaceExternalService.getProjectComponent(projectId, componentId);
            if (marketplaceComponent == null) {
                log.info("Marketplace component with id {} not found", componentId);
                throw new ComponentNotFoundException(
                        String.format("Component '%s' not found for project '%s'", componentId, projectId)
                );
            }
            String catalogItemId = CatalogItemOperations.buildCatalogItemId(marketplaceComponent);
            CatalogItem catalogItem = marketplaceExternalService.getCatalogItem(catalogItemId);
            if (catalogItem == null) {
                log.info("Catalog item with id {} not found", catalogItemId);
                throw new CatalogItemNotFoundException(
                        String.format("Catalog item with id '%s' not found", catalogItemId)
                );
            }
            Component component = marketplaceMapper.mapMarketplaceComponentToV0Component(marketplaceComponent, catalogItem);
            log.info("Marketplace v0 component retrieved: {}", component);
            return component;
        } catch (MarketplaceException e) {
            log.error("Failed to retrieve component with id {} for project with id {}: {}", componentId, projectId, e.getMessage(), e);
            throw new ComponentRetrievalException(
                    String.format("Failed to retrieve component '%s' for project '%s': %s", componentId, projectId, e.getMessage()), e
            );
        }
    }

    public void provisionProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        try {
            List<ProvisionActionParameter> createComponentParameterList = marketplaceMapper.mapCreateComponentRequestToCreateComponentParameterList(createComponentRequest);
            boolean success = marketplaceExternalService.provisionProjectComponent(projectId, createComponentParameterList);
            if (!success) {
                log.error("Failed to create component in marketplace for project with id {}", projectId);
                throw new ComponentCreationException(
                        String.format("Failed to create component for project '%s'", projectId)
                );
            }
        } catch (MarketplaceException e) {
            if (isConflictCause(e)) {
                throw new ComponentAlreadyExistsException(e.getMessage(), e);
            }
            throw new ComponentCreationException(
                    String.format("Failed to create component for project '%s': %s", projectId, e.getMessage()), e
            );
        }
    }

    private boolean isConflictCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof HttpClientErrorException.Conflict) {
                return true;
            }
            current = current.getCause();
        }
        return false;
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
