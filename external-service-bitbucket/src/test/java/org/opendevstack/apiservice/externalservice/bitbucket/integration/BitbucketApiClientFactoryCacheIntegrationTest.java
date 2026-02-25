package org.opendevstack.apiservice.externalservice.bitbucket.integration;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.bitbucket.client.BitbucketApiClient;
import org.opendevstack.apiservice.externalservice.bitbucket.client.BitbucketApiClientFactory;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Integration tests for the {@link BitbucketApiClientFactory} client cache.
 *
 * <p>The {@code @Cacheable} annotation on {@link BitbucketApiClientFactory#getClient(String)} is only
 * activated by the Spring AOP proxy, so caching behaviour must be verified with a real
 * {@link org.springframework.boot.test.context.SpringBootTest} context rather than a plain
 * Mockito unit test.
 *
 * <p>Tests define two lightweight, fake Bitbucket instances via {@link TestPropertySource}.
 * No real Bitbucket connectivity is required – the clients are created but never used to make
 * HTTP calls.
 */
@SpringBootTest(classes = BitbucketIntegrationTestConfig.class)
@TestPropertySource(properties = {
        "externalservices.bitbucket.instances.dev.base-url=https://bitbucket.dev.example.com",
        "externalservices.bitbucket.instances.staging.base-url=https://bitbucket.staging.example.com"
})
class BitbucketApiClientFactoryCacheIntegrationTest {

    @Autowired
    private BitbucketApiClientFactory factory;

    // -------------------------------------------------------------------------
    // Same instance name → same cached instance
    // -------------------------------------------------------------------------

    @Test
    void getClient_sameInstanceName_returnsCachedInstance() throws BitbucketException {
        BitbucketApiClient first  = factory.getClient("dev");
        BitbucketApiClient second = factory.getClient("dev");

        assertSame(first, second,
                "Repeated calls with the same instance name must return the cached client");
    }

    @Test
    void getClient_sameInstanceName_multipleCallsAlwaysReturnSameInstance() throws BitbucketException {
        BitbucketApiClient reference = factory.getClient("staging");

        for (int i = 0; i < 5; i++) {
            assertSame(reference, factory.getClient("staging"),
                    "Call #" + (i + 1) + " should return the same cached client for 'staging'");
        }
    }

    // -------------------------------------------------------------------------
    // Different instance names → different instances
    // -------------------------------------------------------------------------

    @Test
    void getClient_differentInstanceNames_returnDifferentInstances() throws BitbucketException {
        BitbucketApiClient dev     = factory.getClient("dev");
        BitbucketApiClient staging = factory.getClient("staging");

        assertNotSame(dev, staging,
                "Different instance names must produce distinct client objects");
        assertEquals("dev",     dev.getInstanceName());
        assertEquals("staging", staging.getInstanceName());
    }

    // -------------------------------------------------------------------------
    // Default client → cached
    // -------------------------------------------------------------------------

    @Test
    void getDefaultClient_returnsCachedInstance() throws BitbucketException {
        BitbucketApiClient first  = factory.getClient();
        BitbucketApiClient second = factory.getClient();

        assertSame(first, second,
                "Repeated calls to getClient() must return the cached default client");
    }

    // -------------------------------------------------------------------------
    // clearCache → evicts all cached clients
    // -------------------------------------------------------------------------

    @Test
    void clearCache_evictsCachedClients() throws BitbucketException {
        BitbucketApiClient before = factory.getClient("dev");

        factory.clearCache();

        BitbucketApiClient after = factory.getClient("dev");
        assertNotSame(before, after,
                "After clearCache(), a new client instance must be created");
    }
}

