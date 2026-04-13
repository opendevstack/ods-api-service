package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.EnvironmentsStatusDTO;

import org.opendevstack.apiservice.project.model.EnvironmentsTypeDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MarketplaceMapper {

    default Component mapMarketplaceComponentToV0Component(ProjectComponent source) {
        if (source == null) {
            return null;
        }

        Component target = new Component();
        target.setId((source.getComponentId() != null) ? source.getComponentId().toString() : null);
        target.setName(source.getName());
        target.setProductDescription(source.getProductDescription());
        target.setProductName(source.getProductName());
        target.setProductId(source.getProductId());
        target.setEnvironment(toEnvironmentType(source.getEnvironment()));
        target.setStatus(toEnvironmentStatus(source.getStatus()));
        target.setRepositoryURL(source.getRepositoryURL());
        target.setComponentType(source.getComponentType());
        target.setParams(Collections.emptyMap());
        return target;
    }

    default List<CreateComponentParameter> mapCreateComponentRequestToCreateComponentParameterList(CreateComponentRequest createComponentRequest) {
        if (createComponentRequest == null || createComponentRequest.getParams() == null) {
            return List.of();
        }

        return createComponentRequest.getParams().entrySet().stream()
                .map(this::toCreateComponentParameter)
                .toList();
    }

    private CreateComponentParameter toCreateComponentParameter(Map.Entry<String, Object> entry) {
        return new CreateComponentParameter(entry.getKey(), "string", String.valueOf(entry.getValue()));
    }

    private EnvironmentsStatusDTO toEnvironmentStatus(String sourceStatus) {
        if (sourceStatus == null || sourceStatus.isBlank()) {
            return null;
        }
        try {
            return EnvironmentsStatusDTO.fromValue(sourceStatus);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private EnvironmentsTypeDTO toEnvironmentType(String sourceEnv) {
        if (sourceEnv == null || sourceEnv.isBlank()) {
            return null;
        }
        try {
            return EnvironmentsTypeDTO.fromValue(sourceEnv);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
