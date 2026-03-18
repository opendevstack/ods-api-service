package org.opendevstack.apiservice.project.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Component
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.15.0")
public class Component {

  private @Nullable String id;

  private @Nullable String name;

  private @Nullable String productDescription;

  private @Nullable String productName;

  private @Nullable String productId;

  private @Nullable String environment;

  private @Nullable String status;

  private @Nullable String resultTraceback;

  private @Nullable String repositoryURL;

  private @Nullable Object params;

  private @Nullable String componentType;

  public Component id(@Nullable String id) {
    this.id = id;
    return this;
  }

  /**
   * Component ID
   * @return id
   */
  
  @Schema(name = "id", description = "Component ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  public Component name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the component
   * @return name
   */
  
  @Schema(name = "name", description = "Name of the component", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  public void setName(@Nullable String name) {
    this.name = name;
  }

  public Component productDescription(@Nullable String productDescription) {
    this.productDescription = productDescription;
    return this;
  }

  /**
   * Description of the product
   * @return productDescription
   */
  
  @Schema(name = "productDescription", description = "Description of the product", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("productDescription")
  public @Nullable String getProductDescription() {
    return productDescription;
  }

  public void setProductDescription(@Nullable String productDescription) {
    this.productDescription = productDescription;
  }

  public Component productName(@Nullable String productName) {
    this.productName = productName;
    return this;
  }

  /**
   * Name of the product (e.g. Docker plain)
   * @return productName
   */
  
  @Schema(name = "productName", description = "Name of the product (e.g. Docker plain)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("productName")
  public @Nullable String getProductName() {
    return productName;
  }

  public void setProductName(@Nullable String productName) {
    this.productName = productName;
  }

  public Component productId(@Nullable String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * Product ID
   * @return productId
   */
  
  @Schema(name = "productId", description = "Product ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("productId")
  public @Nullable String getProductId() {
    return productId;
  }

  public void setProductId(@Nullable String productId) {
    this.productId = productId;
  }

  public Component environment(@Nullable String environment) {
    this.environment = environment;
    return this;
  }

  /**
   * Environment (e.g. DEV)
   * @return environment
   */
  
  @Schema(name = "environment", description = "Environment (e.g. DEV)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("environment")
  public @Nullable String getEnvironment() {
    return environment;
  }

  public void setEnvironment(@Nullable String environment) {
    this.environment = environment;
  }

  public Component status(@Nullable String status) {
    this.status = status;
    return this;
  }

  /**
   * Status of the component (e.g. READY, NOT_READY)
   * @return status
   */
  
  @Schema(name = "status", description = "Status of the component (e.g. READY, NOT_READY)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable String getStatus() {
    return status;
  }

  public void setStatus(@Nullable String status) {
    this.status = status;
  }

  public Component resultTraceback(@Nullable String resultTraceback) {
    this.resultTraceback = resultTraceback;
    return this;
  }

  /**
   * Traceback information in case of error
   * @return resultTraceback
   */
  
  @Schema(name = "resultTraceback", description = "Traceback information in case of error", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("resultTraceback")
  public @Nullable String getResultTraceback() {
    return resultTraceback;
  }

  public void setResultTraceback(@Nullable String resultTraceback) {
    this.resultTraceback = resultTraceback;
  }

  public Component repositoryURL(@Nullable String repositoryURL) {
    this.repositoryURL = repositoryURL;
    return this;
  }

  /**
   * URL of the repository (for ODS products)
   * @return repositoryURL
   */
  
  @Schema(name = "repositoryURL", description = "URL of the repository (for ODS products)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("repositoryURL")
  public @Nullable String getRepositoryURL() {
    return repositoryURL;
  }

  public void setRepositoryURL(@Nullable String repositoryURL) {
    this.repositoryURL = repositoryURL;
  }

  public Component params(@Nullable Object params) {
    this.params = params;
    return this;
  }

  /**
   * Additional parameters (key-value pairs)
   * @return params
   */
  
  @Schema(name = "params", description = "Additional parameters (key-value pairs)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("params")
  public @Nullable Object getParams() {
    return params;
  }

  public void setParams(@Nullable Object params) {
    this.params = params;
  }

  public Component componentType(@Nullable String componentType) {
    this.componentType = componentType;
    return this;
  }

  /**
   * Type of component (ods|awx)
   * @return componentType
   */
  
  @Schema(name = "component-type", description = "Type of component (ods|awx)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("component-type")
  public @Nullable String getComponentType() {
    return componentType;
  }

  public void setComponentType(@Nullable String componentType) {
    this.componentType = componentType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Component component = (Component) o;
    return Objects.equals(this.id, component.id) &&
        Objects.equals(this.name, component.name) &&
        Objects.equals(this.productDescription, component.productDescription) &&
        Objects.equals(this.productName, component.productName) &&
        Objects.equals(this.productId, component.productId) &&
        Objects.equals(this.environment, component.environment) &&
        Objects.equals(this.status, component.status) &&
        Objects.equals(this.resultTraceback, component.resultTraceback) &&
        Objects.equals(this.repositoryURL, component.repositoryURL) &&
        Objects.equals(this.params, component.params) &&
        Objects.equals(this.componentType, component.componentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, productDescription, productName, productId, environment, status, resultTraceback, repositoryURL, params, componentType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Component {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    productDescription: ").append(toIndentedString(productDescription)).append("\n");
    sb.append("    productName: ").append(toIndentedString(productName)).append("\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    resultTraceback: ").append(toIndentedString(resultTraceback)).append("\n");
    sb.append("    repositoryURL: ").append(toIndentedString(repositoryURL)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
    sb.append("    componentType: ").append(toIndentedString(componentType)).append("\n");
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

