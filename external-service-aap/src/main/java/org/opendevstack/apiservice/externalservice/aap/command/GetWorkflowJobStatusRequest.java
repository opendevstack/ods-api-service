package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for getting workflow job status from the Automation Platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetWorkflowJobStatusRequest {
    
    /**
     * The ID of the workflow job to check.
     */
    private String workflowId;
}
