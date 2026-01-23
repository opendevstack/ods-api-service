package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for executing a workflow on the Automation Platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteWorkflowRequest {
    
    /**
     * The name of the workflow to execute.
     */
    private String workflowName;
    
    /**
     * Parameters to pass to the workflow.
     */
    private Map<String, Object> parameters;
    
    /**
     * Whether to execute asynchronously.
     */
    @Builder.Default
    private boolean async = false;
}
