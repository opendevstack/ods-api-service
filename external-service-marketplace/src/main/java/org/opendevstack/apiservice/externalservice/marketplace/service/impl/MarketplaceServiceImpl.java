package org.opendevstack.apiservice.externalservice.marketplace.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClientFactory;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
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
    public ProjectComponentInfo getProjectComponent(String projectId, String componentId) throws MarketplaceException {
        return getProjectComponent(getDefaultInstance(), projectId, componentId);
    }

    @Override
    public ProjectComponentInfo getProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException {
        log.debug("Marketplace service GET component with id {} for project {} in instance {} ", componentId, projectId, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            ProjectComponentsApi projectComponentsApi = new ProjectComponentsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());
            List<ProjectComponentInfo> components = projectComponentsApi.getProjectComponents(projectId);
            if (components == null || components.isEmpty()) {
                return null;
            }
            return components.stream()
                    .filter(component -> component.getComponentId().equals(componentId))
                    .findFirst()
                    .orElse(null);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Component with id '{}' not found in Marketplace instance '{}' for project '{}'",
                    componentId, instanceName, projectId);
            return null;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when getting project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to retrieve project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        }
    }

    @Override
    public boolean provisionProjectComponent(String projectId, List<ProvisionActionParameter> params) throws MarketplaceException {
        return provisionProjectComponent(getDefaultInstance(), projectId, params);
    }

    @Override
    public boolean provisionProjectComponent(String instanceName, String projectId, List<ProvisionActionParameter> params) throws MarketplaceException {
        log.debug("Marketplace service PROVISION component for project {}: ", projectId);
        try {
            MarketplaceApiClient marketplaceClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();

            ProvisionAction provisionAction = new ProvisionAction();
            provisionAction.setId("PROVISION");
            provisionAction.addParametersItem(new ProvisionActionParameter().name("project_key").type("string").value(projectId));
            params.forEach(provisionAction::addParametersItem);

            ProvisionerActionsApi provisionerActionsApi = new ProvisionerActionsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProvisionerActionsBaseUrl());

            ProvisionActionResponse response = provisionerActionsApi.triggerProvisionAction(provisionAction);
            return !response.getFailed();
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when provisioning project component in project '%s' and instance '%s'",
                            projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to provision project component in project '%s' and instance '%s'",
                            projectId, instanceName), e);
        }
    }

    @Override
    public boolean deleteProjectComponent(String projectId, String componentId) throws MarketplaceException {
        return deleteProjectComponent(getDefaultInstance(), projectId, componentId);
    }

    @Override
    public boolean deleteProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException {
        log.debug("Marketplace service DELETE component {} for project {}: ", componentId, projectId);
        try {
            MarketplaceApiClient marketplaceClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();

            ProvisionResultsApi provisionResultsApi = new ProvisionResultsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProvisionerActionsBaseUrl());
            log.debug("Api client base path: {}", apiClient.getBasePath());

            CreateIncidentAction deleteAction = new CreateIncidentAction();
            ProvisionActionResponse response = provisionResultsApi.createIncident(projectId, componentId, deleteAction);
            return !response.getFailed();
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when deleting project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to delete project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        }
    }

    @Override
    public void registerProjectComponent(String projectId, String componentId) throws MarketplaceException {
        registerProjectComponent(getDefaultInstance(), projectId, componentId);
    }

    @Override
    public void registerProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException {
        log.debug("Marketplace service REGISTER component {} for project {}: ", componentId, projectId);
        try {
            MarketplaceApiClient marketplaceClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();

            ProvisionResultsApi provisionResultsApi = new ProvisionResultsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProvisionerActionsBaseUrl());
            log.debug("Api client base path: {}", apiClient.getBasePath());

            NotifyProvisioningStatusUpdateRequest registerRequest = new NotifyProvisioningStatusUpdateRequest();
            registerRequest.setComponentId(componentId);
            provisionResultsApi.notifyProvisioningStatusUpdate(projectId, "CREATED", registerRequest);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when registering project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to register project component '%s' in project '%s' and instance '%s'",
                            componentId, projectId, instanceName), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultInstance() throws MarketplaceException {
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
