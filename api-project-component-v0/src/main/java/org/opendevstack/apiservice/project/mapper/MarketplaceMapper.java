package org.opendevstack.apiservice.project.mapper;


import org.mapstruct.Mapper;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProvisionActionParameter;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MarketplaceMapper {

    default Component mapMarketplaceComponentToV0Component(ProjectComponentInfo source) {
        if (source == null) {
            return null;
        }
        Component target = new Component();
        target.setId(source.getComponentId());
        target.setStatus(source.getStatus());
        return target;
    }

    default List<ProvisionActionParameter> mapCreateComponentRequestToCreateComponentParameterList(CreateComponentRequest createComponentRequest) {
        return createComponentRequest.getParams().entrySet().stream()
                .map(entry -> new ProvisionActionParameter().name(entry.getKey()).type("string").value(entry.getValue()))
                .toList();
    }
}
