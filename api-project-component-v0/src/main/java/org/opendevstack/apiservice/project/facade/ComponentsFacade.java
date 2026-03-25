package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.project.mock.ComponentMockService;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentsFacade {

    private final ComponentMockService marketplaceExternalService;

    public Component getProjectComponent(String projectId, String componentId) {
        return marketplaceExternalService.getProjectComponent(projectId, componentId);
    }

    public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        return marketplaceExternalService.createProjectComponent(projectId, createComponentRequest);
    }
}
