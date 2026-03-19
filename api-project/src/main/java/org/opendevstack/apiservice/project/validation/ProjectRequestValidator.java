package org.opendevstack.apiservice.project.validation;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.springframework.stereotype.Component;

@Component
public class ProjectRequestValidator {
    
    private static final String PROJECT_KEY_PATTERN         = "^[A-Z]{2}[A-Z0-9]{1,8}$";
    private static final String PROJECT_NAME_PATTERN        = "^[A-Za-z0-9 ]{0,80}$";
    private static final String PROJECT_DESCRIPTION_PATTERN = "^.{0,255}$";
    
    public void validate(CreateProjectRequest request) {
        validateProjectKey(request.getProjectKey());
        validateProjectName(request.getProjectName());
        validateProjectDescription(request.getProjectDescription());
        validateFlavorOrConfigItem(request);
    }
    
    private void validateProjectKey(String projectKey) {
        if (projectKey != null && !projectKey.matches(PROJECT_KEY_PATTERN)) {
            throw new ProjectValidationException(ErrorKey.PROJECT_KEY_INVALID_FORMAT);
        }
    }
    
    private void validateProjectName(String projectName) {
        if (projectName != null && !projectName.matches(PROJECT_NAME_PATTERN)) {
            throw new ProjectValidationException(ErrorKey.PROJECT_NAME_INVALID_FORMAT);
        }
    }
    
    private void validateProjectDescription(String projectDescription) {
        if (projectDescription != null && !projectDescription.matches(PROJECT_DESCRIPTION_PATTERN)) {
            throw new ProjectValidationException(ErrorKey.PROJECT_DESCRIPTION_INVALID_FORMAT);
        }
    }
    
    private void validateFlavorOrConfigItem(CreateProjectRequest request) {
        String projectFlavor = request.getProjectFlavor();
        String configurationItem = request.getConfigurationItem();
        
        boolean hasFlavor = projectFlavor != null && !projectFlavor.trim().isEmpty();
        boolean hasConfigItem = configurationItem != null && !configurationItem.trim().isEmpty();
        
        if (!hasFlavor && !hasConfigItem) {
            throw new ProjectValidationException(
                ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM
            );
        }
    }
}
