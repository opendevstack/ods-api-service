package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MarketplaceMapper {

    @Mapping(target = "id", source = "componentId")
    @Mapping(target = "resultTraceback", ignore = true)
    Component mapMarketplaceComponentToV0Component(ProjectComponentInfo source);

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
