package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for getting job status from the Automation Platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetJobStatusRequest {
    
    /**
     * The ID of the job to check.
     */
    private String jobId;
}
