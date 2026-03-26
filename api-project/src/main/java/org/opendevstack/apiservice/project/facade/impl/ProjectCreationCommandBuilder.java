package org.opendevstack.apiservice.project.facade.impl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.ClientAppProjectFlavorEntity;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCreationCommandBuilder {

    private final ProjectService projectService;
    
    private final GenerateProjectKeyService generateProjectKeyService;

    public ProjectCreationCommand build(CreateProjectRequest request, ClientAppEntity clientApp, UUID clientId) {
        ClientAppProjectFlavorEntity flavor = resolveFlavor(request, clientApp);

        String projectFlavor = firstNonBlank(request.getProjectFlavor(), flavor.getName());
        String configurationItem = firstNonBlank(request.getConfigurationItem(), flavor.getConfigItem());
        String owner = firstNonBlank(request.getOwner(), flavor.getProjectOwner());
        String location = firstNonBlank(request.getLocation(), flavor.getLocation());
        String projectKey = resolveProjectKey(request.getProjectKey(), flavor);

        return new ProjectCreationCommand(
                projectKey,
                request.getProjectName(),
                request.getProjectDescription(),
                projectFlavor,
                configurationItem,
                location,
                request.getX2OdsAccount(),
                owner,
                clientId);
    }

    private ClientAppProjectFlavorEntity resolveFlavor(CreateProjectRequest request, ClientAppEntity clientApp) {
        List<ClientAppProjectFlavorEntity> flavors = clientApp.getProjectFlavors();
        
        if (flavors == null || flavors.isEmpty()) {
            log.warn("ClientApp '{}' has no project flavors configured", clientApp.getClientId());
            throw new ProjectValidationException(ErrorKey.INVALID_PROJECT_FLAVOR);
        }

        if (Strings.isNotEmpty(request.getProjectFlavor())) {
            return resolveByFlavorName(request.getProjectFlavor(), flavors, clientApp.getClientId());
        }

        if (Strings.isNotEmpty(request.getConfigurationItem())) {
            return resolveByConfigurationItem(request.getConfigurationItem(), flavors, clientApp.getClientId());
        }

        throw new ProjectValidationException(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM);
    }

    private ClientAppProjectFlavorEntity resolveByFlavorName(
            String flavorName, List<ClientAppProjectFlavorEntity> flavors, String clientId) {
        return flavors.stream()
                .filter(f -> flavorName.equals(f.getName()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Flavor '{}' is not configured for clientApp '{}'", flavorName, clientId);
                    return new ProjectValidationException(ErrorKey.INVALID_PROJECT_FLAVOR);
                });
    }

    private ClientAppProjectFlavorEntity resolveByConfigurationItem(
            String configurationItem, List<ClientAppProjectFlavorEntity> flavors, String clientId) {
        List<ClientAppProjectFlavorEntity> matchingFlavors = flavors.stream()
                .filter(f -> configurationItem.equals(f.getConfigItem()))
                .toList();

        if (matchingFlavors.size() != 1) {
            log.warn("ConfigItem '{}' does not match exactly one flavor for clientApp '{}'",
                    configurationItem, clientId);
            throw new ProjectValidationException(ErrorKey.INVALID_CONFIG_ITEM);
        }

        ClientAppProjectFlavorEntity matchedFlavor = matchingFlavors.getFirst();
        
        if (!isAllowedConfigItem(configurationItem, matchedFlavor)) {
            log.warn("ConfigItem '{}' is not in the allowed list for flavor '{}' of clientApp '{}'",
                    configurationItem, matchedFlavor.getName(), clientId);
            throw new ProjectValidationException(ErrorKey.INVALID_CONFIG_ITEM);
        }

        return matchedFlavor;
    }

    private String resolveProjectKey(String existingProjectKey, ClientAppProjectFlavorEntity flavor) {
        if (Strings.isNotEmpty(existingProjectKey)) {
            validateProjectNotExists(existingProjectKey);
            return existingProjectKey;
        }

        String pattern = flavor.getProjectKeyPattern();
        
        try {
            String generatedProjectKey = generateProjectKeyService.generateProjectKey(pattern);
            validateProjectNotExists(generatedProjectKey);
            return generatedProjectKey;
        } catch (org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException e) {
            throw new ProjectKeyGenerationException("Failed to generate unique project key", e);
        }
    }

    private void validateProjectNotExists(String projectKey) {
        if (projectService.getProject(projectKey) != null) {
            throw new ProjectValidationException(ErrorKey.DUPLICATE_RECORD);
        }
    }

    private boolean isAllowedConfigItem(String configurationItem, ClientAppProjectFlavorEntity flavor) {
        String[] allowedConfigItems = flavor.getAllowedConfigItems();
        
        if (allowedConfigItems == null || allowedConfigItems.length == 0) {
            return true;
        }
        
        return Arrays.asList(allowedConfigItems).contains(configurationItem);
    }

    private String firstNonBlank(String preferred, String fallback) {
        return Strings.isNotEmpty(preferred) ? preferred : fallback;
    }
}

