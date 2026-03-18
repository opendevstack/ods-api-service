package org.opendevstack.apiservice.project.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentsService {

    private final MarketplaceExternalServicePlaceholder marketplaceExternalService;

    public Component getProjectComponent(String projectId, String componentId) {
        return marketplaceExternalService.getProjectComponent(projectId, componentId);
    }

    public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        return marketplaceExternalService.createProjectComponent(projectId, createComponentRequest);
    }

    //TODO replace this with actual external service implementations and remove the placeholder
    @Service
    class MarketplaceExternalServicePlaceholder implements ExternalService {

        @Override
        public boolean isHealthy() {
            return false;
        }

        public Component getProjectComponent(String projectId, String componentId) {
            return null;
        }

        public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
            return null;
        }
    }
}
