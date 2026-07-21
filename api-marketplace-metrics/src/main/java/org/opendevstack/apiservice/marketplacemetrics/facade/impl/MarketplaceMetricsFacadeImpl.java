package org.opendevstack.apiservice.marketplacemetrics.facade.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListResponse;
import org.opendevstack.apiservice.marketplacemetrics.exception.MarketplaceMetricsException;
import org.opendevstack.apiservice.marketplacemetrics.facade.MarketplaceMetricsFacade;
import org.opendevstack.apiservice.marketplacemetrics.mapper.MarketplaceMetricsMapper;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceCatalogItemsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MarketplaceMetricsFacadeImpl implements MarketplaceMetricsFacade {

    private final MarketplaceService marketplaceService;
    private final MarketplaceMetricsMapper mapper;

    public MarketplaceMetricsFacadeImpl(MarketplaceService marketplaceService, MarketplaceMetricsMapper mapper) {
        this.marketplaceService = marketplaceService;
        this.mapper = mapper;
    }

    @Override
    public MarketplaceProjectComponentsMetrics getMarketplaceProjectComponentsMetrics(Integer page, Integer size) throws MarketplaceMetricsException {
        try {
            ProjectComponentListResponse allProjectComponents =
                    marketplaceService.getAllProjectComponents(page, size);
            return mapper.toApiModel(allProjectComponents);
        } catch (MarketplaceException e) {
            throw new MarketplaceMetricsException("Failed to retrieve marketplace project components metrics.", e);
        }
    }

    @Override
    public MarketplaceCatalogItemsMetrics getMarketplaceCatalogItemsMetrics() throws MarketplaceMetricsException {
        try {
            var allCatalogItems = marketplaceService.getAllCatalogItems();
            return mapper.toApiModel(allCatalogItems);
        } catch (MarketplaceException e) {
            throw new MarketplaceMetricsException("Failed to retrieve marketplace catalog items metrics.", e);
        }
    }
}
