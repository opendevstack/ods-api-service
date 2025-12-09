package org.opendevstack.apiservice.externalservice.commons.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.commons.registry.ExternalServiceRegistry;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Auto-configuration for external service health indicators.
 * Creates health indicators for all registered external services.
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@RequiredArgsConstructor
public class ExternalServiceHealthConfiguration {
    
    private final ExternalServiceRegistry registry;
    
    /**
     * Creates a composite health contributor that includes all external services.
     * Each service will appear under /actuator/health/externalServices/{serviceName}
     */
    @Bean
    public HealthContributor externalServicesHealthContributor() {
        Map<String, HealthContributor> indicators = registry.getAllServices().stream()
            .collect(Collectors.toMap(
                ExternalService::getServiceName,
                service -> new ExternalServiceHealthIndicator(service)
            ));
        
        log.info("Registered health indicators for {} external services", indicators.size());
        
        return CompositeHealthContributor.fromMap(indicators);
    }
}
