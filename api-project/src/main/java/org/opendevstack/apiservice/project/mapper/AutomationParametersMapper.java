package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.opendevstack.apiservice.project.facade.impl.ProjectCreationCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.opendevstack.apiservice.project.util.ComponentUtil.calculateComponentsToProvision;

@Mapper(componentModel = "spring")
public interface AutomationParametersMapper {

    default Map<String, Object> toWorkflowParameters(ProjectCreationCommand command, String projectId,
                                                     Set<String> allowedComponentValues,
                                                     Set<String> mandatoryComponentValues) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("geographic_region", command.getLocation());
        parameters.put("project_flavor", command.getProjectFlavor());
        parameters.put("project_owner", command.getOwner());
        parameters.put("project_id", projectId);
        parameters.put("configuration_item", command.getConfigurationItem());
        parameters.put("project_key", command.getProjectKey());
        parameters.put("special_account", command.getX2OdsAccount());
        parameters.put("description", command.getProjectDescription());
        parameters.put("project_name", command.getProjectName());
        parameters.put("client_id", command.getClientId().toString());
        if (command.getProjectManagers() != null && !command.getProjectManagers().isEmpty()) {
            parameters.put("project_managers", command.getProjectManagers());
        }
        Set<String> componentsToProvision = calculateComponentsToProvision(command, allowedComponentValues,
                mandatoryComponentValues);
        if (componentsToProvision != null && !componentsToProvision.isEmpty()) {
            parameters.put("components_to_provision", componentsToProvision);
        }
        return parameters;
    }
}