package org.opendevstack.apiservice.project.util;

import org.opendevstack.apiservice.project.facade.impl.ProjectCreationCommand;

import java.util.HashSet;
import java.util.Set;

public class ComponentUtil {

    public static Set<String> calculateComponentsToProvision(ProjectCreationCommand command,
                                                              Set<String> allowedComponentValues,
                                                              Set<String> mandatoryComponentValues) {
        Set<String> componentsToProvision = null;
        if (command.getIncludedComponents() != null && !command.getIncludedComponents().isEmpty()) {
            componentsToProvision = new HashSet<>(command.getIncludedComponents());
        }
        if (command.getExcludedComponents() != null && !command.getExcludedComponents().isEmpty()) {
            if (componentsToProvision == null) {
                componentsToProvision = new HashSet<>(allowedComponentValues);
            }
            componentsToProvision.removeAll(command.getExcludedComponents());
        }
        if (componentsToProvision != null && mandatoryComponentValues != null && !mandatoryComponentValues.isEmpty()) {
            componentsToProvision.addAll(mandatoryComponentValues);
        }
        return componentsToProvision;
    }

}
