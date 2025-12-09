package org.opendevstack.apiservice.externalservice.commons.command;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Context information for command execution.
 * Contains metadata about the execution environment and parameters.
 */
@Data
@Builder
public class CommandContext {
    
    /**
     * Optional instance name for multi-instance services
     */
    private String instanceName;
    
    /**
     * Timeout in milliseconds for this command execution
     */
    @Builder.Default
    private Long timeoutMs = 30000L;
    
    /**
     * Whether to execute asynchronously
     */
    @Builder.Default
    private Boolean async = false;
    
    /**
     * Number of retry attempts
     */
    @Builder.Default
    private Integer retryAttempts = 0;
    
    /**
     * Additional metadata for the command execution
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Get a metadata value by key
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Set a metadata value
     */
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
}
