package org.opendevstack.apiservice.externalservice.commons.command;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for command execution results.
 * Provides metadata about the execution along with the actual response.
 * 
 * @param <T> the type of the response data
 */
@Data
@Builder
public class CommandResult<T> {
    
    /**
     * The actual response data
     */
    private T data;
    
    /**
     * Whether the command execution was successful
     */
    private boolean success;
    
    /**
     * Error message if execution failed
     */
    private String errorMessage;
    
    /**
     * Error code if execution failed
     */
    private String errorCode;
    
    /**
     * Service that executed the command
     */
    private String serviceName;
    
    /**
     * Command that was executed
     */
    private String commandName;
    
    /**
     * Timestamp when the command started
     */
    private Instant startTime;
    
    /**
     * Timestamp when the command completed
     */
    private Instant endTime;
    
    /**
     * Execution duration in milliseconds
     */
    private Long executionTimeMs;
    
    /**
     * Additional metadata about the execution
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Create a successful result
     */
    public static <T> CommandResult<T> success(T data, String serviceName, String commandName) {
        Instant now = Instant.now();
        return CommandResult.<T>builder()
                .data(data)
                .success(true)
                .serviceName(serviceName)
                .commandName(commandName)
                .startTime(now)
                .endTime(now)
                .executionTimeMs(0L)
                .build();
    }
    
    /**
     * Create a failed result
     */
    public static <T> CommandResult<T> failure(String errorMessage, String errorCode, 
                                               String serviceName, String commandName) {
        Instant now = Instant.now();
        return CommandResult.<T>builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .serviceName(serviceName)
                .commandName(commandName)
                .startTime(now)
                .endTime(now)
                .executionTimeMs(0L)
                .build();
    }
}
