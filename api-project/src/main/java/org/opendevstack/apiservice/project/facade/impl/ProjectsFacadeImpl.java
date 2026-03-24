package org.opendevstack.apiservice.project.facade.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.ClientAppProjectFlavorEntity;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.mapper.ProjectMapper;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.service.ClientAppService;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component("apiProjectFacadeImpl")
@Slf4j
public class ProjectsFacadeImpl implements ProjectsFacade {

    @Value("${apis.projects.ansible-workflow-name}")
    private String createProjectWorkflow;

    private final ProjectService projectService;
    
    private final ProjectMapper projectMapper;
    
    private final ClientAppService clientAppService;
    
    private final GenerateProjectKeyService generateProjectKeyService;
    
    private final AutomationPlatformService automationPlatformService;

    public ProjectsFacadeImpl(
            ProjectService projectService,
            ProjectMapper projectMapper,
            ClientAppService clientAppService,
            GenerateProjectKeyService generateProjectKeyService, 
            AutomationPlatformService automationPlatformService) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
        this.clientAppService = clientAppService;
        this.generateProjectKeyService = generateProjectKeyService;
        this.automationPlatformService = automationPlatformService;
    }

    @Override
    public CreateProjectResponse createProject(CreateProjectRequest request, UUID clientId)
            throws ProjectCreationException, ProjectKeyGenerationException, AutomationPlatformException {

        ClientAppEntity clientApp = clientAppService.findByClientId(clientId);

        ClientAppProjectFlavorEntity resolvedFlavor = resolveFlavor(request, clientApp);

        resolveRequestDefaults(request, resolvedFlavor);

        String resolvedProjectKey = resolveProjectKey(request.getProjectKey(), resolvedFlavor);
        request.setProjectKey(resolvedProjectKey);
        
        ProjectResponse project = projectService.createProject(projectMapper.toServiceRequest(request));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("geographic_region", request.getLocation());
        parameters.put("project_flavor", request.getProjectFlavor());
        parameters.put("project_owner", request.getOwner());
        parameters.put("project_id", project.getProjectId().toString());
        parameters.put("configuration_item", request.getConfigurationItem());
        parameters.put("project_key", request.getProjectKey());
        parameters.put("special_account", request.getX2OdsAccount());
        parameters.put("description", request.getProjectDescription());
        parameters.put("project_name", request.getProjectName());
        parameters.put("client_id", clientId.toString());

        AutomationExecutionResult automationExecutionResult = automationPlatformService
                .executeWorkflow(createProjectWorkflow, parameters);

        if (automationExecutionResult.isSuccessful()) {
            CreateProjectResponse response = new CreateProjectResponse();
            response.setMessage("The project creation process has been successfully initiated.");
            response.setStatus(Status.PENDING.getDbValue());
            response.setProjectFlavor(request.getProjectFlavor());
            response.setProjectKey(project.getProjectKey());
            return response;
        } else {
            throw new AutomationPlatformException("Failed to create project: " 
                    + automationExecutionResult.getErrorDetails());
        } 
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
    
    private void resolveRequestDefaults(CreateProjectRequest request, ClientAppProjectFlavorEntity flavor) {
        if (Strings.isEmpty(request.getProjectFlavor())) {
            request.setProjectFlavor(flavor.getName());
        }
        if (Strings.isEmpty(request.getConfigurationItem())) {
            request.setConfigurationItem(flavor.getConfigItem());
        }
        if (Strings.isEmpty(request.getOwner())) {
            request.setOwner(flavor.getProjectOwner());
        }
        if (Strings.isEmpty(request.getLocation())) {
            request.setLocation(flavor.getLocation());
        }
    }
    
    private String resolveProjectKey(String existingProjectKey, ClientAppProjectFlavorEntity flavor)
            throws ProjectKeyGenerationException {
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

    @Override
    public CreateProjectResponse getProject(String projectKey) {
        return projectMapper.toApiResponse(projectService.getProject(projectKey));
    }

    private boolean isAllowedConfigItem(String configurationItem, ClientAppProjectFlavorEntity flavor) {
        String[] allowedConfigItems = flavor.getAllowedConfigItems();
        if (allowedConfigItems == null || allowedConfigItems.length == 0) {
            return true;
        }
        return Arrays.asList(allowedConfigItems).contains(configurationItem);
    }
}