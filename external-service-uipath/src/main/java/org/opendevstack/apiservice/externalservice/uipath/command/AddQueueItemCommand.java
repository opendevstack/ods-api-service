package org.opendevstack.apiservice.externalservice.uipath.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.service.UiPathOrchestratorService;
import org.springframework.stereotype.Component;

/**
 * Command for adding a queue item to UiPath Orchestrator.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddQueueItemCommand implements ExternalServiceCommand<AddQueueItemRequest, UiPathQueueItem> {
    
    private final UiPathOrchestratorService uiPathOrchestratorService;
    
    @Override
    public UiPathQueueItem execute(AddQueueItemRequest request) throws ExternalServiceException {
        try {
            validateRequest(request);
            
            log.info("Adding queue item to UiPath");
            
            if (request.isAsync()) {
                log.warn("Async execution requested but not fully implemented, executing synchronously");
            }
            
            return uiPathOrchestratorService.addQueueItem(request.getQueueItemRequest());
            
        } catch (Exception e) {
            log.error("Failed to add queue item to UiPath", e);
            throw new ExternalServiceException(
                "Failed to add queue item: " + e.getMessage(),
                e,
                "QUEUE_ITEM_CREATION_FAILED",
                "uipath",
                "addQueueItem"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "add-queue-item";
    }
    
    @Override
    public String getServiceName() {
        return "uipath";
    }
    
    
    public void validateRequest(AddQueueItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getQueueItemRequest() == null) {
            throw new IllegalArgumentException("Queue item request cannot be null");
        }
    }
}
