package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceClientException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;

import java.util.List;
import java.util.Set;

public interface MarketplaceService extends ExternalService {

    Set<String> getAvailableInstances();

    boolean hasInstance(String instanceName);

    String getDefaultInstance() throws MarketplaceClientException;

    ProjectComponentInfo getProjectComponent(String projectId, String componentId) throws MarketplaceClientException;

    ProjectComponentInfo getProjectComponent(String instanceName, String projectId, String componentId) throws MarketplaceClientException;

    boolean provisionProjectComponent(String projectId, List<ProvisionActionParameter> params) throws MarketplaceClientException;

    boolean deleteProjectComponent(String projectId, String componentId) throws MarketplaceClientException;

    void registerProjectComponent(String projectId, String componentId) throws MarketplaceClientException;

}
