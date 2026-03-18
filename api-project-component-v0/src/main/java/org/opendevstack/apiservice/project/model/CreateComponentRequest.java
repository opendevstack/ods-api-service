package org.opendevstack.apiservice.project.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateComponentRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.15.0")
public class CreateComponentRequest {

  private @Nullable String name;

  private @Nullable String productId;

  @Valid
  private Map<String, String> params = new HashMap<>();

  public CreateComponentRequest name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * component name
   * @return name
   */
  
  @Schema(name = "name", description = "component name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  public void setName(@Nullable String name) {
    this.name = name;
  }

  public CreateComponentRequest productId(@Nullable String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * product id
   * @return productId
   */
  
  @Schema(name = "productId", description = "product id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("productId")
  public @Nullable String getProductId() {
    return productId;
  }

  public void setProductId(@Nullable String productId) {
    this.productId = productId;
  }

  public CreateComponentRequest params(Map<String, String> params) {
    this.params = params;
    return this;
  }

  public CreateComponentRequest putParamsItem(String key, String paramsItem) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.put(key, paramsItem);
    return this;
  }

  /**
   * Get params
   * @return params
   */
  
  @Schema(name = "params", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("params")
  public Map<String, String> getParams() {
    return params;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateComponentRequest createComponentRequest = (CreateComponentRequest) o;
    return Objects.equals(this.name, createComponentRequest.name) &&
        Objects.equals(this.productId, createComponentRequest.productId) &&
        Objects.equals(this.params, createComponentRequest.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, productId, params);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateComponentRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
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

