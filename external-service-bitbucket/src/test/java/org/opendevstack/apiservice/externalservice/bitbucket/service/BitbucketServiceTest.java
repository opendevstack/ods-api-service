package org.opendevstack.apiservice.externalservice.bitbucket.service;

import org.opendevstack.apiservice.externalservice.bitbucket.client.ApiClient;
import org.opendevstack.apiservice.externalservice.bitbucket.client.BitbucketApiClient;
import org.opendevstack.apiservice.externalservice.bitbucket.client.BitbucketApiClientFactory;
import org.opendevstack.apiservice.externalservice.bitbucket.client.model.GetBranches200Response;
import org.opendevstack.apiservice.externalservice.bitbucket.client.model.RestBranch;
import org.opendevstack.apiservice.externalservice.bitbucket.client.model.RestMinimalRef;
import org.opendevstack.apiservice.externalservice.bitbucket.client.model.RestProject;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.opendevstack.apiservice.externalservice.bitbucket.service.impl.BitbucketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BitbucketService
 * These tests use mocks and do not require actual Bitbucket connectivity
 */
@ExtendWith(MockitoExtension.class)
class BitbucketServiceTest {

    @Mock
    private BitbucketApiClientFactory clientFactory;

    @Mock
    private BitbucketApiClient bitbucketApiClient;

    @Mock
    private ApiClient apiClient;

    private BitbucketService bitbucketService;

    @BeforeEach
    void setUp() {
        bitbucketService = new BitbucketServiceImpl(clientFactory);
    }

    @Test
    void testGetDefaultBranch_InstanceNotConfigured() throws BitbucketException {
        // Arrange
        String instanceName = "nonexistent";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";

        when(clientFactory.getClient(instanceName))
            .thenThrow(new BitbucketException("Instance not configured"));

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.getDefaultBranch(instanceName, projectKey, repositorySlug)
        );

