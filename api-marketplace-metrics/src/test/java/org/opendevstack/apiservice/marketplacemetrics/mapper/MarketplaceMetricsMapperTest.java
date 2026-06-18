package org.opendevstack.apiservice.marketplacemetrics.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.Pagination;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListResponse;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MarketplaceMetricsMapperTest {

    private MarketplaceMetricsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MarketplaceMetricsMapper();
    }

    @Test
    void to_api_model_returns_null_when_response_is_null() {
        assertThat(mapper.toApiModel(null)).isNull();
    }

    @Test
    void to_api_model_maps_all_fields_when_response_contains_data_and_pagination() {
        ProjectComponentListItem item = new ProjectComponentListItem();
        item.setComponentId("comp-1");
        item.setProjectKey("PRJ1");
        item.setCaller("user@example.com");
        item.setCatalogItemSlug("java-17");
        item.setCreatedAt(new BigDecimal("1707043200000"));
        item.setUpdatedAt(new BigDecimal("1707043300000"));

        Pagination pagination = new Pagination();
        pagination.setPage(1);
        pagination.setSize(20);
        pagination.setTotalPages(3);
        pagination.setTotalElements(57);
        pagination.setNext(URI.create("https://api.example.com/resources?page=2&size=20"));
        pagination.setPrevious(URI.create("https://api.example.com/resources?page=0&size=20"));

        ProjectComponentListResponse response = new ProjectComponentListResponse();
        response.setData(Arrays.asList(item));
        response.setPagination(pagination);

        MarketplaceProjectComponentsMetrics result = mapper.toApiModel(response);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);

        MarketplaceProjectComponentMetrics mappedItem = result.getData().get(0);
        assertThat(mappedItem.getComponentId()).isEqualTo("comp-1");
        assertThat(mappedItem.getProjectKey()).isEqualTo("PRJ1");
        assertThat(mappedItem.getCaller()).isEqualTo("user@example.com");
        assertThat(mappedItem.getCatalogItemSlug()).isEqualTo("java-17");
        assertThat(mappedItem.getCreatedAt()).isEqualByComparingTo("1707043200000");
        assertThat(mappedItem.getUpdatedAt()).isEqualByComparingTo("1707043300000");

        assertThat(result.getPagination()).isNotNull();
        assertThat(result.getPagination().getPage()).isEqualTo(1);
        assertThat(result.getPagination().getSize()).isEqualTo(20);
        assertThat(result.getPagination().getTotalPages()).isEqualTo(3);
        assertThat(result.getPagination().getTotalElements()).isEqualTo(57);
        assertThat(result.getPagination().getNext().isPresent()).isTrue();
        assertThat(result.getPagination().getNext().get())
                .isEqualTo(URI.create("https://api.example.com/resources?page=2&size=20"));
        assertThat(result.getPagination().getPrevious().isPresent()).isTrue();
        assertThat(result.getPagination().getPrevious().get())
                .isEqualTo(URI.create("https://api.example.com/resources?page=0&size=20"));
    }

    @Test
    void to_api_model_maps_null_item_in_data_list_as_null_entry() {
        ProjectComponentListResponse response = new ProjectComponentListResponse();
        response.setData(Arrays.asList((ProjectComponentListItem) null));

        MarketplaceProjectComponentsMetrics result = mapper.toApiModel(response);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0)).isNull();
    }

    @Test
    void to_api_model_keeps_default_data_and_sets_null_pagination_when_source_has_null_data_and_pagination() {
        ProjectComponentListResponse response = new ProjectComponentListResponse();
        response.setData(null);
        response.setPagination(null);

        MarketplaceProjectComponentsMetrics result = mapper.toApiModel(response);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
        assertThat(result.getPagination()).isNull();
    }
}

