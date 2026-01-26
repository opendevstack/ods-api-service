package org.opendevstack.apiservice.externalservice.ocp.command.secret;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for retrieving a specific value from an OpenShift secret.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSecretValueRequest {
    
    /**
     * The name of the OpenShift instance to use.
     */
    private String instanceName;
    
    /**
     * The name of the secret to retrieve.
     */
    private String secretName;
    
    /**
     * The key within the secret to retrieve.
     */
    private String key;
    
    /**
     * The namespace where the secret is located. If not specified, uses the default namespace.
     */
    private String namespace;
}
