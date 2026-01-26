package org.opendevstack.apiservice.externalservice.ocp.command.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for validating connection to an OpenShift instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateConnectionRequest {
    
    /**
     * The name of the OpenShift instance to validate connection for.
     */
    private String instanceName;
}
