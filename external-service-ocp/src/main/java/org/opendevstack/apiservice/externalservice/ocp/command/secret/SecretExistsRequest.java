package org.opendevstack.apiservice.externalservice.ocp.command.secret;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for checking if a secret exists in OpenShift.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecretExistsRequest {
    
    /**
     * The name of the OpenShift instance to use.
     */
    private String instanceName;
    
    /**
     * The name of the secret to check.
     */
    private String secretName;
    
    /**
     * The namespace where the secret is located. If not specified, uses the default namespace.
     */
    private String namespace;
}
