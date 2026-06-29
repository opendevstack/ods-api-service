package org.opendevstack.apiservice.marketplacemetrics.controller;

import lombok.AllArgsConstructor;
import org.opendevstack.apiservice.marketplacemetrics.api.MarketplaceMetricsApi;
import org.opendevstack.apiservice.marketplacemetrics.exception.MarketplaceMetricsException;
import org.opendevstack.apiservice.marketplacemetrics.facade.MarketplaceMetricsFacade;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceCatalogItemsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@AllArgsConstructor
public class MarketplaceMetricsController implements MarketplaceMetricsApi {

    MarketplaceMetricsFacade marketplaceMetricsFacade;

    @CrossOrigin(origins = "*")
    @GetMapping("/metrics/marketplace/catalog-items")
    @Override
    public ResponseEntity<MarketplaceCatalogItemsMetrics> getMarketplaceCatalogItemsMetrics() {
        MarketplaceCatalogItemsMetrics catalogItemsMetrics;
        try {
            catalogItemsMetrics = marketplaceMetricsFacade.getMarketplaceCatalogItemsMetrics();
        } catch (MarketplaceMetricsException e) {
            return sneakyThrow(e);
        }

        if (catalogItemsMetrics == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(catalogItemsMetrics);
        }
    }

    @Override
    @CrossOrigin(origins = "*")
    @GetMapping("/metrics/marketplace/project-components")
    public ResponseEntity<MarketplaceProjectComponentsMetrics> getMarketplaceProjectComponentsMetrics(Integer page, Integer size) {
        MarketplaceProjectComponentsMetrics marketplaceProjectComponentsMetrics;
        try {
            marketplaceProjectComponentsMetrics = marketplaceMetricsFacade.getMarketplaceProjectComponentsMetrics(page, size);
        } catch (MarketplaceMetricsException e) {
            return sneakyThrow(e);
        }

        if (marketplaceProjectComponentsMetrics == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(marketplaceProjectComponentsMetrics);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, R> R sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
