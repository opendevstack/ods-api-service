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
        validateOwner(request.getOwner());
        validateX2OdsAccount(request.getX2OdsAccount());        
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


    private void validateX2OdsAccount(String x2OdsAccount) {
        if (!isValidX2OdsAccount(x2OdsAccount)) {
            throw new ProjectValidationException(ErrorKey.PROJECT_X2ACCOUNT_INVALID_FORMAT);
        }
    }

    private void validateOwner(String owner) {
        if (!isValidOwner(owner)) {
            throw new ProjectValidationException(ErrorKey.PROJECT_OWNER_INVALID_FORMAT);
        }
    }

    private boolean isValidOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            return true;
        }
        return owner.matches("^[a-z]{1,10}$");
    }

    private boolean isValidX2OdsAccount(String x2OdsAccount) {
        if (x2OdsAccount == null || x2OdsAccount.isBlank()) {
            return true;
        }
        return x2OdsAccount.matches("^x2[a-zA-Z0-9]{0,13}$");
    }    
}
