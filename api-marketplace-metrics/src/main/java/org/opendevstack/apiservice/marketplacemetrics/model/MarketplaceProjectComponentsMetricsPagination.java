package org.opendevstack.apiservice.marketplacemetrics.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.net.URI;
import java.util.Arrays;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.lang.Nullable;
import java.util.NoSuchElementException;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MarketplaceProjectComponentsMetricsPagination
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.15.0")
public class MarketplaceProjectComponentsMetricsPagination {

  private @Nullable Integer page;

  private @Nullable Integer size;

  private @Nullable Integer totalElements;

  private @Nullable Integer totalPages;

  private JsonNullable<URI> next = JsonNullable.<URI>undefined();

  private JsonNullable<URI> previous = JsonNullable.<URI>undefined();

  public MarketplaceProjectComponentsMetricsPagination page(@Nullable Integer page) {
    this.page = page;
    return this;
  }

  /**
   * Current page of the response.
   * @return page
   */
  
  @Schema(name = "page", example = "0", description = "Current page of the response.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("page")
  public @Nullable Integer getPage() {
    return page;
  }

  public void setPage(@Nullable Integer page) {
    this.page = page;
  }

  public MarketplaceProjectComponentsMetricsPagination size(@Nullable Integer size) {
    this.size = size;
    return this;
  }

  /**
   * Current page size of the response.
   * @return size
   */
  
  @Schema(name = "size", example = "20", description = "Current page size of the response.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("size")
  public @Nullable Integer getSize() {
    return size;
  }

  public void setSize(@Nullable Integer size) {
    this.size = size;
  }

  public MarketplaceProjectComponentsMetricsPagination totalElements(@Nullable Integer totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  /**
   * Total number of elements.
   * @return totalElements
   */
  
  @Schema(name = "totalElements", example = "117", description = "Total number of elements.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalElements")
  public @Nullable Integer getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(@Nullable Integer totalElements) {
    this.totalElements = totalElements;
  }

  public MarketplaceProjectComponentsMetricsPagination totalPages(@Nullable Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  /**
   * Total number of pages of this exact size.
   * @return totalPages
   */
  
  @Schema(name = "totalPages", example = "6", description = "Total number of pages of this exact size.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalPages")
  public @Nullable Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(@Nullable Integer totalPages) {
    this.totalPages = totalPages;
  }

  public MarketplaceProjectComponentsMetricsPagination next(URI next) {
    this.next = JsonNullable.of(next);
    return this;
  }

  /**
   * URL of the next page (or null if the current is the last one)
   * @return next
   */
  @Valid 
  @Schema(name = "next", example = "https://api.example.com/resources?page=1&size=20", description = "URL of the next page (or null if the current is the last one)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("next")
  public JsonNullable<URI> getNext() {
    return next;
  }

  public void setNext(JsonNullable<URI> next) {
    this.next = next;
  }

  public MarketplaceProjectComponentsMetricsPagination previous(URI previous) {
    this.previous = JsonNullable.of(previous);
    return this;
  }

  /**
   * URL of the previous page (or null if the current is the first one)
   * @return previous
   */
  @Valid 
  @Schema(name = "previous", example = "https://api.example.com/resources?page=0&size=20", description = "URL of the previous page (or null if the current is the first one)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("previous")
  public JsonNullable<URI> getPrevious() {
    return previous;
  }

  public void setPrevious(JsonNullable<URI> previous) {
    this.previous = previous;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketplaceProjectComponentsMetricsPagination marketplaceProjectComponentsMetricsPagination = (MarketplaceProjectComponentsMetricsPagination) o;
    return Objects.equals(this.page, marketplaceProjectComponentsMetricsPagination.page) &&
        Objects.equals(this.size, marketplaceProjectComponentsMetricsPagination.size) &&
        Objects.equals(this.totalElements, marketplaceProjectComponentsMetricsPagination.totalElements) &&
        Objects.equals(this.totalPages, marketplaceProjectComponentsMetricsPagination.totalPages) &&
        equalsNullable(this.next, marketplaceProjectComponentsMetricsPagination.next) &&
        equalsNullable(this.previous, marketplaceProjectComponentsMetricsPagination.previous);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(page, size, totalElements, totalPages, hashCodeNullable(next), hashCodeNullable(previous));
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarketplaceProjectComponentsMetricsPagination {\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

