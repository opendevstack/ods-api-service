package org.opendevstack.apiservice.externalservice.ocp.command.secret;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for retrieving an entire secret from OpenShift.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSecretRequest {
    
    /**
     * The name of the OpenShift instance to use.
     */
    private String instanceName;
    
    /**
     * The name of the secret to retrieve.
     */
    private String secretName;
    
    /**
     * The namespace where the secret is located. If not specified, uses the default namespace.
     */
    private String namespace;
}
