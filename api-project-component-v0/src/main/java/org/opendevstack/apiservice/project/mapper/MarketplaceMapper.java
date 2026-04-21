package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.ComponentsStatusDTO;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.EnvironmentsDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MarketplaceMapper {

    @Mapping(target = "id", source = "componentId", qualifiedByName = "uuidToString")
//    TODO @Mapping(target = "environment", source = "environment", qualifiedByName = "toEnvironment")
    @Mapping(target = "status", source = "status", qualifiedByName = "toComponentStatus")
//   TODO @Mapping(target = "params", expression = "java(java.util.Collections.emptyMap())")
    @Mapping(target = "resultTraceback", ignore = true)
    Component mapMarketplaceComponentToV0Component(ProjectComponentInfo source);

    default List<ProvisionActionParameter> mapCreateComponentRequestToCreateComponentParameterList(CreateComponentRequest createComponentRequest) {
        if (createComponentRequest == null || createComponentRequest.getParams() == null) {
            return List.of();
        }

        return mapEntriesToCreateComponentParameterList(createComponentRequest.getParams().entrySet().stream().toList());
    }

    @IterableMapping(qualifiedByName = "toCreateComponentParameter")
    List<ProvisionActionParameter> mapEntriesToCreateComponentParameterList(List<Map.Entry<String, Object>> entries);

    @Named("toCreateComponentParameter")
    @Mapping(target = "name", source = "key")
    @Mapping(target = "type", constant = "string")
    @Mapping(target = "value", expression = "java(String.valueOf(entry.getValue()))")
    ProvisionActionParameter toCreateComponentParameter(Map.Entry<String, Object> entry);

    @Named("uuidToString")
    default String uuidToString(UUID sourceId) {
        return sourceId != null ? sourceId.toString() : null;
    }

    @Named("toComponentStatus")
    default ComponentsStatusDTO toComponentStatus(String sourceStatus) {
        if (sourceStatus == null || sourceStatus.isBlank()) {
            return null;
        }
        try {
            return ComponentsStatusDTO.fromValue(sourceStatus);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Named("toEnvironment")
    default EnvironmentsDTO toEnvironment(String sourceEnv) {
        if (sourceEnv == null || sourceEnv.isBlank()) {
            return null;
        }
        try {
            return EnvironmentsDTO.fromValue(sourceEnv);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
