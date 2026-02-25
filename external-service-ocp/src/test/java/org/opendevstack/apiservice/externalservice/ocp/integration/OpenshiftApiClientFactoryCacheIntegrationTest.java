package org.opendevstack.apiservice.externalservice.ocp.integration;

import org.opendevstack.apiservice.externalservice.ocp.client.OpenshiftApiClient;
import org.opendevstack.apiservice.externalservice.ocp.client.OpenshiftApiClientFactory;
import org.opendevstack.apiservice.externalservice.ocp.exception.OpenshiftException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link OpenshiftApiClientFactory} client cache.
 *
 * <p>The {@code @Cacheable} annotation on {@link OpenshiftApiClientFactory#getClient(String)} is only
 * activated by the Spring AOP proxy, so caching behaviour must be verified with a real
 * {@link org.springframework.boot.test.context.SpringBootTest} context rather than a plain
 * Mockito unit test.
 *
 * <p>Tests define two lightweight, fake OpenShift instances via {@link TestPropertySource}.
 * No real OpenShift connectivity is required – the clients are created but never used to make
 * API calls.
 */
@SpringBootTest(classes = OpenshiftIntegrationTestConfig.class)
@TestPropertySource(properties = {
        "externalservices.openshift.instances.dev.api-url=https://api.dev.ocp.example.com:6443",
        "externalservices.openshift.instances.dev.token=fake-dev-token",
        "externalservices.openshift.instances.dev.namespace=dev-ns",
        "externalservices.openshift.instances.dev.trust-all-certificates=true",
        "externalservices.openshift.instances.staging.api-url=https://api.staging.ocp.example.com:6443",
        "externalservices.openshift.instances.staging.token=fake-staging-token",
        "externalservices.openshift.instances.staging.namespace=staging-ns",
        "externalservices.openshift.instances.staging.trust-all-certificates=true"
})
class OpenshiftApiClientFactoryCacheIntegrationTest {

    @Autowired
    private OpenshiftApiClientFactory factory;

    // -------------------------------------------------------------------------
    // Same instance name → same cached instance
    // -------------------------------------------------------------------------

    @Test
    void getClient_sameInstanceName_returnsCachedInstance() throws OpenshiftException {
        OpenshiftApiClient first  = factory.getClient("dev");
        OpenshiftApiClient second = factory.getClient("dev");

        assertSame(first, second,
                "Repeated calls with the same instance name must return the cached client");
    }

    @Test
    void getClient_sameInstanceName_multipleCallsAlwaysReturnSameInstance() throws OpenshiftException {
        OpenshiftApiClient reference = factory.getClient("staging");

        for (int i = 0; i < 5; i++) {
            assertSame(reference, factory.getClient("staging"),
                    "Call #" + (i + 1) + " should return the same cached client for 'staging'");
        }
    }

    // -------------------------------------------------------------------------
    // Different instance names → different instances
    // -------------------------------------------------------------------------

    @Test
    void getClient_differentInstanceNames_returnDifferentInstances() throws OpenshiftException {
        OpenshiftApiClient dev     = factory.getClient("dev");
        OpenshiftApiClient staging = factory.getClient("staging");

        assertNotSame(dev, staging,
                "Different instance names must produce distinct client objects");
        assertEquals("dev",     dev.getInstanceName());
        assertEquals("staging", staging.getInstanceName());
    }

    // -------------------------------------------------------------------------
    // Default client is cached
    // -------------------------------------------------------------------------

    @Test
    void getDefaultClient_returnsCachedInstance() throws OpenshiftException {
        OpenshiftApiClient first  = factory.getDefaultClient();
        OpenshiftApiClient second = factory.getDefaultClient();

        assertSame(first, second,
                "Repeated calls to getDefaultClient must return the cached client");
    }

    // -------------------------------------------------------------------------
    // clearCache evicts all entries
    // -------------------------------------------------------------------------

    @Test
    void clearCache_evictsAllCachedClients() throws OpenshiftException {
        OpenshiftApiClient before = factory.getClient("dev");

        factory.clearCache();

        OpenshiftApiClient after = factory.getClient("dev");
        assertNotSame(before, after,
                "After clearing cache, a new client instance should be created");
    }

    // -------------------------------------------------------------------------
    // Get client with null/blank instance name should throw exception
    // -------------------------------------------------------------------------
    @Test
    void getClient_nullOrBlankInstanceName_throwsExceptionAndDoesNotCache() {
        assertThrows(OpenshiftException.class, () -> factory.getClient(null),
                "Calling getClient with null instance name should throw OpenshiftException");
        assertThrows(OpenshiftException.class, () -> factory.getClient(""),
                "Calling getClient with blank instance name should throw OpenshiftException");
        assertThrows(OpenshiftException.class, () -> factory.getClient("   "),
                "Calling getClient with blank instance name should throw OpenshiftException");
    }
}
