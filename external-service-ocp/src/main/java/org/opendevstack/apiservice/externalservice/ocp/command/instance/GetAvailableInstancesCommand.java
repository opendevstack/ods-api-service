package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Command to retrieve all available OpenShift instance names.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetAvailableInstancesCommand implements ExternalServiceCommand<GetAvailableInstancesRequest, Set<String>> {
    
    private final OpenshiftService openshiftService;
    
    @Override
    public Set<String> execute(GetAvailableInstancesRequest request) throws ExternalServiceException {
        validateRequest(request);
        
        try {
            log.info("Retrieving all available OpenShift instances");
            Set<String> instances = openshiftService.getAvailableInstances();
            log.info("Found {} available OpenShift instances", instances.size());
            return instances;
        } catch (Exception e) {
            log.error("Failed to retrieve available OpenShift instances: {}", e.getMessage());
            throw new ExternalServiceException(
                    "Failed to retrieve available instances: " + e.getMessage(),
                    e,
                    "GET_AVAILABLE_INSTANCES_FAILED",
                    "openshift",
                    "getAvailableInstances"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "get-available-instances";
    }
    
    @Override
    public String getServiceName() {
        return "openshift";
    }
    
    @Override
    public void validateRequest(GetAvailableInstancesRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        // No other validation needed - this command has no required parameters
    }
}
