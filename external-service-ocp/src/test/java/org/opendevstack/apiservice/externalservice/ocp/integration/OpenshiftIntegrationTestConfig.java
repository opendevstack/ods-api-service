package org.opendevstack.apiservice.externalservice.ocp.integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Configuration for OpenShift integration tests.
 * Loads the full Spring Boot context with all auto-configured beans.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "org.opendevstack.apiservice.externalservice.ocp",
        "org.opendevstack.apiservice.externalservice.commons"
})
public class OpenshiftIntegrationTestConfig {
}
