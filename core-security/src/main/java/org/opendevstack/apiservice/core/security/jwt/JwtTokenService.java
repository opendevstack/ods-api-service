package org.opendevstack.apiservice.core.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class JwtTokenService {

    private static final String GRANT_TYPE = "client_credentials";

    private final JwtTokenProperties properties;
    private final RestTemplate restTemplate;

    @Autowired
    public JwtTokenService(JwtTokenProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * Requests an access token using the given JWT assertion.
     *
     * @param scope the target API scope (e.g. {@code api://<app-id>/Api.Access})
     *
     * @return the access token string
     * @throws JwtTokenException if the token request fails
     */
    public String requestToken(String scope, String tenantId) {
        log.debug("Requesting access token using JWT assertion");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("scope", scope);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            String urlWithTenant = properties.getTokenUrl().replace("<app-tenant-id>", tenantId);

            ResponseEntity<JwtTokenResponse> response =
                    restTemplate.postForEntity(urlWithTenant, request, JwtTokenResponse.class);

            if (response.getBody() == null || response.getBody().getAccessToken() == null) {
                throw new JwtTokenException("JWT token response body or access_token is null");
            }

            log.debug("JWT token obtained successfully, expires in {} seconds", response.getBody().getExpiresIn());
            return response.getBody().getAccessToken();
        } catch (RestClientException e) {
            throw new JwtTokenException("Failed to exchange JWT for JWT token: " + e.getMessage(), e);
        }
    }
}
