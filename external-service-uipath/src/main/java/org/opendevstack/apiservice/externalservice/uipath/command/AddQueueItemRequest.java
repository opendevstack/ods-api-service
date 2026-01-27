package org.opendevstack.apiservice.externalservice.uipath.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemRequest;

/**
 * Request DTO for adding a queue item to UiPath Orchestrator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQueueItemRequest {
    
    /**
     * The queue item details.
     */
    private UiPathQueueItemRequest queueItemRequest;
    
    /**
     * Whether to execute asynchronously.
     */
    @Builder.Default
    private boolean async = false;
}
