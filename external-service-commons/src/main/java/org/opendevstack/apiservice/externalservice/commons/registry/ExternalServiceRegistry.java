package org.opendevstack.apiservice.externalservice.commons.registry;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for external services and their commands.
 * Provides discovery and lookup capabilities for services and commands.
 */
@Slf4j
@Component
public class ExternalServiceRegistry {
    
    private final Map<String, ExternalService> services = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ExternalServiceCommand<?, ?>>> commandsByService = new ConcurrentHashMap<>();
    
    /**
     * Register an external service.
     * 
     * @param service the service to register
     */
    public void registerService(ExternalService service) {
        String serviceName = service.getServiceName();
        services.put(serviceName, service);
        commandsByService.putIfAbsent(serviceName, new ConcurrentHashMap<>());
        log.info("Registered external service: {}", serviceName);
    }
    
    /**
     * Register a command for a service.
     * 
     * @param command the command to register
     */
    public void registerCommand(ExternalServiceCommand<?, ?> command) {
        String serviceName = command.getServiceName();
        String commandName = command.getCommandName();
        
        commandsByService
                .computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                .put(commandName, command);
        
        log.info("Registered command: {} for service: {}", commandName, serviceName);
    }
    
    /**
     * Get a service by name.
     * 
     * @param serviceName the service name
     * @return the service, or null if not found
     */
    public ExternalService getService(String serviceName) {
        return services.get(serviceName);
    }
    
    /**
     * Get a command by service and command name.
     * 
     * @param serviceName the service name
     * @param commandName the command name
     * @return the command, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <Q, R> ExternalServiceCommand<Q, R> getCommand(
            String serviceName, String commandName) {
        Map<String, ExternalServiceCommand<?, ?>> commands = commandsByService.get(serviceName);
        if (commands == null) {
            return null;
        }
        return (ExternalServiceCommand<Q, R>) commands.get(commandName);
    }
    
    /**
     * Get all registered service names.
     * 
     * @return set of service names
     */
    public Set<String> getServiceNames() {
        return Collections.unmodifiableSet(services.keySet());
    }
    
    /**
     * Get all command names for a service.
     * 
     * @param serviceName the service name
     * @return set of command names
     */
    public Set<String> getCommandNames(String serviceName) {
        Map<String, ExternalServiceCommand<?, ?>> commands = commandsByService.get(serviceName);
        if (commands == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(commands.keySet());
    }
    
    /**
     * Get all services.
     * 
     * @return collection of all services
     */
    public Collection<ExternalService> getAllServices() {
        return Collections.unmodifiableCollection(services.values());
    }
    
    /**
     * Check if a service is registered.
     * 
     * @param serviceName the service name
     * @return true if the service is registered
     */
    public boolean hasService(String serviceName) {
        return services.containsKey(serviceName);
    }
    
    /**
     * Check if a command is registered for a service.
     * 
     * @param serviceName the service name
     * @param commandName the command name
     * @return true if the command is registered
     */
    public boolean hasCommand(String serviceName, String commandName) {
        Map<String, ExternalServiceCommand<?, ?>> commands = commandsByService.get(serviceName);
        return commands != null && commands.containsKey(commandName);
    }
}
