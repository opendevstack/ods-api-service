package org.opendevstack.apiservice.marketplacemetrics.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentMetrics;
import org.opendevstack.apiservice.marketplacemetrics.model.MarketplaceProjectComponentsMetricsPagination;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MarketplaceProjectComponentsMetrics
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.15.0")
public class MarketplaceProjectComponentsMetrics {

  @Valid
  private List<@Valid MarketplaceProjectComponentMetrics> data = new ArrayList<>();

  private @Nullable MarketplaceProjectComponentsMetricsPagination pagination;

  public MarketplaceProjectComponentsMetrics data(List<@Valid MarketplaceProjectComponentMetrics> data) {
    this.data = data;
    return this;
  }

  public MarketplaceProjectComponentsMetrics addDataItem(MarketplaceProjectComponentMetrics dataItem) {
    if (this.data == null) {
      this.data = new ArrayList<>();
    }
    this.data.add(dataItem);
    return this;
  }

  /**
   * Get data
   * @return data
   */
  @Valid 
  @Schema(name = "data", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("data")
  public List<@Valid MarketplaceProjectComponentMetrics> getData() {
    return data;
  }

  public void setData(List<@Valid MarketplaceProjectComponentMetrics> data) {
    this.data = data;
  }

  public MarketplaceProjectComponentsMetrics pagination(@Nullable MarketplaceProjectComponentsMetricsPagination pagination) {
    this.pagination = pagination;
    return this;
  }

  /**
   * Get pagination
   * @return pagination
   */
  @Valid 
  @Schema(name = "pagination", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("pagination")
  public @Nullable MarketplaceProjectComponentsMetricsPagination getPagination() {
    return pagination;
  }

  public void setPagination(@Nullable MarketplaceProjectComponentsMetricsPagination pagination) {
    this.pagination = pagination;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketplaceProjectComponentsMetrics marketplaceProjectComponentsMetrics = (MarketplaceProjectComponentsMetrics) o;
    return Objects.equals(this.data, marketplaceProjectComponentsMetrics.data) &&
        Objects.equals(this.pagination, marketplaceProjectComponentsMetrics.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarketplaceProjectComponentsMetrics {\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
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

