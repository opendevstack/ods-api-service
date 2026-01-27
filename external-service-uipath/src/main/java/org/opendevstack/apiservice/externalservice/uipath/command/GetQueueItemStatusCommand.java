package org.opendevstack.apiservice.externalservice.uipath.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.service.UiPathOrchestratorService;
import org.springframework.stereotype.Component;

/**
 * Command for getting queue item status from UiPath Orchestrator.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetQueueItemStatusCommand implements ExternalServiceCommand<GetQueueItemStatusRequest, UiPathQueueItem> {
    
    private final UiPathOrchestratorService uiPathOrchestratorService;
    
    @Override
    public UiPathQueueItem execute(GetQueueItemStatusRequest request) throws ExternalServiceException {
        try {
            validateRequest(request);
            
            log.debug("Getting queue item status for ID: {}", request.getQueueItemId());
            
            return uiPathOrchestratorService.getQueueItemById(request.getQueueItemId());
            
        } catch (Exception e) {
            log.error("Failed to get queue item status for ID: {}", request.getQueueItemId(), e);
            throw new ExternalServiceException(
                "Failed to get queue item status: " + e.getMessage(),
                e,
                "QUEUE_ITEM_STATUS_CHECK_FAILED",
                "uipath",
                "getQueueItemStatus"
            );
        }
    }
    
    @Override
    public String getCommandName() {
        return "get-queue-item-status";
    }
    
    @Override
    public String getServiceName() {
        return "uipath";
    }
    
    
    public void validateRequest(GetQueueItemStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getQueueItemId() == null) {
            throw new IllegalArgumentException("Queue item ID cannot be null");
        }
    }
}
