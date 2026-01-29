package org.opendevstack.apiservice.externalservice.aap.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration for AAP (Ansible Automation Platform) integration tests.
 * This configuration enables Spring Boot auto-configuration and component scanning
 * for the AAP service module and external service commons.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "org.opendevstack.apiservice.externalservice.aap",
        "org.opendevstack.apiservice.externalservice.commons"
})
public class AapIntegrationTestConfig {
    // Configuration class for integration tests
    // All beans will be auto-configured by Spring Boot
}
