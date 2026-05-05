package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.opendevstack.apiservice.core.security.obo.OboTokenService;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.client.MarketplaceApiClientFactory;
import org.opendevstack.apiservice.externalservice.marketplace.config.MarketplaceInstanceConfig;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.ApiClient;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentExtendedInfo;
import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentInfo;
import org.opendevstack.apiservice.externalservice.marketplace.service.impl.MarketplaceServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MarketplaceService}.
 * These tests use mocks and do not require actual Marketplace connectivity.
 */
@ExtendWith(MockitoExtension.class)
class MarketplaceServiceImplTest {

    // TODO tests for the rest of the methods in MarketplaceServiceImpl

    @Mock
    private MarketplaceApiClientFactory clientFactory;

    @Mock
    private MarketplaceApiClient marketplaceApiClient;

    @Mock
    private ApiClient apiClient;

    @Mock
    private OboTokenService oboTokenService;

    private MarketplaceService marketplaceService;

    @BeforeEach
    void setUp() {
        marketplaceService = new MarketplaceServiceImpl(clientFactory, oboTokenService);

        // Set up a fake SecurityContext so JwtUtils.getTokenValue() works
        Jwt jwt = Jwt.withTokenValue("test-jwt-assertion")
                .header("alg", "RS256")
                .claim("azp", "test-client-id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(ctx);

        // Default OBO stub — tests that fail before OBO won't reach this
        lenient().when(oboTokenService.exchangeToken(anyString(), anyString()))
                .thenReturn("obo-test-token");

        // Stub ApiClient utility methods used by the generated ProjectApi / ServerInfoApi
        // before invokeAPI is reached. Without these, putAll(null) causes NullPointerException.
        lenient().when(apiClient.parameterToMultiValueMap(any(), anyString(), any()))
                .thenReturn(new LinkedMultiValueMap<>());
        lenient().when(apiClient.selectHeaderAccept(any()))
                .thenReturn(List.of(MediaType.APPLICATION_JSON));
        lenient().when(apiClient.selectHeaderContentType(any()))
                .thenReturn(MediaType.APPLICATION_JSON);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // getProjectComponent
    // -------------------------------------------------------------------------

    @Test
    void testGetProjectComponent_InstanceNotConfigured() throws MarketplaceException {
        // Arrange
        String instanceName = "nonexistent";
        String projectKey = "PROJ";
        String componentId = "test-component";

        when(clientFactory.getClient(instanceName))
                .thenThrow(new MarketplaceException("Marketplace instance 'nonexistent' is not configured"));

        // Act & Assert
        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
                marketplaceService.getProjectComponent(instanceName, projectKey, componentId));

        assertTrue(exception.getMessage().contains("not configured"));
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testGetProjectComponent_RestClientException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "PROJ";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.getProjectComponent(instanceName, projectKey, componentId));

        verify(clientFactory).getClient(instanceName);
        verify(marketplaceApiClient).getApiClient();
    }

    @Test
    void testGetProjectComponent_NotFound_ReturnsNull() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String projectKey = "UNKNOWN";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        ProjectComponentExtendedInfo result = marketplaceService.getProjectComponent(instanceName, projectKey, componentId);

        // Assert
        assertNull(result);
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
        boolean result = marketplaceService.isHealthy();

        // Assert
        assertFalse(result);
    }

    // TODO reenable these tests when we implement the health check to actually call the Marketplace API.
    //  For now, since isHealthy only checks if instances are configured, this test is not relevant
    //  and fails due to the RestClientException being thrown by the mocked ApiClient when invokeAPI is called.
