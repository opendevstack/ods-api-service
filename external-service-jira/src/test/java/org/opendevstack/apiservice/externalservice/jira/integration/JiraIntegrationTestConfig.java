package org.opendevstack.apiservice.externalservice.jira.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration for Jira integration tests.
 * Enables Spring Boot auto-configuration and component scanning
 * for the jira service module.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.opendevstack.apiservice.externalservice.jira")
public class JiraIntegrationTestConfig {
    // All beans will be auto-configured by Spring Boot
}
