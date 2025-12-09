package org.opendevstack.apiservice.externalservice.commons.executor;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.commons.command.CommandContext;
import org.opendevstack.apiservice.externalservice.commons.command.CommandResult;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Executor for external service commands.
 * Handles command execution with retry logic, metrics, and error handling.
 */
@Slf4j
@Component
public class ExternalServiceExecutor {
    
    /**
     * Execute a command synchronously.
     * 
     * @param command the command to execute
     * @param request the request data
     * @param context the execution context
     * @return the command result
     */
    public <Q, R> CommandResult<R> execute(
            ExternalServiceCommand<Q, R> command,
            Q request,
            CommandContext context) {
        
        Instant startTime = Instant.now();
        String serviceName = command.getServiceName();
        String commandName = command.getCommandName();
        
        log.debug("Executing command: {} on service: {}", commandName, serviceName);
        
        try {
            // Validate request
            command.validateRequest(request);
            
            // Execute command with retry logic
            R response = executeWithRetry(command, request, context);
            
            Instant endTime = Instant.now();
            long executionTime = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            log.debug("Command {} completed successfully in {}ms", commandName, executionTime);
            
            return CommandResult.<R>builder()
                    .data(response)
                    .success(true)
                    .serviceName(serviceName)
                    .commandName(commandName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .executionTimeMs(executionTime)
                    .metadata(context.getMetadata())
                    .build();
            
        } catch (ExternalServiceException e) {
            Instant endTime = Instant.now();
            long executionTime = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            log.error("Command {} failed after {}ms: {}", commandName, executionTime, e.getMessage(), e);
            
            return CommandResult.<R>builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorCode(e.getErrorCode())
                    .serviceName(serviceName)
                    .commandName(commandName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .executionTimeMs(executionTime)
                    .metadata(context.getMetadata())
                    .build();
                    
        } catch (Exception e) {
            Instant endTime = Instant.now();
            long executionTime = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            log.error("Unexpected error executing command {}: {}", commandName, e.getMessage(), e);
            
            return CommandResult.<R>builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorCode("UNKNOWN_ERROR")
                    .serviceName(serviceName)
                    .commandName(commandName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .executionTimeMs(executionTime)
                    .metadata(context.getMetadata())
                    .build();
        }
    }
    
    /**
     * Execute a command asynchronously.
     * 
     * @param command the command to execute
     * @param request the request data
     * @param context the execution context
     * @return a CompletableFuture with the command result
     */
    public <Q, R> CompletableFuture<CommandResult<R>> executeAsync(
            ExternalServiceCommand<Q, R> command,
            Q request,
            CommandContext context) {
        
        return CompletableFuture.supplyAsync(() -> execute(command, request, context));
    }
    
    /**
     * Execute command with retry logic.
     */
    private <Q, R> R executeWithRetry(
            ExternalServiceCommand<Q, R> command,
            Q request,
            CommandContext context) throws ExternalServiceException {
        
        int maxAttempts = context.getRetryAttempts() + 1;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return command.execute(request);
            } catch (ExternalServiceException e) {
                if (attempt < maxAttempts) {
                    log.warn("Command {} failed (attempt {}/{}), retrying...", 
                            command.getCommandName(), attempt, maxAttempts);
                    try {
                        Thread.sleep(1000L * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                } else {
                    // Last attempt failed, throw the exception
                    throw e;
                }
            }
        }
        
        // This should never be reached since maxAttempts is always >= 1
        throw new ExternalServiceException(
                "Command execution failed unexpectedly",
                "RETRY_EXHAUSTED",
                command.getServiceName(),
                command.getCommandName()
        );
    }
}
