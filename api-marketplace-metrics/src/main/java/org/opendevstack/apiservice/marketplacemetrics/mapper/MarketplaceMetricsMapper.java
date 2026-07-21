package org.opendevstack.apiservice.marketplacemetrics.mapper;

import org.openapitools.jackson.nullable.JsonNullable;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.Pagination;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListResponse;
import org.opendevstack.apiservice.marketplacemetrics.model.CatalogItem;
import org.opendevstack.apiservice.marketplacemetrics.model.CatalogItemTag;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceCatalogItemsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetricsPagination;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                            .toList()
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
        marketplaceProjectComponentsMetricsPagination.setNext(JsonNullable.of(this.mapToCurrentApiUrlKeepingQueryParams(pagination.getNext())));
        marketplaceProjectComponentsMetricsPagination.setPage(pagination.getPage());
        marketplaceProjectComponentsMetricsPagination.setPrevious(JsonNullable.of(this.mapToCurrentApiUrlKeepingQueryParams(pagination.getPrevious())));
        marketplaceProjectComponentsMetricsPagination.setSize(pagination.getSize());
        marketplaceProjectComponentsMetricsPagination.setTotalPages(pagination.getTotalPages());
        marketplaceProjectComponentsMetricsPagination.setTotalElements(pagination.getTotalElements());

        return marketplaceProjectComponentsMetricsPagination;
    }

    private URI mapToCurrentApiUrlKeepingQueryParams(URI uri) {
        if (uri == null) {
            return null;
        }

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return uri;
        }

        return ServletUriComponentsBuilder.fromCurrentRequestUri()
                .replaceQuery(uri.getRawQuery())
                .build(true)
                .toUri();
    }

    public MarketplaceCatalogItemsMetrics toApiModel(List<org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem> allCatalogItems) {
        var marketplaceCatalogItemsMetrics = new MarketplaceCatalogItemsMetrics();

        var catalogItems = allCatalogItems.stream()
                .map(this::toApiModel)
                .toList();

        marketplaceCatalogItemsMetrics.setData(catalogItems);

        return marketplaceCatalogItemsMetrics;
    }

    public CatalogItem toApiModel(org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem catalogItem) {

        var tags = Optional.ofNullable(catalogItem.getTags()).orElse(Collections.emptyList())
                .stream()
                .map(this::toApiModel)
                .toList();

        var apiCatalogItem = new CatalogItem();

        apiCatalogItem.setId(catalogItem.getId());
        apiCatalogItem.setSlug(catalogItem.getSlug());
        apiCatalogItem.setPath(catalogItem.getPath());
        apiCatalogItem.setTitle(catalogItem.getTitle());
        apiCatalogItem.setShortDescription(catalogItem.getShortDescription());
        apiCatalogItem.setDescriptionFileId(catalogItem.getDescriptionFileId());
        apiCatalogItem.setImageFileId(catalogItem.getImageFileId());
        apiCatalogItem.setDescription(catalogItem.getDescription());
        apiCatalogItem.setItemSrc(catalogItem.getItemSrc());
        apiCatalogItem.setTags(tags);
        apiCatalogItem.setAuthors(catalogItem.getAuthors());
        apiCatalogItem.setDate(catalogItem.getDate());

        return apiCatalogItem;
    }

    public CatalogItemTag toApiModel(org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag catalogItemTag) {
        var options = Optional.ofNullable(catalogItemTag.getOptions()).orElse(Collections.emptySet())
                .stream()
                .toList();

        var apiCatalogItemTag = new CatalogItemTag();

        apiCatalogItemTag.setLabel(catalogItemTag.getLabel());
        apiCatalogItemTag.setOptions(options);

        return apiCatalogItemTag;
    }
}

