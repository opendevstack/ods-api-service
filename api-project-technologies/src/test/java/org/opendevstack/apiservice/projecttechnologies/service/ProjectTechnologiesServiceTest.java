package org.opendevstack.apiservice.projecttechnologies.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceNotFoundException;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.projecttechnologies.model.ProjectTechnology;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectTechnologiesServiceTest {

    @Mock
    private MarketplaceService marketplaceService;

    private ProjectTechnologiesService service;

    @BeforeEach
    void setUp() {
        service = new ProjectTechnologiesService(marketplaceService);
    }

    @Test
    void getProjectTechnologiesMapsMarketplaceItems() throws Exception {
        String projectKey = "PROJECT";
        org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectTechnology marketplaceTechnology =
                new org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectTechnology()
                        .slug("java")
                        .name("Java");
        when(marketplaceService.getProjectTechnologies(projectKey)).thenReturn(List.of(marketplaceTechnology));

        List<ProjectTechnology> result = service.getProjectTechnologies(projectKey);

        assertThat(result).containsExactly(new ProjectTechnology().slug("java").name("Java"));
        verify(marketplaceService).getProjectTechnologies(projectKey);
    }

    @Test
    void getProjectTechnologiesTranslatesMarketplaceNotFoundTo404() throws Exception {
        String projectKey = "MISSING";
        MarketplaceNotFoundException cause = new MarketplaceNotFoundException("Project not found", null);
        when(marketplaceService.getProjectTechnologies(projectKey)).thenThrow(cause);

        assertThatThrownBy(() -> service.getProjectTechnologies(projectKey))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getProjectTechnologiesTranslatesOtherMarketplaceErrorsTo502() throws Exception {
        String projectKey = "PROJECT";
        MarketplaceException cause = new MarketplaceException("Marketplace unavailable");
        when(marketplaceService.getProjectTechnologies(projectKey)).thenThrow(cause);

        assertThatThrownBy(() -> service.getProjectTechnologies(projectKey))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }
}