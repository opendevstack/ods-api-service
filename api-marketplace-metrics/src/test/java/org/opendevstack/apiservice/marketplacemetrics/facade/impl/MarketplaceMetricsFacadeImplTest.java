package org.opendevstack.apiservice.marketplacemetrics.facade.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.marketplacemetrics.mapper.MarketplaceMetricsMapper;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
    void fakeTest() {
        assertThat(true).isTrue();
    }

}