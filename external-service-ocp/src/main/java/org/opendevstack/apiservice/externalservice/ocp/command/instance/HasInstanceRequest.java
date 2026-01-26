package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for checking if an OpenShift instance is configured.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HasInstanceRequest {
    
    /**
     * The name of the OpenShift instance to check.
     */
    private String instanceName;
}
