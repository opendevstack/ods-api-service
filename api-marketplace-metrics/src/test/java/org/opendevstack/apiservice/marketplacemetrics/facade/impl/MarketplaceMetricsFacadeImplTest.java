package org.opendevstack.apiservice.marketplacemetrics.facade.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListResponse;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.marketplacemetrics.exception.MarketplaceMetricsException;
import org.opendevstack.apiservice.marketplacemetrics.mapper.MarketplaceMetricsMapper;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MarketplaceMetricsFacadeImplTest {

    private MarketplaceService marketplaceService;
    private MarketplaceMetricsMapper mapper;

    private MarketplaceMetricsFacadeImpl sut;

    @BeforeEach
    void setup() {
        marketplaceService = mock(MarketplaceService.class);
        mapper = mock(MarketplaceMetricsMapper.class);

        sut = new MarketplaceMetricsFacadeImpl(marketplaceService, mapper);

        // Reset security context before each test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void get_marketplace_project_components_metrics_returns_mapped_result_when_service_succeeds() throws Exception {
        Integer page = 1;
        Integer size = 25;
        ProjectComponentListResponse serviceResponse = mock(ProjectComponentListResponse.class);
        MarketplaceProjectComponentsMetrics mappedResponse = mock(MarketplaceProjectComponentsMetrics.class);

        when(marketplaceService.getAllProjectComponents(page, size)).thenReturn(serviceResponse);
        when(mapper.toApiModel(serviceResponse)).thenReturn(mappedResponse);

        MarketplaceProjectComponentsMetrics result = sut.getMarketplaceProjectComponentsMetrics(page, size);

        assertThat(result).isSameAs(mappedResponse);
        verify(marketplaceService).getAllProjectComponents(page, size);
        verify(mapper).toApiModel(serviceResponse);
    }

    @Test
    void get_marketplace_project_components_metrics_wraps_marketplace_exception() throws Exception {
        Integer page = 2;
        Integer size = 10;
        MarketplaceException cause = new MarketplaceException("marketplace unavailable");

        when(marketplaceService.getAllProjectComponents(page, size)).thenThrow(cause);

        assertThatThrownBy(() -> sut.getMarketplaceProjectComponentsMetrics(page, size))
                .isInstanceOf(MarketplaceMetricsException.class)
                .hasMessage("Failed to retrieve marketplace project components metrics.")
                .hasCause(cause);

        verify(marketplaceService).getAllProjectComponents(page, size);
        verifyNoInteractions(mapper);
    }

}