package org.opendevstack.apiservice.externalservice.bitbucket.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration;
import org.opendevstack.apiservice.externalservice.bitbucket.config.BitbucketServiceConfiguration.BitbucketInstanceConfig;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BitbucketApiClientFactory}.
 * Focuses on default-instance resolution logic and client creation.
 */
@ExtendWith(MockitoExtension.class)
class BitbucketApiClientFactoryTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private BitbucketServiceConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new BitbucketServiceConfiguration();
        lenient().when(restTemplateBuilder.build()).thenReturn(restTemplate);
    }

    private BitbucketApiClientFactory factory() {
        return new BitbucketApiClientFactory(configuration, restTemplateBuilder);
    }

    // -------------------------------------------------------------------------
    // getDefaultInstanceName → configured default
    // -------------------------------------------------------------------------

    @Test
    void getDefaultInstanceName_returnsConfiguredDefaultInstance() throws BitbucketException {
        configuration.setDefaultInstance("prod");
        configuration.setInstances(Map.of("prod", config("https://bitbucket.prod.example.com")));

        assertEquals("prod", factory().getDefaultInstanceName());
    }

    // -------------------------------------------------------------------------
    // getDefaultInstanceName – without default → fallback to first instance
    // -------------------------------------------------------------------------

    @Test
    void getDefaultInstanceName_noDefaultConfigured_returnsFirstInstance() throws BitbucketException {
        Map<String, BitbucketInstanceConfig> instances = new LinkedHashMap<>();
        instances.put("alpha", config("https://bitbucket-alpha.example.com"));
        instances.put("beta", config("https://bitbucket-beta.example.com"));
        configuration.setInstances(instances);

        assertEquals("alpha", factory().getDefaultInstanceName());
    }

    // -------------------------------------------------------------------------
    // getDefaultInstanceName – no instances at all → exception
    // -------------------------------------------------------------------------

    @Test
    void getDefaultInstanceName_noInstancesConfigured_throwsBitbucketException() {
        BitbucketApiClientFactory f = factory();

        BitbucketException ex = assertThrows(BitbucketException.class, f::getDefaultInstanceName);
        assertTrue(ex.getMessage().toLowerCase().contains("no bitbucket instances configured"),
                "Expected 'no bitbucket instances configured' in: " + ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // getClient(null) – should throw
    // -------------------------------------------------------------------------

    @Test
    void getClient_null_throwsBitbucketException() {
        BitbucketException ex = assertThrows(BitbucketException.class, () -> factory().getClient(null));
        assertTrue(ex.getMessage().toLowerCase().contains("provide instance name"),
                "Expected 'provide instance name' in: " + ex.getMessage());
    }

    @Test
    void getClient_blank_throwsBitbucketException() {
        BitbucketException ex = assertThrows(BitbucketException.class, () -> factory().getClient(""));
        assertTrue(ex.getMessage().toLowerCase().contains("provide instance name"),
                "Expected 'provide instance name' in: " + ex.getMessage());
    }

    @Test
    void getClient_unknownInstance_throwsBitbucketException() {
        configuration.setInstances(Map.of("dev", config("https://bitbucket.dev.example.com")));

        BitbucketException ex = assertThrows(BitbucketException.class,
                () -> factory().getClient("nonexistent"));
        assertTrue(ex.getMessage().contains("not configured"));
        assertTrue(ex.getMessage().contains("nonexistent"));
    }

    // -------------------------------------------------------------------------
    // getClient(instanceName) – valid instance
    // -------------------------------------------------------------------------

    @Test
    void getClient_validInstance_returnsClient() throws BitbucketException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        configuration.setInstances(Map.of("dev", config("https://bitbucket.dev.example.com")));

        BitbucketApiClient client = factory().getClient("dev");

        assertNotNull(client);
        assertEquals("dev", client.getInstanceName());
    }

    // -------------------------------------------------------------------------
    // getClient() – convenience method for default instance
    // -------------------------------------------------------------------------

    @Test
    void getClient_returnsClientForConfiguredDefaultInstance() throws BitbucketException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        configuration.setDefaultInstance("prod");
        configuration.setInstances(orderedMap("dev", "prod"));

        BitbucketApiClient client = factory().getClient();

        assertNotNull(client);
        assertEquals("prod", client.getInstanceName());
    }

    @Test
    void getClient_noDefaultConfigured_returnsFirstInstance() throws BitbucketException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        Map<String, BitbucketInstanceConfig> instances = new LinkedHashMap<>();
        instances.put("alpha", config("https://bitbucket-alpha.example.com"));
        instances.put("beta", config("https://bitbucket-beta.example.com"));
        configuration.setInstances(instances);

        BitbucketApiClient client = factory().getClient();

        assertNotNull(client);
        assertEquals("alpha", client.getInstanceName());
    }

    @Test
    void getClient_noInstancesConfigured_throwsBitbucketException() {
        BitbucketException ex = assertThrows(BitbucketException.class, () -> factory().getClient());
        assertTrue(ex.getMessage().toLowerCase().contains("no bitbucket instances configured"));
    }

    // -------------------------------------------------------------------------
    // getAvailableInstances & hasInstance
    // -------------------------------------------------------------------------

    @Test
    void getAvailableInstances_returnsConfiguredNames() {
        configuration.setInstances(orderedMap("dev", "prod"));

        assertEquals(2, factory().getAvailableInstances().size());
        assertTrue(factory().getAvailableInstances().contains("dev"));
        assertTrue(factory().getAvailableInstances().contains("prod"));
    }

    @Test
    void hasInstance_returnsTrueForConfigured() {
        configuration.setInstances(Map.of("dev", config("https://bitbucket.dev.example.com")));

        assertTrue(factory().hasInstance("dev"));
        assertFalse(factory().hasInstance("nonexistent"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static BitbucketInstanceConfig config(String baseUrl) {
        BitbucketInstanceConfig c = new BitbucketInstanceConfig();
        c.setBaseUrl(baseUrl);
        return c;
    }

    /** Creates a LinkedHashMap with two configs using their names as base-url stems. */
    private static Map<String, BitbucketInstanceConfig> orderedMap(String first, String second) {
        Map<String, BitbucketInstanceConfig> m = new LinkedHashMap<>();
        m.put(first, config("https://" + first + ".example.com"));
        m.put(second, config("https://" + second + ".example.com"));
        return m;
    }
}

