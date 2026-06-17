package org.opendevstack.apiservice.marketplacemetrics.mapper;

import org.openapitools.jackson.nullable.JsonNullable;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListResponse;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.Pagination;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetricsPagination;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper class for converting between external service and API models.
 */
@Component
public class MarketplaceMetricsMapper {

    /**
     * Converts external service ProjectComponentListResponse to API MarketplaceProjectComponentsMetrics.
     *
     * @param projectComponentListResponse the external service project components metrics
     * @return the API MarketplaceProjectComponentsMetrics
     */
    public MarketplaceProjectComponentsMetrics toApiModel(ProjectComponentListResponse projectComponentListResponse) {

        if (projectComponentListResponse == null) {
            return null;
        }

        MarketplaceProjectComponentsMetrics marketplaceProjectComponentsMetrics = new MarketplaceProjectComponentsMetrics();

        if (projectComponentListResponse.getData() != null) {
            marketplaceProjectComponentsMetrics.setData(
                    projectComponentListResponse.getData().stream()
                            .map(this::toApiMarketplaceProjectComponentMetric)
                            .collect(Collectors.toList())
            );
        }

        marketplaceProjectComponentsMetrics.setPagination(this.toApiMarketplaceProjectComponentsMetricsPagination(projectComponentListResponse.getPagination()));

        return marketplaceProjectComponentsMetrics;
    }

    /**
     * Converts external service ProjectComponentListItem to API MarketplaceProjectComponentMetrics.
     *
     * @param projectComponentListItem the external service ProjectComponentListItem
     * @return the API MarketplaceProjectComponentMetrics
     */
    private MarketplaceProjectComponentMetrics toApiMarketplaceProjectComponentMetric(ProjectComponentListItem projectComponentListItem) {

        if (projectComponentListItem == null) {
            return null;
        }

        MarketplaceProjectComponentMetrics marketplaceProjectComponentMetrics = new MarketplaceProjectComponentMetrics();
        marketplaceProjectComponentMetrics.setComponentId(projectComponentListItem.getComponentId());
        marketplaceProjectComponentMetrics.setProjectKey(projectComponentListItem.getProjectKey());
        marketplaceProjectComponentMetrics.setCaller(projectComponentListItem.getCaller());
        marketplaceProjectComponentMetrics.setCreatedAt(projectComponentListItem.getCreatedAt());
        marketplaceProjectComponentMetrics.setUpdatedAt(projectComponentListItem.getUpdatedAt());
        marketplaceProjectComponentMetrics.setCatalogItemSlug(projectComponentListItem.getCatalogItemSlug());

        return marketplaceProjectComponentMetrics;
    }

    /**
     * Converts external service Pagination to API MarketplaceProjectComponentsMetricsPagination.
     *
     * @param pagination the external service Pagination
     * @return the API MarketplaceProjectComponentsMetricsPagination
     */
    private MarketplaceProjectComponentsMetricsPagination toApiMarketplaceProjectComponentsMetricsPagination(Pagination pagination) {

        if (pagination == null) {
            return null;
        }

        MarketplaceProjectComponentsMetricsPagination marketplaceProjectComponentsMetricsPagination = new MarketplaceProjectComponentsMetricsPagination();
        marketplaceProjectComponentsMetricsPagination.setNext(JsonNullable.of(pagination.getNext()));
        marketplaceProjectComponentsMetricsPagination.setPage(pagination.getPage());
        marketplaceProjectComponentsMetricsPagination.setPrevious(JsonNullable.of(pagination.getPrevious()));
        marketplaceProjectComponentsMetricsPagination.setSize(pagination.getSize());
        marketplaceProjectComponentsMetricsPagination.setTotalPages(pagination.getTotalPages());
        marketplaceProjectComponentsMetricsPagination.setTotalElements(pagination.getTotalElements());

        return marketplaceProjectComponentsMetricsPagination;
    }
}

