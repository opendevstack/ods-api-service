package org.opendevstack.apiservice.externalservice.jira.integration;

import org.opendevstack.apiservice.externalservice.jira.client.JiraApiClient;
import org.opendevstack.apiservice.externalservice.jira.client.JiraApiClientFactory;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link JiraApiClientFactory} client cache.
 *
 * <p>The {@code @Cacheable} annotation on {@link JiraApiClientFactory#getClient(String)} is only
 * activated by the Spring AOP proxy, so caching behaviour must be verified with a real
 * {@link org.springframework.boot.test.context.SpringBootTest} context rather than a plain
 * Mockito unit test.
 *
 * <p>Tests define two lightweight, fake Jira instances via {@link TestPropertySource}.
 * No real Jira connectivity is required – the clients are created but never used to make
 * HTTP calls.
 */
@SpringBootTest(classes = JiraIntegrationTestConfig.class)
@TestPropertySource(properties = {
        "externalservices.jira.instances.dev.base-url=https://jira.dev.example.com",
        "externalservices.jira.instances.staging.base-url=https://jira.staging.example.com"
})
class JiraApiClientFactoryCacheIntegrationTest {

    @Autowired
    private JiraApiClientFactory factory;

    // -------------------------------------------------------------------------
    // Same instance name → same cached instance
    // -------------------------------------------------------------------------

    @Test
    void getClient_sameInstanceName_returnsCachedInstance() throws JiraException {
        JiraApiClient first  = factory.getClient("dev");
        JiraApiClient second = factory.getClient("dev");

        assertSame(first, second,
                "Repeated calls with the same instance name must return the cached client");
    }

    @Test
    void getClient_sameInstanceName_multipleCallsAlwaysReturnSameInstance() throws JiraException {
        JiraApiClient reference = factory.getClient("staging");

        for (int i = 0; i < 5; i++) {
            assertSame(reference, factory.getClient("staging"),
                    "Call #" + (i + 1) + " should return the same cached client for 'staging'");
        }
    }

    // -------------------------------------------------------------------------
    // Different instance names → different instances
    // -------------------------------------------------------------------------

    @Test
    void getClient_differentInstanceNames_returnDifferentInstances() throws JiraException {
        JiraApiClient dev     = factory.getClient("dev");
        JiraApiClient staging = factory.getClient("staging");

        assertNotSame(dev, staging,
                "Different instance names must produce distinct client objects");
        assertEquals("dev",     dev.getInstanceName());
        assertEquals("staging", staging.getInstanceName());
    }
}
