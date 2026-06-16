package org.opendevstack.apiservice.core.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtTokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private JwtTokenProperties properties;

    private JwtTokenService sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        properties = new JwtTokenProperties();
        properties.setTokenUrl("https://login.microsoftonline.com/<app-tenant-id>/oauth2/v2.0/token");
        properties.setClientId("test-client-id");
        properties.setClientSecret("test-client-secret");
        sut = new JwtTokenService(properties, restTemplate);
    }

    @Test
    void request_token_returns_access_token_on_success() {
        // GIVEN
        String tenantId = "tenant-id";
        String scope = "api://target-app/Api.Access";
        JwtTokenResponse tokenResponse = new JwtTokenResponse();
        tokenResponse.setAccessToken("jwt-access-token");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(3600);
        tokenResponse.setScope(scope);

        String expectedUrl = properties.getTokenUrl().replace("<app-tenant-id>", tenantId);

        when(restTemplate.postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(JwtTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        // WHEN
        String result = sut.requestToken(scope, tenantId);

        // THEN
        assertEquals("jwt-access-token", result);
        verify(restTemplate).postForEntity(eq(expectedUrl), any(HttpEntity.class), eq(JwtTokenResponse.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void request_token_sends_correct_form_parameters_and_headers() {
        // GIVEN
        String tenantId = "t-1";
        String scope = "api://app/scope";
        JwtTokenResponse tokenResponse = new JwtTokenResponse();
        tokenResponse.setAccessToken("token");

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(anyString(), captor.capture(), eq(JwtTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        // WHEN
        sut.requestToken(scope, tenantId);

        // THEN
        MultiValueMap<String, String> body = captor.getValue().getBody();
        assertNotNull(body);
        assertEquals("client_credentials", body.getFirst("grant_type"));
        assertEquals("test-client-id", body.getFirst("client_id"));
        assertEquals("test-client-secret", body.getFirst("client_secret"));
        assertEquals("api://app/scope", body.getFirst("scope"));

        MediaType contentType = captor.getValue().getHeaders().getContentType();
        assertNotNull(contentType);
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, contentType);
    }

    @Test
    void request_token_throws_jwt_exception_when_response_body_is_null() {
        // GIVEN
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(JwtTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // WHEN / THEN
        JwtTokenException ex = assertThrows(JwtTokenException.class,
                () -> sut.requestToken("scope", "tenant"));
        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void request_token_throws_jwt_exception_when_access_token_is_null() {
        // GIVEN
        JwtTokenResponse response = new JwtTokenResponse();
        response.setAccessToken(null);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(JwtTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        // WHEN / THEN
        assertThrows(JwtTokenException.class, () -> sut.requestToken("scope", "tenant"));
    }

    @Test
    void request_token_throws_jwt_exception_on_rest_client_error() {
        // GIVEN
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(JwtTokenResponse.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // WHEN / THEN
        JwtTokenException ex = assertThrows(JwtTokenException.class,
                () -> sut.requestToken("scope", "tenant"));
        assertTrue(ex.getMessage().contains("Connection refused"));
    }
}

