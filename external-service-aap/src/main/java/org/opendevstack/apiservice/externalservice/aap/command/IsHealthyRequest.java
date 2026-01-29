package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for checking health of the Automation Platform.
 * This is an empty request as no parameters are needed for health check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsHealthyRequest {
    
    /**
     * Optional flag to include detailed health information.
     * If true, additional diagnostic information may be collected.
     */
    private boolean includeDetails;
}
