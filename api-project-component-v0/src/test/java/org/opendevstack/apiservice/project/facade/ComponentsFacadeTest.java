package org.opendevstack.apiservice.project.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.project.mapper.MarketplaceMapper;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestCreateComponentRequest;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestMarketplaceComponent;

@ExtendWith(MockitoExtension.class)
class ComponentsFacadeTest {

    private final MarketplaceMapper marketplaceMapper = Mappers.getMapper(MarketplaceMapper.class);

    @Mock
    private MarketplaceService marketplaceExternalService;

    private ComponentsFacade componentsFacade;

    @BeforeEach
    void setup() {
        componentsFacade = new ComponentsFacade(marketplaceExternalService, marketplaceMapper);
    }

    @Test
    void testGetProjectComponent_whenSuccess_thenReturnCorrectComponent() throws Exception {
        ProjectComponentInfo testComponent = buildTestMarketplaceComponent();

        when(marketplaceExternalService.getProjectComponent(anyString(), eq("testId")))
                .thenReturn(testComponent);

        Component retrievedComponent = componentsFacade.getProjectComponent("testId", "testId");
        assertThat(retrievedComponent.getId()).isEqualTo(testComponent.getComponentId());
        assertThat(retrievedComponent.getStatus()).isEqualTo(testComponent.getStatus());
    }

    @Test
    void testGetProjectComponent_whenNoComponentFound_thenReturnNull() throws Exception {
        when(marketplaceExternalService.getProjectComponent(anyString(), eq("testId")))
                .thenReturn(null);

        Component retrievedComponent = componentsFacade.getProjectComponent("testId", "testId");
        assertThat(retrievedComponent).isNull();
    }

    @Test
    void testCreateProjectComponent_whenSuccess_thenReturnCorrectComponent() throws Exception {
        ProjectComponentInfo testComponent = buildTestMarketplaceComponent();
        CreateComponentRequest testRequest = buildTestCreateComponentRequest();

        when(marketplaceExternalService.provisionProjectComponent(anyString(), any(List.class)))
                .thenReturn(true);

        boolean result = componentsFacade.createProjectComponent("testId", testRequest);
        assertThat(result).isTrue();
    }


    @Test
    void testCreateProjectComponent_whenFailure_thenReturnNull() throws Exception {
        CreateComponentRequest testRequest = buildTestCreateComponentRequest();

        when(marketplaceExternalService.provisionProjectComponent(anyString(), any(List.class)))
                .thenReturn(false);

        boolean result = componentsFacade.createProjectComponent("testId", testRequest);
        assertThat(result).isFalse();
    }
}