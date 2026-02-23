package org.opendevstack.apiservice.externalservice.jira.service;

import org.opendevstack.apiservice.externalservice.jira.client.ApiClient;
import org.opendevstack.apiservice.externalservice.jira.client.JiraApiClient;
import org.opendevstack.apiservice.externalservice.jira.client.JiraApiClientFactory;
import org.opendevstack.apiservice.externalservice.jira.exception.JiraException;
import org.opendevstack.apiservice.externalservice.jira.service.impl.JiraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JiraService}.
 * These tests use mocks and do not require actual Jira connectivity.
 */
@ExtendWith(MockitoExtension.class)
class JiraServiceTest {

    @Mock
    private JiraApiClientFactory clientFactory;

    @Mock
    private JiraApiClient jiraApiClient;

    @Mock
    private ApiClient apiClient;

    private JiraService jiraService;

    @BeforeEach
    void setUp() {
        jiraService = new JiraServiceImpl(clientFactory);
        // Stub ApiClient utility methods used by the generated ProjectApi / ServerInfoApi
        // before invokeAPI is reached. Without these, putAll(null) causes NullPointerException.
        lenient().when(apiClient.parameterToMultiValueMap(any(), anyString(), any()))
                .thenReturn(new LinkedMultiValueMap<>());
        lenient().when(apiClient.selectHeaderAccept(any()))
                .thenReturn(List.of(MediaType.APPLICATION_JSON));
        lenient().when(apiClient.selectHeaderContentType(any()))
                .thenReturn(MediaType.APPLICATION_JSON);
    }

    // -------------------------------------------------------------------------
    // projectExists
    // -------------------------------------------------------------------------

    @Test
    void testProjectExists_InstanceNotConfigured() throws JiraException {
        // Arrange
        String instanceName = "nonexistent";
        String projectKey = "PROJ";

        when(clientFactory.getClient(instanceName))
                .thenThrow(new JiraException("Jira instance 'nonexistent' is not configured"));

        // Act & Assert
        JiraException exception = assertThrows(JiraException.class, () ->
                jiraService.projectExists(instanceName, projectKey));

        assertTrue(exception.getMessage().contains("not configured"));
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testProjectExists_RestClientException() throws JiraException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";

        when(clientFactory.getClient(instanceName)).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(JiraException.class, () ->
                jiraService.projectExists(instanceName, projectKey));

        verify(clientFactory).getClient(instanceName);
        verify(jiraApiClient).getApiClient();
    }

    @Test
    void testProjectExists_NotFound_ReturnsFalse() throws JiraException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "UNKNOWN";
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(notFoundEx);

        // Act
        boolean result = jiraService.projectExists(instanceName, projectKey);

        // Assert
        assertFalse(result);
        verify(clientFactory).getClient(instanceName);
    }

    // -------------------------------------------------------------------------
    // isHealthy
    // -------------------------------------------------------------------------

    @Test
    void testIsHealthy_NoInstancesConfigured_ReturnsFalse() {
        // Arrange
        when(clientFactory.getAvailableInstances()).thenReturn(Collections.emptySet());

        // Act
        boolean result = jiraService.isHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsHealthy_RestClientException_ReturnsFalse() throws JiraException {
        // Arrange
        when(clientFactory.getAvailableInstances()).thenReturn(Set.of("dev"));
        when(clientFactory.getDefaultClient()).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        // Act
        boolean result = jiraService.isHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsHealthy_JiraException_ReturnsFalse() throws JiraException {
        // Arrange
        when(clientFactory.getAvailableInstances()).thenReturn(Set.of("dev"));
        when(clientFactory.getDefaultClient()).thenThrow(new JiraException("No Jira instances configured"));

        // Act
        boolean result = jiraService.isHealthy();

        // Assert
        assertFalse(result);
    }

    // -------------------------------------------------------------------------
    // getAvailableInstances / hasInstance
    // -------------------------------------------------------------------------

    @Test
    void testGetAvailableInstances() {
        // Arrange
        Set<String> expected = Set.of("dev", "prod");
        when(clientFactory.getAvailableInstances()).thenReturn(expected);

        // Act
        Set<String> result = jiraService.getAvailableInstances();

        // Assert
        assertEquals(expected, result);
        verify(clientFactory).getAvailableInstances();
    }

    @Test
    void testHasInstance_Existing_ReturnsTrue() {
        // Arrange
        when(clientFactory.hasInstance("dev")).thenReturn(true);

        // Act + Assert
        assertTrue(jiraService.hasInstance("dev"));
    }

    @Test
    void testHasInstance_NonExistent_ReturnsFalse() {
        // Arrange
        when(clientFactory.hasInstance("nope")).thenReturn(false);

        // Act + Assert
        assertFalse(jiraService.hasInstance("nope"));
    }

    // -------------------------------------------------------------------------
    // Default-instance support
    // -------------------------------------------------------------------------

    @Test
    void testProjectExists_NoInstanceArg_UsesDefaultClient() throws JiraException {
        // projectExists(key) → projectExists(null, key) → getClient(null) → default instance
        String projectKey = "PROJ";

        when(clientFactory.getClient(null)).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(jiraApiClient.getInstanceName()).thenReturn("dev");
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        boolean result = jiraService.projectExists(projectKey);

        assertTrue(result);
        verify(clientFactory).getClient(null);
    }

    @Test
    void testProjectExists_NullInstanceName_UsesDefaultClient() throws JiraException {
        // Passing null explicitly as instanceName should also resolve to the default
        String projectKey = "PROJ";

        when(clientFactory.getClient(null)).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(jiraApiClient.getInstanceName()).thenReturn("dev");
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        boolean result = jiraService.projectExists(null, projectKey);

        assertTrue(result);
        verify(clientFactory).getClient(null);
    }

    @Test
    void testProjectExists_BlankInstanceName_UsesDefaultClient() throws JiraException {
        String projectKey = "PROJ";

        when(clientFactory.getClient("")).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(jiraApiClient.getInstanceName()).thenReturn("dev");
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));

        boolean result = jiraService.projectExists("", projectKey);

        assertTrue(result);
        verify(clientFactory).getClient("");
    }

    @Test
    void testProjectExists_NoInstanceArg_NotFound_ReturnsFalse() throws JiraException {
        String projectKey = "ZZZNOPE";
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(null)).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(notFoundEx);

        boolean result = jiraService.projectExists(projectKey);

        assertFalse(result);
    }

    @Test
    void testProjectExists_NoInstanceArg_RestClientException_ThrowsJiraException() throws JiraException {
        when(clientFactory.getClient(null)).thenReturn(jiraApiClient);
        when(jiraApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("timeout"));

        assertThrows(JiraException.class, () -> jiraService.projectExists("PROJ"));
    }

    @Test
    void testGetDefaultInstance_DelegatesToFactory() throws JiraException {
        when(clientFactory.resolveInstanceName(null)).thenReturn("prod");

        String result = jiraService.getDefaultInstance();

        assertEquals("prod", result);
        verify(clientFactory).resolveInstanceName(null);
    }

    @Test
    void testGetDefaultInstance_FactoryThrows_PropagatesException() throws JiraException {
        when(clientFactory.resolveInstanceName(null))
                .thenThrow(new JiraException("No Jira instances configured"));

        assertThrows(JiraException.class, () -> jiraService.getDefaultInstance());
    }
}
