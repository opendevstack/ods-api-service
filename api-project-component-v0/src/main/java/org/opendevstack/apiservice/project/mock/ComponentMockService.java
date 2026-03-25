package org.opendevstack.apiservice.project.mock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.ExternalService;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class ComponentMockService implements ExternalService {

    @Override
    public boolean isHealthy() {
        return true;
    }

    private Map<ComposedId, Component> mockComponentsCache = Collections.synchronizedMap(new HashMap<>());

    public Component getProjectComponent(String projectId, String componentId) {
        log.info("Get component with id '" + componentId + "' for project '" + projectId + "'");
        ComposedId composedId = new ComposedId(projectId, componentId);
        return mockComponentsCache.get(composedId);
    }

    public Component createProjectComponent(String projectId, CreateComponentRequest createComponentRequest) {
        log.info("Creating component for project '" + projectId + "'" + " with request: " + createComponentRequest);
        Component mockComponent = new Component();
        mockComponent.setId(generateNextId());
        mockComponent.setName("Mock Component " + mockComponent.getId() + " for project " + projectId);
        synchronized (mockComponentsCache) {
            ComposedId composedId = new ComposedId(projectId, mockComponent.getId());
            mockComponentsCache.put(composedId, mockComponent);
        }
        return mockComponent;
    }

    private String generateNextId() {
        return "mock-component-id-" + (mockComponentsCache.size() + 1);
    }

    class ComposedId {
        private String projectId;
        private String componentId;

        public ComposedId(String projectId, String componentId) {
            this.projectId = projectId;
            this.componentId = componentId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComposedId that = (ComposedId) o;

            if (!projectId.equals(that.projectId)) return false;
            return componentId.equals(that.componentId);
        }

        @Override
        public int hashCode() {
            int result = projectId.hashCode();
            result = 31 * result + componentId.hashCode();
            return result;
        }
    }
}