        assertTrue(exception.getMessage().contains("Instance not configured"));
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testGetDefaultBranch_ThrowsException() throws BitbucketException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);
        
        // Mock the invokeAPI to throw RestClientException
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(BitbucketException.class, () ->
            bitbucketService.getDefaultBranch(instanceName, projectKey, repositorySlug)
        );

        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }

    @Test
    void testBranchExists_InstanceNotConfigured() throws BitbucketException {
        // Arrange
        String instanceName = "nonexistent";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";
        String branchName = "feature/test";

        when(clientFactory.getClient(instanceName))
            .thenThrow(new BitbucketException("Instance not configured"));

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.branchExists(instanceName, projectKey, repositorySlug, branchName)
        );

        assertTrue(exception.getMessage().contains("Instance not configured"));
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testBranchExists_ThrowsException() throws BitbucketException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";
        String branchName = "feature/test";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);
        
        // Mock the parameterToMultiValueMap method to return empty map instead of null
        when(apiClient.parameterToMultiValueMap(any(), anyString(), any()))
            .thenReturn(new LinkedMultiValueMap<>());
        
        // Mock the invokeAPI to throw RestClientException
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(BitbucketException.class, () ->
            bitbucketService.branchExists(instanceName, projectKey, repositorySlug, branchName)
        );

        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }

    @Test
    void testGetAvailableInstances() {
        // Arrange
        Set<String> expectedInstances = Set.of("dev", "prod");
        when(clientFactory.getAvailableInstances()).thenReturn(expectedInstances);

        // Act
        Set<String> result = bitbucketService.getAvailableInstances();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("dev"));
        assertTrue(result.contains("prod"));
        verify(clientFactory).getAvailableInstances();
    }

    @Test
    void testHasInstance_True() {
        // Arrange
        String instanceName = "dev";
        when(clientFactory.hasInstance(instanceName)).thenReturn(true);

        // Act
        boolean result = bitbucketService.hasInstance(instanceName);

        // Assert
        assertTrue(result);
        verify(clientFactory).hasInstance(instanceName);
    }

    @Test
    void testHasInstance_False() {
        // Arrange
        String instanceName = "nonexistent";
        when(clientFactory.hasInstance(instanceName)).thenReturn(false);

        // Act
        boolean result = bitbucketService.hasInstance(instanceName);

        // Assert
        assertFalse(result);
        verify(clientFactory).hasInstance(instanceName);
    }

    @Test
    void testServiceCreation() {
        // Verify service is properly initialized
        assertNotNull(bitbucketService);
    }
    @Test
    void testGetDefaultBranch_Success() throws Exception {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";
        String expectedBranch = "main";
        
        // Create a RestMinimalRef with the expected branch name
        RestMinimalRef minimalRef = new RestMinimalRef();
        minimalRef.displayId(expectedBranch);
        minimalRef.id("refs/heads/" + expectedBranch);
        
        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);
        
        // Mock the invokeAPI to return the RestMinimalRef
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(minimalRef));
        
        // Act
        String result = bitbucketService.getDefaultBranch(instanceName, projectKey, repositorySlug);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedBranch, result);
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }
    
    @Test
    void testBranchExists_True() throws Exception {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";
        String branchName = "feature/test";
        
        // Create a branch using the constructor
        RestBranch branch = new RestBranch(branchName, false, null, null);
        branch.id("refs/heads/" + branchName);
        
        // Create the response with the branch
        GetBranches200Response response = new GetBranches200Response();
        response.values(List.of(branch));
        
        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);
        
        // Mock the parameterToMultiValueMap and invokeAPI methods
        when(apiClient.parameterToMultiValueMap(any(), anyString(), any()))
            .thenReturn(new LinkedMultiValueMap<>());
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(response));
        
        // Act
        boolean result = bitbucketService.branchExists(instanceName, projectKey, repositorySlug, branchName);
        
        // Assert
        assertTrue(result);
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }
    
    @Test
    void testBranchExists_False() throws Exception {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";
        String branchName = "nonexistent-branch";
        
        // Create empty response (branch not found)
        GetBranches200Response response = new GetBranches200Response();
        response.values(Collections.emptyList());
        
        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);
        
        // Mock the parameterToMultiValueMap and invokeAPI methods
        when(apiClient.parameterToMultiValueMap(any(), anyString(), any()))
            .thenReturn(new LinkedMultiValueMap<>());
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(response));
        
        // Act
        boolean result = bitbucketService.branchExists(instanceName, projectKey, repositorySlug, branchName);
        
        // Assert
        assertFalse(result);
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }

    @Test
    void testProjectExists_True() throws Exception {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);

        // Mock invokeAPI to return a successful response (project found)
        RestProject restProject = new RestProject();
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(restProject));

        // Act
        boolean result = bitbucketService.projectExists(instanceName, projectKey);

        // Assert
        assertTrue(result);
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }

    @Test
    void testProjectExists_False_NotFound() throws Exception {
        // Arrange
        String instanceName = "dev";
        String projectKey = "NONEXISTENT";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);

        // Mock invokeAPI to throw HttpClientErrorException.NotFound (404)
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(HttpClientErrorException.NotFound.create(
                org.springframework.http.HttpStatus.NOT_FOUND, "Not Found",
                org.springframework.http.HttpHeaders.EMPTY, new byte[0], null));

        // Act
        boolean result = bitbucketService.projectExists(instanceName, projectKey);

        // Assert
        assertFalse(result);
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }

    @Test
    void testProjectExists_InstanceNotConfigured() throws BitbucketException {
        // Arrange
        String instanceName = "nonexistent";
        String projectKey = "PROJ";

        when(clientFactory.getClient(instanceName))
            .thenThrow(new BitbucketException("Instance not configured"));

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.projectExists(instanceName, projectKey)
        );

        assertTrue(exception.getMessage().contains("Instance not configured"));
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testProjectExists_ThrowsExceptionOnNetworkError() throws BitbucketException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);

        // Mock invokeAPI to throw RestClientException (network / infrastructure error)
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new RestClientException("Connection refused"));

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.projectExists(instanceName, projectKey)
        );

        assertTrue(exception.getMessage().contains("Failed to check if project"));
        assertTrue(exception.getMessage().contains(projectKey));
        assertNotNull(exception.getCause());
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }
    
    @Test
    void testGetDefaultBranch_Unauthorized() throws BitbucketException {
        // Arrange – simulate a Bitbucket client configured with wrong password
        String instanceName = "dev";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);

        // Mock invokeAPI to throw HttpClientErrorException.Unauthorized (401) – wrong password
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(HttpClientErrorException.Unauthorized.create(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized",
                org.springframework.http.HttpHeaders.EMPTY, new byte[0], null));

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.getDefaultBranch(instanceName, projectKey, repositorySlug)
        );

        assertNotNull(exception.getCause());
        assertInstanceOf(HttpClientErrorException.Unauthorized.class, exception.getCause(),
            "Cause should be HttpClientErrorException.Unauthorized");
        assertTrue(exception.getMessage().contains("Authentication failed (401 Unauthorized)"),
            "Exception message should indicate authentication failure, but was: " + exception.getMessage());
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }

    @Test
    void testBranchExists_Unauthorized() throws BitbucketException {
        // Arrange – simulate a Bitbucket client configured with wrong password
        String instanceName = "dev";
        String projectKey = "PROJ";
        String repositorySlug = "my-repo";
        String branchName = "feature/test";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);

        // Mock the parameterToMultiValueMap method to return empty map instead of null
        when(apiClient.parameterToMultiValueMap(any(), anyString(), any()))
            .thenReturn(new LinkedMultiValueMap<>());

        // Mock invokeAPI to throw HttpClientErrorException.Unauthorized (401) – wrong password
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(HttpClientErrorException.Unauthorized.create(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized",
                org.springframework.http.HttpHeaders.EMPTY, new byte[0], null));

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.branchExists(instanceName, projectKey, repositorySlug, branchName)
        );

        assertNotNull(exception.getCause());
        assertInstanceOf(HttpClientErrorException.Unauthorized.class, exception.getCause(),
            "Cause should be HttpClientErrorException.Unauthorized");
        assertTrue(exception.getMessage().contains("Authentication failed (401 Unauthorized)"),
            "Exception message should indicate authentication failure, but was: " + exception.getMessage());
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }

    @Test
    void testProjectExists_Unauthorized() throws BitbucketException {
        // Arrange – simulate a Bitbucket client configured with wrong password
        String instanceName = "dev";
        String projectKey = "PROJ";

        when(clientFactory.getClient(instanceName)).thenReturn(bitbucketApiClient);
        when(bitbucketApiClient.getApiClient()).thenReturn(apiClient);

        // Mock invokeAPI to throw HttpClientErrorException.Unauthorized (401) – wrong password
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(HttpClientErrorException.Unauthorized.create(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized",
                org.springframework.http.HttpHeaders.EMPTY, new byte[0], null));

        // Act & Assert
        BitbucketException exception = assertThrows(BitbucketException.class, () ->
            bitbucketService.projectExists(instanceName, projectKey)
        );

        assertNotNull(exception.getCause());
        assertInstanceOf(HttpClientErrorException.Unauthorized.class, exception.getCause(),
            "Cause should be HttpClientErrorException.Unauthorized");
        assertTrue(exception.getMessage().contains("Authentication failed (401 Unauthorized)"),
            "Exception message should indicate authentication failure, but was: " + exception.getMessage());
        verify(clientFactory).getClient(instanceName);
        verify(bitbucketApiClient).getApiClient();
    }
}
