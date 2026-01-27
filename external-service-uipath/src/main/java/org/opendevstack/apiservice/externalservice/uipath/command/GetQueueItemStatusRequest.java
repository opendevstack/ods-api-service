package org.opendevstack.apiservice.externalservice.uipath.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for getting queue item status from UiPath Orchestrator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetQueueItemStatusRequest {
    
    /**
     * The ID of the queue item to check.
     */
    private Long queueItemId;
}
