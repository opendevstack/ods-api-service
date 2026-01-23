package org.opendevstack.apiservice.externalservice.commons.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.registry.ExternalServiceRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Auto-configuration for external services.
 * Automatically discovers and registers all ExternalService and ExternalServiceCommand beans.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExternalServiceAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {
    
    private final ApplicationContext applicationContext;
    
    /**
     * Create the ExternalServiceRegistry bean.
     */
    @Bean
    public ExternalServiceRegistry externalServiceRegistry() {
        return new ExternalServiceRegistry();
    }
    
    
    /**
     * Called when the application is fully started.
     * Discovers and registers all services and commands.
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Auto-discovering external services and commands...");
        
        // Get the registry from the context
        ExternalServiceRegistry registry = applicationContext.getBean(ExternalServiceRegistry.class);
        
        // Discover and register all ExternalService beans
        Map<String, ExternalService> services = applicationContext.getBeansOfType(ExternalService.class);
        services.forEach((beanName, service) -> {
            registry.registerService(service);
            log.info("Registered external service: {} (bean: {})", service.getServiceName(), beanName);
        });
        
        // Discover and register all ExternalServiceCommand beans
        @SuppressWarnings("rawtypes")
        Map<String, ExternalServiceCommand> commands = applicationContext.getBeansOfType(ExternalServiceCommand.class);
        commands.forEach((beanName, command) -> {
            registry.registerCommand(command);
            log.info("Registered command: {}.{} (bean: {})", 
                     command.getServiceName(), command.getCommandName(), beanName);
        });
        
        log.info("Auto-discovery complete. Registered {} services and {} commands", 
                 services.size(), commands.size());
    }
}
