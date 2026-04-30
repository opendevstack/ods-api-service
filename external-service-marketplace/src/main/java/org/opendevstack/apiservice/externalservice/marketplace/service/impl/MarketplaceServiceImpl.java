package org.opendevstack.apiservice.externalservice.marketplace.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.core.security.jwt.JwtUtils;
import org.opendevstack.apiservice.core.security.obo.OboTokenService;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClientFactory;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.ApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.api.CatalogItemsApi;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.api.ProjectComponentsApi;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.api.ProvisionResultsApi;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.api.ProvisionerActionsApi;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.CreateIncidentAction;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.NotifyProvisioningStatusUpdateRequest;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentExtendedInfo;
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
    private final OboTokenService oboTokenService;

    public MarketplaceServiceImpl(MarketplaceApiClientFactory clientFactory,
                                  OboTokenService oboTokenService) {
        this.clientFactory = clientFactory;
        this.oboTokenService = oboTokenService;
        log.info("MarketplaceServiceImpl initialized");
    }


    @Override
    public CatalogItem getCatalogItem(String catalogItemId) throws MarketplaceException {
        return getCatalogItem(getDefaultInstance(), catalogItemId);
    }

    @Override
    public CatalogItem getCatalogItem(String instanceName, String catalogItemId) throws MarketplaceException {
        log.debug("Marketplace service GET catalog item with id {} in instance {} ", catalogItemId, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = getAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            CatalogItemsApi catalogItemsApi = new CatalogItemsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());
            return catalogItemsApi.getCatalogItemById(catalogItemId);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Catalog item with id '{}' not found in Marketplace instance '{}'",
                    catalogItemId, instanceName);
            return null;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when getting catalog item '%s' in instance '%s'",
                            catalogItemId, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to retrieve catalog item '%s' in instance '%s'",
                            catalogItemId, instanceName), e);
        }
    }

    @Override
    public CatalogItem getCatalogItemBySlug(String slug) throws MarketplaceException {
        return getCatalogItemBySlug(getDefaultInstance(), slug);
    }

    @Override
    public CatalogItem getCatalogItemBySlug(String instanceName, String slug) throws MarketplaceException {
        log.debug("Marketplace service GET catalog item with slug {} in instance {} ", slug, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = getAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            CatalogItemsApi catalogItemsApi = new CatalogItemsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());
            return catalogItemsApi.getCatalogItemBySlug(slug);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Catalog item with slug '{}' not found in Marketplace instance '{}'",
                    slug, instanceName);
            return null;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new MarketplaceException(
                    String.format("Access denied when getting catalog item with slug '%s' in instance '%s'",
                            slug, instanceName), e);
        } catch (RestClientException e) {
            throw new MarketplaceException(
                    String.format("Failed to retrieve catalog item with slug '%s' in instance '%s'",
                            slug, instanceName), e);
        }
    }

    @Override
    public ProjectComponentExtendedInfo getProjectComponent(String projectId, String componentId) throws MarketplaceException {
        return getProjectComponent(getDefaultInstance(), projectId, componentId);
    }

    @Override
    public ProjectComponentExtendedInfo getProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceException {
        log.debug("Marketplace service GET component with id {} for project {} in instance {} ", componentId, projectId, instanceName);
        try {
            MarketplaceApiClient marketplaceClient = getAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            ProjectComponentsApi projectComponentsApi = new ProjectComponentsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProjectComponentsBaseUrl());
            return projectComponentsApi.getProjectComponentById(projectId, componentId);
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
    public boolean provisionProjectComponent(String instanceName, 
                                             String projectId,
                                             List<ProvisionActionParameter> params) 
            throws MarketplaceException {
        log.debug("Marketplace service PROVISION component for project {}: ", projectId);
        try {
            MarketplaceApiClient marketplaceClient = getAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();
            MarketplaceInstanceConfig config = marketplaceClient.getConfig();

            String provisionerActionsBaseUrl = config.getProvisionerActionsBaseUrl();

            ProvisionAction provisionAction = new ProvisionAction();
            provisionAction.setId("PROVISION");
            provisionAction.addParametersItem(new ProvisionActionParameter().name("project_key").type("string").value(projectId));

            params.forEach(provisionAction::addParametersItem);

            ProvisionerActionsApi provisionerActionsApi = new ProvisionerActionsApi(apiClient);
            apiClient.setBasePath(provisionerActionsBaseUrl);

            ProvisionActionResponse response = provisionerActionsApi.triggerProvisionAction(provisionAction);
            return !Boolean.TRUE.equals(response.getFailed());
        } catch (HttpClientErrorException.Conflict e) {
            throw new MarketplaceException("This component name already exists, please choose another name.", e);
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
            MarketplaceApiClient marketplaceClient = getAuthenticatedClient(instanceName);
            ApiClient apiClient = marketplaceClient.getApiClient();

            ProvisionResultsApi provisionResultsApi = new ProvisionResultsApi(apiClient);
            apiClient.setBasePath(marketplaceClient.getConfig().getProvisionerActionsBaseUrl());
            log.debug("Api client base path: {}", apiClient.getBasePath());

            CreateIncidentAction deleteAction = new CreateIncidentAction();
            ProvisionActionResponse response = provisionResultsApi.createIncident(projectId, componentId, deleteAction);
            return !Boolean.TRUE.equals(response.getFailed());
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
            MarketplaceApiClient marketplaceClient = getAuthenticatedClient(instanceName);
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

    /**
     * Creates a {@link MarketplaceApiClient} authenticated with an OBO token
     * obtained from the current request's JWT.
     */
    private MarketplaceApiClient getAuthenticatedClient(String instanceName) throws MarketplaceException {
        MarketplaceApiClient client = clientFactory.getClient(instanceName);
        String oboScope = client.getConfig().getOboScope();
        if (oboScope == null || oboScope.isBlank()) {
            throw new MarketplaceException(
                    String.format("OBO scope not configured for Marketplace instance '%s'", instanceName));
        }
        String assertion = JwtUtils.getTokenValue();
        final String oboToken;
        try {
            oboToken = oboTokenService.exchangeToken(assertion, oboScope);
        } catch (RuntimeException ex) {
            throw new MarketplaceException(
                    String.format(
                            "Failed to exchange OBO token for Marketplace instance '%s' with scope '%s'",
                            instanceName, oboScope),
                    ex);
        }
        client.setBearerToken(oboToken);
        return client;
    }
}
