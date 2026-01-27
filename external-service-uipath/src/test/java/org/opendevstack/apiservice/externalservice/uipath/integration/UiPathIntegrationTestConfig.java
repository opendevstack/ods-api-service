package org.opendevstack.apiservice.externalservice.uipath.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.opendevstack.apiservice.externalservice.uipath.config.UiPathProperties;

/**
 * Test configuration for UiPath integration tests.
 * This configuration enables Spring Boot auto-configuration and component scanning
 * for the UiPath service module.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties(UiPathProperties.class)
@ComponentScan(basePackages = {
        "org.opendevstack.apiservice.externalservice.uipath",
        "org.opendevstack.apiservice.externalservice.commons"
})
public class UiPathIntegrationTestConfig {
    // Configuration class for integration tests
    // All beans will be auto-configured by Spring Boot
}
