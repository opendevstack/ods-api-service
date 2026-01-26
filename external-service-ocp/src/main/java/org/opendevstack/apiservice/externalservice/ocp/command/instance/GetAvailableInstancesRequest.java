package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for retrieving all available OpenShift instance names.
 * This request has no parameters as it returns all configured instances.
 */
@Data
@Builder
@NoArgsConstructor
public class GetAvailableInstancesRequest {
    
    // No parameters needed - returns all available instances
}
