package org.opendevstack.apiservice.marketplacemetrics.controller;

import lombok.AllArgsConstructor;
import org.opendevstack.apiservice.marketplacemetrics.api.MarketplaceMetricsApi;
import org.opendevstack.apiservice.marketplacemetrics.facade.MarketplaceMetricsFacade;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceCatalogItemsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metrics")
@AllArgsConstructor
public class MarketplaceMetricsController implements MarketplaceMetricsApi {

    MarketplaceMetricsFacade marketplaceMetricsFacade;

    @Override
    public ResponseEntity<MarketplaceCatalogItemsMetrics> getMarketplaceCatalogItemsMetrics() {
        MarketplaceCatalogItemsMetrics catalogItemsMetrics = marketplaceMetricsFacade.getMarketplaceCatalogItemsMetrics();

        if (catalogItemsMetrics == null || catalogItemsMetrics.getData().isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(catalogItemsMetrics);
        }
    }

    @Override
    public ResponseEntity<MarketplaceProjectComponentsMetrics> getMarketplaceProjectComponentsMetrics(Integer page, Integer size) {
        MarketplaceProjectComponentsMetrics marketplaceProjectComponentsMetrics =
                marketplaceMetricsFacade.getMarketplaceProjectComponentsMetrics(page, size);

        if (marketplaceProjectComponentsMetrics == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(marketplaceProjectComponentsMetrics);
        }
    }
}
