package org.opendevstack.apiservice.marketplacemetrics.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.marketplacemetrics.exception.MarketplaceMetricsException;
import org.opendevstack.apiservice.marketplacemetrics.facade.MarketplaceMetricsFacade;
import org.opendevstack.apiservice.marketplacemetrics.model.CatalogItem;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceCatalogItemsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketplaceMetricsControllerTest {

    @Mock
    MarketplaceMetricsFacade marketplaceMetricsFacade;

    MarketplaceMetricsController controller;

    @BeforeEach
    void setUp() {
        controller = new MarketplaceMetricsController(marketplaceMetricsFacade);
    }

    @Test
    void GivenFacadeReturnsCatalogMetrics_whenGetMarketplaceCatalogItemsMetrics_ThenReturnOkWithBody() throws MarketplaceMetricsException {
        //given
        MarketplaceCatalogItemsMetrics metrics = new MarketplaceCatalogItemsMetrics()
                .addDataItem(new CatalogItem().id("id1").title("title1"));
        when(marketplaceMetricsFacade.getMarketplaceCatalogItemsMetrics()).thenReturn(metrics);

        //when
        ResponseEntity<MarketplaceCatalogItemsMetrics> response = controller.getMarketplaceCatalogItemsMetrics();

        //then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(metrics);
    }

    @Test
    void GivenFacadeReturnsNull_whenGetMarketplaceCatalogItemsMetrics_ThenReturnNotFound() throws MarketplaceMetricsException {
        //given
        when(marketplaceMetricsFacade.getMarketplaceCatalogItemsMetrics()).thenReturn(null);

        //when
        ResponseEntity<MarketplaceCatalogItemsMetrics> response = controller.getMarketplaceCatalogItemsMetrics();

        //then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void GivenFacadeReturnsEmptyDataList_whenGetMarketplaceCatalogItemsMetrics_ThenReturnNotFound() throws MarketplaceMetricsException {
        //given
        MarketplaceCatalogItemsMetrics emptyMetrics = new MarketplaceCatalogItemsMetrics();
        when(marketplaceMetricsFacade.getMarketplaceCatalogItemsMetrics()).thenReturn(emptyMetrics);

        //when
        ResponseEntity<MarketplaceCatalogItemsMetrics> response = controller.getMarketplaceCatalogItemsMetrics();

        //then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void GivenFacadeThrowsException_whenGetMarketplaceCatalogItemsMetrics_ThenThrowMarketplaceMetricsException() throws MarketplaceMetricsException {
        //given
        when(marketplaceMetricsFacade.getMarketplaceCatalogItemsMetrics()).thenThrow(new MarketplaceMetricsException("boom"));

        //when //then
        assertThatThrownBy(() -> controller.getMarketplaceCatalogItemsMetrics())
                .isInstanceOf(MarketplaceMetricsException.class)
                .hasMessageContaining("boom");
    }

    @Test
    void GivenFacadeReturnsProjectComponentsMetrics_whenGetMarketplaceProjectComponentsMetrics_ThenReturnOkWithBody() throws MarketplaceMetricsException {
        //given
        MarketplaceProjectComponentsMetrics metrics = new MarketplaceProjectComponentsMetrics()
                .addDataItem(new MarketplaceProjectComponentMetrics().projectKey("P1").componentId("C1"));
        when(marketplaceMetricsFacade.getMarketplaceProjectComponentsMetrics(1, 10)).thenReturn(metrics);

        //when
        ResponseEntity<MarketplaceProjectComponentsMetrics> response = controller.getMarketplaceProjectComponentsMetrics(1, 10);

        //then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(metrics);
    }

    @Test
    void GivenFacadeReturnsNull_whenGetMarketplaceProjectComponentsMetrics_ThenReturnNotFound() throws MarketplaceMetricsException {
        //given
        when(marketplaceMetricsFacade.getMarketplaceProjectComponentsMetrics(0, 0)).thenReturn(null);

        //when
        ResponseEntity<MarketplaceProjectComponentsMetrics> response = controller.getMarketplaceProjectComponentsMetrics(0, 0);

        //then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void GivenFacadeThrowsException_whenGetMarketplaceProjectComponentsMetrics_ThenThrowMarketplaceMetricsException() throws MarketplaceMetricsException {
        //given
        when(marketplaceMetricsFacade.getMarketplaceProjectComponentsMetrics(null, null)).thenThrow(new MarketplaceMetricsException("err"));

        //when //then
        assertThatThrownBy(() -> controller.getMarketplaceProjectComponentsMetrics(null, null))
                .isInstanceOf(MarketplaceMetricsException.class)
                .hasMessageContaining("err");
    }
}


