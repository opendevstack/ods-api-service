package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for executing a workflow asynchronously on the Automation Platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteWorkflowAsyncRequest {
    
    /**
     * The name of the workflow to execute.
     */
    private String workflowName;
    
    /**
     * The parameters to pass to the workflow.
     */
    private Map<String, Object> parameters;
    
    /**
     * Optional callback URL to notify when workflow completes.
     */
    private String callbackUrl;
}
