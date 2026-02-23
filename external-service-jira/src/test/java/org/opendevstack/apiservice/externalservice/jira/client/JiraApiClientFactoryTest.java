package org.opendevstack.apiservice.externalservice.jira.client;

import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration;
import org.opendevstack.apiservice.externalservice.jira.config.JiraServiceConfiguration.JiraInstanceConfig;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JiraApiClientFactory}.
 * Focuses on the default-instance resolution logic introduced in {@code resolveInstanceName}.
 */
@ExtendWith(MockitoExtension.class)
class JiraApiClientFactoryTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private JiraServiceConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new JiraServiceConfiguration();
        lenient().when(restTemplateBuilder.build()).thenReturn(restTemplate);
    }

    private JiraApiClientFactory factory() {
        return new JiraApiClientFactory(configuration, restTemplateBuilder);
    }

    // -------------------------------------------------------------------------
    // resolveInstanceName – explicit name
    // -------------------------------------------------------------------------

    @Test
    void resolveInstanceName_explicitName_returnsSameName() throws JiraException {
        assertEquals("dev",  factory().resolveInstanceName("dev"));
        assertEquals("prod", factory().resolveInstanceName("prod"));
    }

    // -------------------------------------------------------------------------
    // resolveInstanceName – null / blank → configured default
    // -------------------------------------------------------------------------

    @Test
    void resolveInstanceName_null_returnsConfiguredDefaultInstance() throws JiraException {
        configuration.setDefaultInstance("prod");

        assertEquals("prod", factory().resolveInstanceName(null));
    }

    @Test
    void resolveInstanceName_blank_returnsConfiguredDefaultInstance() throws JiraException {
        configuration.setDefaultInstance("prod");

        assertEquals("prod", factory().resolveInstanceName(""));
        assertEquals("prod", factory().resolveInstanceName("   "));
    }

    // -------------------------------------------------------------------------
    // resolveInstanceName – null / blank → fallback to first instance
    // -------------------------------------------------------------------------

    @Test
    void resolveInstanceName_null_noDefaultConfigured_returnsFirstInstance() throws JiraException {
        // LinkedHashMap preserves insertion order → "alpha" is first
        Map<String, JiraInstanceConfig> instances = new LinkedHashMap<>();
        instances.put("alpha", config("https://jira-alpha.example.com"));
        instances.put("beta",  config("https://jira-beta.example.com"));
        configuration.setInstances(instances);

        assertEquals("alpha", factory().resolveInstanceName(null));
    }

    @Test
    void resolveInstanceName_blank_noDefaultConfigured_returnsFirstInstance() throws JiraException {
        Map<String, JiraInstanceConfig> instances = new LinkedHashMap<>();
        instances.put("first", config("https://jira.example.com"));
        configuration.setInstances(instances);

        assertEquals("first", factory().resolveInstanceName(""));
        assertEquals("first", factory().resolveInstanceName("   "));
    }

    // -------------------------------------------------------------------------
    // resolveInstanceName – no instances at all → exception
    // -------------------------------------------------------------------------

    @Test
    void resolveInstanceName_null_noInstancesConfigured_throwsJiraException() {
        // no instances set → empty map
        JiraApiClientFactory f = factory();

        JiraException ex = assertThrows(JiraException.class, () -> f.resolveInstanceName(null));
        assertTrue(ex.getMessage().toLowerCase().contains("no jira instances configured"),
                "Expected 'no jira instances configured' in: " + ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // getClient(null) – should delegate to default instance
    // -------------------------------------------------------------------------

    @Test
    void getClient_null_resolvesToConfiguredDefaultInstance() throws JiraException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        configuration.setDefaultInstance("dev");
        configuration.setInstances(Map.of("dev", config("https://jira.dev.example.com")));

        JiraApiClient client = factory().getClient(null);

        assertNotNull(client);
        assertEquals("dev", client.getInstanceName());
    }

    @Test
    void getClient_blank_resolvesToConfiguredDefaultInstance() throws JiraException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        configuration.setDefaultInstance("dev");
        configuration.setInstances(Map.of("dev", config("https://jira.dev.example.com")));

        JiraApiClient client = factory().getClient("");

        assertNotNull(client);
        assertEquals("dev", client.getInstanceName());
    }

    @Test
    void getClient_unknownInstance_throwsJiraException() {
        configuration.setInstances(Map.of("dev", config("https://jira.dev.example.com")));

        JiraException ex = assertThrows(JiraException.class,
                () -> factory().getClient("nonexistent"));
        assertTrue(ex.getMessage().contains("not configured"));
        assertTrue(ex.getMessage().contains("nonexistent"));
    }

    // -------------------------------------------------------------------------
    // getDefaultClient – convenience method
    // -------------------------------------------------------------------------

    @Test
    void getDefaultClient_returnsClientForConfiguredDefaultInstance() throws JiraException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        configuration.setDefaultInstance("prod");
        configuration.setInstances(orderedMap("dev", "prod"));

        JiraApiClient client = factory().getDefaultClient();

        assertNotNull(client);
        assertEquals("prod", client.getInstanceName());
    }

    @Test
    void getDefaultClient_noDefaultConfigured_returnsFirstInstance() throws JiraException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        Map<String, JiraInstanceConfig> instances = new LinkedHashMap<>();
        instances.put("alpha", config("https://jira-alpha.example.com"));
        instances.put("beta",  config("https://jira-beta.example.com"));
        configuration.setInstances(instances);

        JiraApiClient client = factory().getDefaultClient();

        assertNotNull(client);
        assertEquals("alpha", client.getInstanceName());
    }

    @Test
    void getDefaultClient_noInstancesConfigured_throwsJiraException() {
        JiraException ex = assertThrows(JiraException.class, () -> factory().getDefaultClient());
        assertTrue(ex.getMessage().toLowerCase().contains("no jira instances configured"));
    }

    // -------------------------------------------------------------------------
    // Client cache – same instance name returns cached client
    // -------------------------------------------------------------------------

    @Test
    void getClient_cachedOnSecondCall() throws JiraException {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        configuration.setInstances(Map.of("dev", config("https://jira.dev.example.com")));
        JiraApiClientFactory f = factory();

        JiraApiClient first  = f.getClient("dev");
        JiraApiClient second = f.getClient("dev");

        assertSame(first, second, "Second call should return the cached instance");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static JiraInstanceConfig config(String baseUrl) {
        JiraInstanceConfig c = new JiraInstanceConfig();
        c.setBaseUrl(baseUrl);
        return c;
    }

    /** Creates a LinkedHashMap with two configs using their names as base-url stems. */
    private static Map<String, JiraInstanceConfig> orderedMap(String first, String second) {
        Map<String, JiraInstanceConfig> m = new LinkedHashMap<>();
        m.put(first,  config("https://" + first + ".example.com"));
        m.put(second, config("https://" + second + ".example.com"));
        return m;
    }
}
