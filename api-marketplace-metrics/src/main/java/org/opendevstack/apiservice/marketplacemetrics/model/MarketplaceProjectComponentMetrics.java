package org.opendevstack.apiservice.marketplacemetrics.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * MarketplaceProjectComponentMetrics
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.15.0")
public class MarketplaceProjectComponentMetrics {

  private @Nullable String projectKey;

  private @Nullable String componentId;

  private @Nullable String caller;

  private @Nullable String catalogItemSlug;

  private @Nullable BigDecimal createdAt;

  private @Nullable BigDecimal updatedAt;

  public MarketplaceProjectComponentMetrics projectKey(@Nullable String projectKey) {
    this.projectKey = projectKey;
    return this;
  }

  /**
   * The projectKey which the component is provisioned for.
   * @return projectKey
   */
  
  @Schema(name = "projectKey", example = "SOMEPROJECT", description = "The projectKey which the component is provisioned for.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("projectKey")
  public @Nullable String getProjectKey() {
    return projectKey;
  }

  public void setProjectKey(@Nullable String projectKey) {
    this.projectKey = projectKey;
  }

  public MarketplaceProjectComponentMetrics componentId(@Nullable String componentId) {
    this.componentId = componentId;
    return this;
  }

  /**
   * The componentId set by the user.
   * @return componentId
   */
  @Pattern(regexp = "^(?!\\s*$).+") @Size(min = 1) 
  @Schema(name = "componentId", example = "any-component-id-from-backend", description = "The componentId set by the user.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("componentId")
  public @Nullable String getComponentId() {
    return componentId;
  }

  public void setComponentId(@Nullable String componentId) {
    this.componentId = componentId;
  }

  public MarketplaceProjectComponentMetrics caller(@Nullable String caller) {
    this.caller = caller;
    return this;
  }

  /**
   * The email of who provisioned the component.
   * @return caller
   */
  
  @Schema(name = "caller", example = "some-person@email.com", description = "The email of who provisioned the component.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("caller")
  public @Nullable String getCaller() {
    return caller;
  }

  public void setCaller(@Nullable String caller) {
    this.caller = caller;
  }

  public MarketplaceProjectComponentMetrics catalogItemSlug(@Nullable String catalogItemSlug) {
    this.catalogItemSlug = catalogItemSlug;
    return this;
  }

  /**
   * The provisioned catalog item slug.
   * @return catalogItemSlug
   */
  
  @Schema(name = "catalogItemSlug", example = "some_technology-name", description = "The provisioned catalog item slug.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("catalogItemSlug")
  public @Nullable String getCatalogItemSlug() {
    return catalogItemSlug;
  }

  public void setCatalogItemSlug(@Nullable String catalogItemSlug) {
    this.catalogItemSlug = catalogItemSlug;
  }

  public MarketplaceProjectComponentMetrics createdAt(@Nullable BigDecimal createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * The timestamp of the provision action.
   * @return createdAt
   */
  @Valid 
  @Schema(name = "createdAt", example = "1707043200000", description = "The timestamp of the provision action.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public @Nullable BigDecimal getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(@Nullable BigDecimal createdAt) {
    this.createdAt = createdAt;
  }

  public MarketplaceProjectComponentMetrics updatedAt(@Nullable BigDecimal updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * The timestamp of the last change of the provisioned component.
   * @return updatedAt
   */
  @Valid 
  @Schema(name = "updatedAt", example = "1707043200000", description = "The timestamp of the last change of the provisioned component.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedAt")
  public @Nullable BigDecimal getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(@Nullable BigDecimal updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketplaceProjectComponentMetrics marketplaceProjectComponentMetrics = (MarketplaceProjectComponentMetrics) o;
    return Objects.equals(this.projectKey, marketplaceProjectComponentMetrics.projectKey) &&
        Objects.equals(this.componentId, marketplaceProjectComponentMetrics.componentId) &&
        Objects.equals(this.caller, marketplaceProjectComponentMetrics.caller) &&
        Objects.equals(this.catalogItemSlug, marketplaceProjectComponentMetrics.catalogItemSlug) &&
        Objects.equals(this.createdAt, marketplaceProjectComponentMetrics.createdAt) &&
        Objects.equals(this.updatedAt, marketplaceProjectComponentMetrics.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectKey, componentId, caller, catalogItemSlug, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarketplaceProjectComponentMetrics {\n");
    sb.append("    projectKey: ").append(toIndentedString(projectKey)).append("\n");
    sb.append("    componentId: ").append(toIndentedString(componentId)).append("\n");
    sb.append("    caller: ").append(toIndentedString(caller)).append("\n");
    sb.append("    catalogItemSlug: ").append(toIndentedString(catalogItemSlug)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

