package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for checking if an OpenShift instance is healthy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsHealthyRequest {
    
    /**
     * The name of the OpenShift instance to check health for.
     */
    private String instanceName;
}
