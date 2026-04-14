package org.opendevstack.apiservice.externalservice.marketplace.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClientFactory;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceClientException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.ApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.api.ProjectComponentsApi;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.api.ProvisionResultsApi;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.api.ProvisionerActionsApi;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.CreateIncidentAction;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.NotifyProvisioningStatusUpdateRequest;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionAction;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionResponse;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MarketplaceServiceImpl implements MarketplaceService {

    private final MarketplaceApiClientFactory clientFactory;

    public MarketplaceServiceImpl(MarketplaceApiClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        log.info("MarketplaceServiceImpl initialized");
    }

    @Override
    public ProjectComponentInfo getProjectComponent(String projectId, String componentId) throws MarketplaceClientException {
        return getProjectComponent(getDefaultInstance(), projectId, componentId);
    }

    @Override
    public ProjectComponentInfo getProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceClientException {
        log.debug("Marketplace service GET component with id {} for project {} in instance {} ", componentId, projectId, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();

            ProjectComponentsApi projectComponentsApi = new ProjectComponentsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());

            return projectComponentsApi.getProjectComponents(projectId, marketplaceClient.getConfig().getAccessToken()).stream()
                    .filter(component -> component.getComponentId().equals(componentId))
                    .findFirst()
                    .orElse(null);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceClientException(
                    String.format("Access denied when checking project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceClientException(
                    String.format("Failed to check project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        }
    }

    @Override
    public boolean provisionProjectComponent(String projectId, List<ProvisionActionParameter> params) throws MarketplaceClientException {
        log.debug("Marketpalce service PROVISION component for project {}: ", projectId);
        ProvisionAction provisionAction = new ProvisionAction();
        provisionAction.setId("PROVISION");
        provisionAction.addParametersItem(new ProvisionActionParameter().name("project_key").type("string").value(projectId));
        params.forEach(provisionAction::addParametersItem);

        ProvisionerActionsApi provisionerActionsApi = new ProvisionerActionsApi();
        ProvisionActionResponse response = provisionerActionsApi.triggerProvisionAction(provisionAction);
        return !response.getFailed();
    }

    @Override
    public boolean deleteProjectComponent(String projectId, String componentId) throws MarketplaceClientException {
        log.debug("Marketpalce service DELETE component {} for project {}: ", componentId, projectId);
        CreateIncidentAction deleteAction = new CreateIncidentAction();

        ProvisionResultsApi provisionResultsApi = new ProvisionResultsApi();
        ProvisionActionResponse response = provisionResultsApi.createIncident(projectId, componentId, deleteAction);
        return !response.getFailed();
    }

    @Override
    public void registerProjectComponent(String projectId, String componentId) throws MarketplaceClientException {
        log.debug("Marketpalce service REGISTER component {} for project {}: ", componentId, projectId);
        NotifyProvisioningStatusUpdateRequest registerRequest = new NotifyProvisioningStatusUpdateRequest();
        registerRequest.setComponentId(componentId);

        ProvisionResultsApi provisionResultsApi = new ProvisionResultsApi();
        provisionResultsApi.notifyProvisioningStatusUpdate(projectId, "CREATED", registerRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultInstance() throws MarketplaceClientException {
        return clientFactory.getDefaultInstanceName();
    }

    /**
     * {@inheritDoc}
     *
     * Returns {@code false} (without throwing) if no instances are configured.
     */
    @Override
    public boolean isHealthy() {
        Set<String> instances = getAvailableInstances();
        if (instances.isEmpty()) {
            log.warn("No Marketplace instances configured – reporting unhealthy");
            return false;
        }
        return true;
    }

    @Override
    public Set<String> getAvailableInstances() {
        return clientFactory.getAvailableInstances();
    }

    @Override
    public boolean hasInstance(String instanceName) {
        return clientFactory.hasInstance(instanceName);
    }
}
