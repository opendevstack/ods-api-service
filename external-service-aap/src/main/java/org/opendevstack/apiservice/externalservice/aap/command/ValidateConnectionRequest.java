package org.opendevstack.apiservice.externalservice.aap.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for validating connection to the Automation Platform.
 * This is an empty request as no parameters are needed for connection validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateConnectionRequest {
    
    /**
     * Optional timeout in milliseconds for the connection validation.
     * If not provided, the default timeout will be used.
     */
    private Integer timeoutMs;
}
