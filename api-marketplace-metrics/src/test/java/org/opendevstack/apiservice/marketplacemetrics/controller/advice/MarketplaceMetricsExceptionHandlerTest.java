package org.opendevstack.apiservice.marketplacemetrics.controller.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.marketplacemetrics.controller.MarketplaceMetricsController;
import org.opendevstack.apiservice.marketplacemetrics.exception.MarketplaceMetricsException;
import org.opendevstack.apiservice.marketplacemetrics.facade.MarketplaceMetricsFacade;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MarketplaceMetricsExceptionHandlerTest {

    @Mock
    private MarketplaceMetricsFacade marketplaceMetricsFacade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MarketplaceMetricsController controller = new MarketplaceMetricsController(marketplaceMetricsFacade);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new MarketplaceMetricsExceptionHandler())
                .build();
    }

    @Test
    void GivenFacadeThrowsMarketplaceMetricsException_whenGetCatalogItemsMetrics_thenReturnInternalServerError() throws Exception {
        String errorMessage = "Failed to retrieve marketplace catalog items metrics.";
        when(marketplaceMetricsFacade.getMarketplaceCatalogItemsMetrics())
                .thenThrow(new MarketplaceMetricsException(errorMessage));

        mockMvc.perform(get("/api/v1/projects/metrics/marketplace/catalog-items"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    void GivenFacadeThrowsMarketplaceMetricsException_whenGetProjectComponentsMetrics_thenReturnInternalServerError() throws Exception {
        String errorMessage = "Failed to retrieve marketplace project components metrics.";
        when(marketplaceMetricsFacade.getMarketplaceProjectComponentsMetrics(0, 20))
                .thenThrow(new MarketplaceMetricsException(errorMessage));

        mockMvc.perform(get("/api/v1/projects/metrics/marketplace/project-components"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}

