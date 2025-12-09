package org.opendevstack.apiservice.externalservice.commons.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.commons.command.CommandContext;
import org.opendevstack.apiservice.externalservice.commons.command.CommandResult;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.registry.ExternalServiceRegistry;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Facade for executing external service commands.
 * Provides a simplified API for command execution with the registry and executor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalServiceFacade {
    
    private final ExternalServiceRegistry registry;
    private final ExternalServiceExecutor executor;
    
    /**
     * Execute a command by service and command name.
     * 
     * @param serviceName the service name (e.g., "aap", "uipath")
     * @param commandName the command name (e.g., "executeWorkflow")
     * @param request the request object
     * @param <Q> the request type
     * @param <R> the response type
     * @return the command result
     * @throws ExternalServiceException if command is not found or execution fails
     */
    @SuppressWarnings("unchecked")
    public <Q, R> CommandResult<R> executeCommand(String serviceName, String commandName, Q request) 
            throws ExternalServiceException {
        
        ExternalServiceCommand<Q, R> command = registry.getCommand(serviceName, commandName);
        if (command == null) {
            throw new ExternalServiceException(
                "Command not found: " + serviceName + "." + commandName,
                "COMMAND_NOT_FOUND",
                serviceName,
                commandName
            );
        }
        
        CommandContext context = CommandContext.builder()
            .instanceName("default")
            .build();
        
        return executor.execute(command, request, context);
    }
    
    /**
     * Execute a command with a custom context.
     * 
     * @param serviceName the service name
     * @param commandName the command name
     * @param request the request object
     * @param context the execution context
     * @param <Q> the request type
     * @param <R> the response type
     * @return the command result
     * @throws ExternalServiceException if command is not found or execution fails
     */
    @SuppressWarnings("unchecked")
    public <Q, R> CommandResult<R> executeCommand(
            String serviceName, 
            String commandName, 
            Q request,
            CommandContext context) throws ExternalServiceException {
        
        ExternalServiceCommand<Q, R> command = registry.getCommand(serviceName, commandName);
        if (command == null) {
            throw new ExternalServiceException(
                "Command not found: " + serviceName + "." + commandName,
                "COMMAND_NOT_FOUND",
                serviceName,
                commandName
            );
        }
        
        return executor.execute(command, request, context);
    }
    
    /**
     * Get a service by name.
     * 
     * @param serviceName the service name
     * @return optional containing the service if found
     */
    public Optional<ExternalService> getService(String serviceName) {
        return Optional.ofNullable(registry.getService(serviceName));
    }
    
    /**
     * Check if a service is healthy.
     * 
     * @param serviceName the service name
     * @return true if the service exists and is healthy
     */
    public boolean isServiceHealthy(String serviceName) {
        ExternalService service = registry.getService(serviceName);
        return service != null && service.isHealthy();
    }
    
    /**
     * Check if a command is registered.
     * 
     * @param serviceName the service name
     * @param commandName the command name
     * @return true if the command is registered
     */
    public boolean isCommandRegistered(String serviceName, String commandName) {
        return registry.getCommand(serviceName, commandName) != null;
    }
}