//    @Test
//    void testIsHealthy_RestClientException_ReturnsFalse() throws MarketplaceException {
//        // Arrange
//        when(clientFactory.getAvailableInstances()).thenReturn(Set.of("dev"));
//        when(clientFactory.getClient()).thenReturn(marketplaceApiClient);
//        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
//        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
//                .thenThrow(new RestClientException("Connection refused"));
//
//        // Act
//        boolean result = marketplaceService.isHealthy();
//
//        // Assert
//        assertFalse(result);
//    }
//
//    @Test
//    void testIsHealthy_WhenException_ReturnsFalse() throws MarketplaceException {
//        // Arrange
//        when(clientFactory.getAvailableInstances()).thenReturn(Set.of("dev"));
//        when(clientFactory.getClient()).thenThrow(new MarketplaceException("No Marketplace instances configured"));
//
//        // Act
//        boolean result = marketplaceService.isHealthy();
//
//        // Assert
//        assertFalse(result);
//    }

    // -------------------------------------------------------------------------
    // getAvailableInstances / hasInstance
    // -------------------------------------------------------------------------

    @Test
    void testGetAvailableInstances() {
        // Arrange
        Set<String> expected = Set.of("dev", "prod");
        when(clientFactory.getAvailableInstances()).thenReturn(expected);

        // Act
        Set<String> result = marketplaceService.getAvailableInstances();

        // Assert
        assertEquals(expected, result);
        verify(clientFactory).getAvailableInstances();
    }

    @Test
    void testHasInstance_Existing_ReturnsTrue() {
        // Arrange
        when(clientFactory.hasInstance("dev")).thenReturn(true);

        // Act + Assert
        assertTrue(marketplaceService.hasInstance("dev"));
    }

    @Test
    void testHasInstance_NonExistent_ReturnsFalse() {
        // Arrange
        when(clientFactory.hasInstance("nope")).thenReturn(false);

        // Act + Assert
        assertFalse(marketplaceService.hasInstance("nope"));
    }

    // -------------------------------------------------------------------------
    // Default-instance support
    // -------------------------------------------------------------------------

    @Test
    void testGetProjectComponent_NoInstanceArg_UsesDefaultClient() throws MarketplaceException {
        String projectKey = "PROJ";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        ProjectComponentExtendedInfo result = marketplaceService.getProjectComponent(projectKey, componentId);

        assertNull(result);
        verify(clientFactory).getClient("default");
    }

    @Test
    void testGetProjectComponent_NullInstanceName_UsesDefaultClient() throws MarketplaceException {
        // Passing null explicitly as instanceName should resolve via getDefaultInstance -> getAuthenticatedClient
        String projectKey = "PROJ";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getClient(null)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        ProjectComponentExtendedInfo result = marketplaceService.getProjectComponent(null, projectKey, componentId);

        assertNull(result);
        verify(clientFactory).getClient(null);
    }

    @Test
    void testGetProjectComponent_BlankInstanceName_UsesDefaultClient() throws MarketplaceException {
        String projectKey = "PROJ";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getClient("")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        ProjectComponentExtendedInfo result = marketplaceService.getProjectComponent("", projectKey, componentId);

        assertNull(result);
        verify(clientFactory).getClient("");
    }

    @Test
    void testGetProjectComponent_NoInstanceArg_NotFound_ReturnsFalse() throws MarketplaceException {
        String projectKey = "ZZZNOPE";
        String componentId = "test-component";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        ProjectComponentExtendedInfo result = marketplaceService.getProjectComponent(projectKey, componentId);

        assertNull(result);
    }

    @Test
    void testGetProjectComponent_NoInstanceArg_RestClientException_ThrowsMarketplaceException() throws MarketplaceException {
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setOboScope("api://test/scope");

        when(clientFactory.getDefaultInstanceName()).thenReturn("default");
        when(clientFactory.getClient("default")).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("timeout"));
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        assertThrows(MarketplaceException.class, () -> marketplaceService.getProjectComponent("PROJ",
                "test-component"));
    }

    @Test
    void testGetDefaultInstance_DelegatesToFactory() throws MarketplaceException {
        when(clientFactory.getDefaultInstanceName()).thenReturn("prod");

        String result = marketplaceService.getDefaultInstance();

        assertEquals("prod", result);
        verify(clientFactory).getDefaultInstanceName();
    }

    @Test
    void testGetDefaultInstance_FactoryThrows_PropagatesException() throws MarketplaceException {
        when(clientFactory.getDefaultInstanceName())
                .thenThrow(new MarketplaceException("No Marketplace instances configured"));

        assertThrows(MarketplaceException.class, () -> marketplaceService.getDefaultInstance());
    }

    @Test
    void testProvisionProjectComponent_Conflict_ThrowsMarketplaceExceptionWithDuplicateMessage() throws MarketplaceException {
        String instanceName = "dev";
        String projectKey = "EDPC";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        instanceConfig.setProvisionerActionsBaseUrl("https://example/provision-actions");
        instanceConfig.setOboScope("api://test/scope");

        HttpClientErrorException conflictEx = HttpClientErrorException.create(
            HttpStatus.CONFLICT,
            "Conflict",
            HttpHeaders.EMPTY,
            "{\"message\":\"This component name already exists, please choose another name.\"}".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8
        );

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(conflictEx);

        MarketplaceException exception = assertThrows(MarketplaceException.class, () ->
            marketplaceService.provisionProjectComponent(instanceName, projectKey, List.of()));

        assertEquals("This component name already exists, please choose another name.", exception.getMessage());
    }

    @Test
    void testGetCatalogItem_RestClientException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String catalogItemId = "test-catalog-item-base64-string";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.getCatalogItem(instanceName, catalogItemId));

        verify(clientFactory).getClient(instanceName);
        verify(marketplaceApiClient).getApiClient();
    }

    @Test
    void testGetCatalogItem_NotFound_ReturnsNull() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String catalogItemId = "test-catalog-item-base64-string";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        CatalogItem result = marketplaceService.getCatalogItem(instanceName, catalogItemId);

        // Assert
        assertNull(result);
        verify(clientFactory).getClient(instanceName);
    }


    @Test
    void testDeleteProjectComponent_RestClientException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String componentId = "test-component-id";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.deleteProjectComponent(instanceName, componentId));

        verify(clientFactory).getClient(instanceName);
        verify(marketplaceApiClient).getApiClient();
    }

    @Test
    void testDeleteProjectComponent_NotFound_ThrowsException() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String componentId = "test-component-id";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(notFoundEx);
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        assertThrows(MarketplaceException.class, () ->
                marketplaceService.deleteProjectComponent(instanceName, componentId));

        // Assert
        verify(clientFactory).getClient(instanceName);
    }

    @Test
    void testDeleteProjectComponent_ComponentExists_NoExceptionThrown() throws MarketplaceException {
        // Arrange
        String instanceName = "dev";
        String componentId = "test-component-id";
        MarketplaceInstanceConfig instanceConfig = new MarketplaceInstanceConfig();
        HttpClientErrorException notFoundEx = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);

        when(clientFactory.getDefaultInstanceName()).thenReturn(instanceName);
        when(clientFactory.getClient(instanceName)).thenReturn(marketplaceApiClient);
        when(marketplaceApiClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.invokeAPI(anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(null));
        when(marketplaceApiClient.getConfig()).thenReturn(instanceConfig);

        // Act
        marketplaceService.deleteProjectComponent(instanceName, componentId);

        // Assert
        verify(clientFactory).getClient(instanceName);
    }

}