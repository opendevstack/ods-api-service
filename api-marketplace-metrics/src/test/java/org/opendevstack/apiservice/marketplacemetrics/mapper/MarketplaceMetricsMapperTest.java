package org.opendevstack.apiservice.marketplacemetrics.mapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.Pagination;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentListResponse;
import org.opendevstack.apiservice.marketplacemetrics.model.CatalogItem;
import org.opendevstack.apiservice.marketplacemetrics.model.CatalogItemTag;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceCatalogItemsMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetrics;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarketplaceMetricsMapperTest {

    private MarketplaceMetricsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MarketplaceMetricsMapper();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void to_api_model_returns_null_when_response_is_null() {
        assertThat(mapper.toApiModel( (ProjectComponentListResponse) null)).isNull();
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
        response.setData(List.of(item));
        response.setPagination(pagination);

        MarketplaceProjectComponentsMetrics result = mapper.toApiModel(response);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);

        MarketplaceProjectComponentMetrics mappedItem = result.getData().getFirst();
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
        response.setData(Collections.singletonList(null));

        MarketplaceProjectComponentsMetrics result = mapper.toApiModel(response);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst()).isNull();
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

     @Test
     void to_api_model_for_catalog_items_list_throws_exception_when_list_is_null() {
         assertThatThrownBy(() -> mapper.toApiModel((List<org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem>) null))
                 .isInstanceOf(NullPointerException.class);
     }

     @Test
     void to_api_model_for_catalog_items_list_maps_empty_list() {
         List<org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem> emptyList = Collections.emptyList();

         MarketplaceCatalogItemsMetrics result = mapper.toApiModel(emptyList);

         assertThat(result).isNotNull();
         assertThat(result.getData()).isEmpty();
     }

     @Test
     void to_api_model_for_catalog_items_list_maps_items_correctly() {
         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem sourceItem = new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem();
         sourceItem.setId("item-1");
         sourceItem.setSlug("java-17");
         sourceItem.setPath("/path/to/item");
         sourceItem.setTitle("Java 17");
         sourceItem.setShortDescription("Short desc");
         sourceItem.setDescription("Full description");
         sourceItem.setItemSrc("http://example.com/item");
         sourceItem.setAuthors(List.of("author1", "author2"));

         MarketplaceCatalogItemsMetrics result = mapper.toApiModel(List.of(sourceItem));

         assertThat(result).isNotNull();
         assertThat(result.getData()).hasSize(1);

         CatalogItem mappedItem = result.getData().getFirst();
         assertThat(mappedItem.getId()).isEqualTo("item-1");
         assertThat(mappedItem.getSlug()).isEqualTo("java-17");
         assertThat(mappedItem.getPath()).isEqualTo("/path/to/item");
         assertThat(mappedItem.getTitle()).isEqualTo("Java 17");
         assertThat(mappedItem.getShortDescription()).isEqualTo("Short desc");
         assertThat(mappedItem.getDescription()).isEqualTo("Full description");
         assertThat(mappedItem.getItemSrc()).isEqualTo("http://example.com/item");
         assertThat(mappedItem.getAuthors()).containsExactly("author1", "author2");
     }

     @Test
     void to_api_model_for_catalog_item_returns_null_when_item_is_null() {
         assertThatThrownBy(() -> mapper.toApiModel((org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem) null))
                 .isInstanceOf(NullPointerException.class);
     }

     @Test
     void to_api_model_for_catalog_item_maps_all_fields_when_populated() {
         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem sourceItem = new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem();
         sourceItem.setId("item-1");
         sourceItem.setSlug("java-17");
         sourceItem.setPath("/path/to/item");
         sourceItem.setTitle("Java 17");
         sourceItem.setShortDescription("Short desc");
         sourceItem.setDescriptionFileId("file-id-1");
         sourceItem.setImageFileId("image-id-1");
         sourceItem.setDescription("Full description");
         sourceItem.setItemSrc("http://example.com/item");
         sourceItem.setAuthors(List.of("author1"));
         sourceItem.setDate(OffsetDateTime.now());

         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag sourceTag =
             new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag();
         sourceTag.setLabel("version");
         sourceTag.setOptions(Set.of("1.0", "2.0"));
         sourceItem.setTags(List.of(sourceTag));

         CatalogItem result = mapper.toApiModel(sourceItem);

         assertThat(result).isNotNull();
         assertThat(result.getId()).isEqualTo("item-1");
         assertThat(result.getSlug()).isEqualTo("java-17");
         assertThat(result.getPath()).isEqualTo("/path/to/item");
         assertThat(result.getTitle()).isEqualTo("Java 17");
         assertThat(result.getShortDescription()).isEqualTo("Short desc");
         assertThat(result.getDescriptionFileId()).isEqualTo("file-id-1");
         assertThat(result.getImageFileId()).isEqualTo("image-id-1");
         assertThat(result.getDescription()).isEqualTo("Full description");
         assertThat(result.getItemSrc()).isEqualTo("http://example.com/item");
         assertThat(result.getAuthors()).containsExactly("author1");
         assertThat(result.getDate()).isNotNull();
         assertThat(result.getTags()).hasSize(1);
         assertThat(result.getTags().getFirst().getLabel()).isEqualTo("version");
         assertThat(result.getTags().getFirst().getOptions()).contains("1.0", "2.0");
     }

     @Test
     void to_api_model_for_catalog_item_maps_with_null_fields() {
         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem sourceItem = new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem();
         sourceItem.setId("item-1");
         sourceItem.setTitle("Title");

         CatalogItem result = mapper.toApiModel(sourceItem);

         assertThat(result).isNotNull();
         assertThat(result.getId()).isEqualTo("item-1");
         assertThat(result.getTitle()).isEqualTo("Title");
         assertThat(result.getSlug()).isNull();
         assertThat(result.getPath()).isNull();
         assertThat(result.getDescription()).isNull();
         assertThat(result.getTags()).isEmpty();
         assertThat(result.getAuthors()).isEmpty();
     }

     @Test
     void to_api_model_for_catalog_item_tag_returns_null_when_tag_is_null() {
         assertThatThrownBy(() -> mapper.toApiModel((org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag) null))
                 .isInstanceOf(NullPointerException.class);
     }

     @Test
     void to_api_model_for_catalog_item_tag_maps_all_fields_when_populated() {
         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag sourceTag =
             new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag();
         sourceTag.setLabel("version");
         sourceTag.setOptions(Set.of("1.0", "2.0", "3.0"));

         CatalogItemTag result = mapper.toApiModel(sourceTag);

         assertThat(result).isNotNull();
         assertThat(result.getLabel()).isEqualTo("version");
         assertThat(result.getOptions()).hasSize(3).contains("1.0", "2.0", "3.0");
     }

     @Test
     void to_api_model_for_catalog_item_tag_maps_with_null_options() {
         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag sourceTag =
             new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag();
         sourceTag.setLabel("environment");
         sourceTag.setOptions(null);

         CatalogItemTag result = mapper.toApiModel(sourceTag);

         assertThat(result).isNotNull();
         assertThat(result.getLabel()).isEqualTo("environment");
         assertThat(result.getOptions()).isEmpty();
     }

     @Test
     void to_api_model_for_catalog_item_maps_multiple_tags() {
         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem sourceItem = new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem();
         sourceItem.setId("item-1");

         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag tag1 =
             new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag();
         tag1.setLabel("version");
         tag1.setOptions(Set.of("1.0", "2.0"));

         org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag tag2 =
             new org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemTag();
         tag2.setLabel("environment");
         tag2.setOptions(Set.of("prod", "dev"));

         sourceItem.setTags(List.of(tag1, tag2));

         CatalogItem result = mapper.toApiModel(sourceItem);

         assertThat(result).isNotNull();
         assertThat(result.getTags()).hasSize(2);
         assertThat(result.getTags().get(0).getLabel()).isEqualTo("version");
         assertThat(result.getTags().get(0).getOptions()).contains("1.0", "2.0");
         assertThat(result.getTags().get(1).getLabel()).isEqualTo("environment");
         assertThat(result.getTags().get(1).getOptions()).contains("prod", "dev");
     }

     @Test
     void to_api_model_replaces_full_next_and_previous_url_with_current_api_url_keeping_only_query_params() {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setScheme("https");
         request.setServerName("api.current-instance.company.tld");
         request.setServerPort(8443);
         request.setRequestURI("/api/v1/projects/metrics/marketplace/project-components");
         RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

         Pagination pagination = new Pagination();
         pagination.setNext(URI.create("http://marketplace-internal.svc.cluster.local/v1/marketplace/project-components?page=2&size=20"));
         pagination.setPrevious(URI.create("http://marketplace-internal.svc.cluster.local/v1/other/path?page=0&size=20"));

         ProjectComponentListResponse response = new ProjectComponentListResponse();
         response.setData(Collections.emptyList());
         response.setPagination(pagination);

         MarketplaceProjectComponentsMetrics result = mapper.toApiModel(response);

         assertThat(result).isNotNull();
         assertThat(result.getPagination()).isNotNull();

         URI next = result.getPagination().getNext().get();
         assertThat(next.getScheme()).isEqualTo("https");
         assertThat(next.getHost()).isEqualTo("api.current-instance.company.tld");
         assertThat(next.getPort()).isEqualTo(8443);
         assertThat(next.getPath()).isEqualTo("/api/v1/projects/metrics/marketplace/project-components");
         assertThat(next.getQuery()).isEqualTo("page=2&size=20");

         URI previous = result.getPagination().getPrevious().get();
         assertThat(previous.getScheme()).isEqualTo("https");
         assertThat(previous.getHost()).isEqualTo("api.current-instance.company.tld");
         assertThat(previous.getPort()).isEqualTo(8443);
         assertThat(previous.getPath()).isEqualTo("/api/v1/projects/metrics/marketplace/project-components");
         assertThat(previous.getQuery()).isEqualTo("page=0&size=20");
     }
}
