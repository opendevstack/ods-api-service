package org.opendevstack.apiservice.project.mapper;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentExtendedInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.EnvironmentsDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MarketplaceMapper {

    default Component mapMarketplaceComponentToV0Component(ProjectComponentExtendedInfo source, CatalogItem catalogItem) throws MarketplaceException {
        Component component = new Component();
        component.setId(source.getComponentId());
        component.setEnvironment(EnvironmentsDTO.DEV); // Env is always DEV so we hardcode it as such
        component.setRepositoryURL(source.getComponentUrl());
        component.setComponentType(""); // We agreed to hardcode the type as empty
        component.setStatus(StatusMap.toOldStatus(source.getStatus()));
        if (component.getStatus() == null) {
            throw new MarketplaceException("No status mapping found for status " + source.getStatus());
        }
        component.setProductId(catalogItem.getId());
        component.setProductName(catalogItem.getTitle());
        component.setProductDescription(catalogItem.getShortDescription());

        source.getParameters().forEach(
                param -> component.putParamsItem(param.getName(), param.getValues())
        );
        return component;
    }

    default List<ProvisionActionParameter> mapCreateComponentRequestToCreateComponentParameterList(CreateComponentRequest createComponentRequest) {
        if (createComponentRequest == null) {
            return List.of();
        }

        List<ProvisionActionParameter> parameters = new ArrayList<>();
        parameters.add(createParameter("component_id", createComponentRequest.getName(), "string"));
        parameters.add(createParameter("component_type", createComponentRequest.getProductId(), "string"));

        if (createComponentRequest.getParams() != null && !createComponentRequest.getParams().isEmpty()) {
            parameters.addAll(mapEntriesToCreateComponentParameterList(createComponentRequest.getParams().entrySet().stream().toList()));
        }

        return parameters;
    }

    default ProvisionActionParameter createParameter(String name, String value, String type) {
        return new ProvisionActionParameter().name(name).type(type).value(value);
    }

    @IterableMapping(qualifiedByName = "toCreateComponentParameter")
    List<ProvisionActionParameter> mapEntriesToCreateComponentParameterList(List<Map.Entry<String, Object>> entries);

    @Named("toCreateComponentParameter")
    @Mapping(target = "name", source = "key")
    @Mapping(target = "type", constant = "string")
    @Mapping(target = "value", expression = "java(String.valueOf(entry.getValue()))")
    ProvisionActionParameter toCreateComponentParameter(Map.Entry<String, Object> entry);
}
