package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.springframework.stereotype.Component;

/**
 * Command for validating connection to the Automation Platform.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateConnectionCommand implements ExternalServiceCommand<ValidateConnectionRequest, Boolean> {
    
    private final AutomationPlatformService automationPlatformService;
    
    @Override
    public Boolean execute(ValidateConnectionRequest request) throws ExternalServiceException {
        try {
            log.debug("Validating connection to Automation Platform");
            
            boolean isValid = automationPlatformService.validateConnection();
            
            log.info("Connection validation result: {}", isValid ? "success" : "failed");
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Failed to validate connection to Automation Platform", e);
            throw new ExternalServiceException(
                "Failed to validate connection: " + e.getMessage(),
                e,
                "CONNECTION_VALIDATION_FAILED",
                "aap",
                "validateConnection"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "validate-connection";
    }
    
    @Override
    public String getServiceName() {
        return "aap";
    }
}
