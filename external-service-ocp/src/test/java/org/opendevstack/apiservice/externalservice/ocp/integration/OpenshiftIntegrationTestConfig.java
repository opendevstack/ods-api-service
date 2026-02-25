package org.opendevstack.apiservice.externalservice.ocp.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration for OpenShift integration tests.
 * This configuration enables Spring Boot auto-configuration, caching and component scanning
 * for the OpenShift service module.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableCaching
@ComponentScan(basePackages = "org.opendevstack.apiservice.externalservice.ocp")
public class OpenshiftIntegrationTestConfig {
    // Configuration class for integration tests
    // All beans will be auto-configured by Spring Boot
}
