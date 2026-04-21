package org.opendevstack.apiservice.project.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.project.exception.ComponentCreationException;
import org.opendevstack.apiservice.project.exception.ComponentNotFoundException;
import org.opendevstack.apiservice.project.mapper.MarketplaceMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.ComponentsStatusDTO;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestCreateComponentRequest;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestMarketplaceComponent;

@ExtendWith(MockitoExtension.class)
class ComponentsFacadeTest {

    private final MarketplaceMapper marketplaceMapper = Mappers.getMapper(MarketplaceMapper.class);

    @Mock
    private MarketplaceService marketplaceExternalService;

    private ComponentsFacade componentsFacade;

    private AutoCloseable openMocks;

    @BeforeEach
    void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        componentsFacade = new ComponentsFacade(marketplaceExternalService, marketplaceMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void get_project_component_returns_mapped_component_when_marketplace_returns_data() throws MarketplaceException {
        ProjectComponentInfo marketplaceComponent = buildTestMarketplaceComponent();

        when(marketplaceExternalService.getProjectComponent("testProject", "testComponent"))
                .thenReturn(marketplaceComponent);

        Component retrievedComponent = componentsFacade.getProjectComponent("testProject", "testComponent");

        assertThat(retrievedComponent).isNotNull();
        assertThat(retrievedComponent.getId()).isEqualTo(marketplaceComponent.getComponentId().toString());
        assertThat(retrievedComponent.getStatus()).isEqualTo(ComponentsStatusDTO.RUNNING);
        verify(marketplaceExternalService).getProjectComponent("testProject", "testComponent");
    }

    @Test
    void get_project_component_throws_not_found_when_marketplace_returns_null() throws MarketplaceException {
        when(marketplaceExternalService.getProjectComponent("testProject", "testComponent"))
                .thenReturn(null);

        assertThatThrownBy(() -> componentsFacade.getProjectComponent("testProject", "testComponent"))
                .isInstanceOf(ComponentNotFoundException.class)
                .hasMessage("Component 'testComponent' not found for project 'testProject'");
        verify(marketplaceExternalService).getProjectComponent("testProject", "testComponent");
    }

    @Test
    void create_project_component_returns_mapped_component_when_marketplace_creates_component() throws MarketplaceException {
        ProjectComponentInfo marketplaceComponent = buildTestMarketplaceComponent();
        CreateComponentRequest request = buildTestCreateComponentRequest();

        when(marketplaceExternalService.provisionProjectComponent(eq("testProject"), any(List.class)))
                .thenReturn(true); //TODO fix this to return more info

        Component createdComponent = componentsFacade.provisionProjectComponent("testProject", request);

        //TODO fix this to return more info and assert on it
//        assertThat(createdComponent).isNotNull();
//        assertThat(createdComponent.getId()).isEqualTo(marketplaceComponent.getComponentId().toString());
//        assertThat(createdComponent.getStatus()).isEqualTo(ComponentsStatusDTO.RUNNING);
        verify(marketplaceExternalService).provisionProjectComponent(eq("testProject"), any(List.class));
    }

    @Test
    void create_project_component_throws_creation_exception_when_marketplace_returns_null() throws MarketplaceException {
        CreateComponentRequest request = buildTestCreateComponentRequest();

        when(marketplaceExternalService.provisionProjectComponent(eq("testProject"), any(List.class)))
                .thenReturn(false); //TODO fix this to return more info

        assertThatThrownBy(() -> componentsFacade.provisionProjectComponent("testProject", request))
                .isInstanceOf(ComponentCreationException.class)
                .hasMessage("Failed to create component for project 'testProject'");
        verify(marketplaceExternalService).provisionProjectComponent(eq("testProject"), any(List.class));
    }
}