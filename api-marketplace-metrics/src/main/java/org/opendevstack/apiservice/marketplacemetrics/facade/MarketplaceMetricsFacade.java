package org.opendevstack.apiservice.marketplacemetrics.facade;

import org.opendevstack.apiservice.marketplacemetrics.exception.MarketplaceMetricsException;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;

public interface MarketplaceMetricsFacade {
    MarketplaceProjectComponentsMetrics getMarketplaceProjectComponentsMetrics(Integer page, Integer size) throws MarketplaceMetricsException;
}
