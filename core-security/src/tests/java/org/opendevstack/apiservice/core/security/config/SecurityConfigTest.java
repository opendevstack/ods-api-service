package org.opendevstack.apiservice.core.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.security.authorization.PolicyAuthorizationManager;
import org.opendevstack.apiservice.core.security.jwt.AzureJwtAuthenticationConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityProperties securityProperties;
    private PolicyAuthorizationManager policyAuthorizationManager;
    private AzureJwtAuthenticationConverter azureJwtAuthenticationConverter;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityProperties = mock(SecurityProperties.class);
        policyAuthorizationManager = mock(PolicyAuthorizationManager.class);
        azureJwtAuthenticationConverter = new AzureJwtAuthenticationConverter();
        securityConfig = new SecurityConfig(securityProperties, policyAuthorizationManager, azureJwtAuthenticationConverter);
    }

    @Test
    void securityFilterChain_withPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(new String[]{"/health", "/info"});
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }

    @Test
    void securityFilterChain_withNullPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(null);
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }

    @Test
    void securityFilterChain_withEmptyPublicEndpoints() throws Exception {
        when(securityProperties.getPublicEndpoints()).thenReturn(new String[]{});
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        SecurityFilterChain chain = securityConfig.securityFilterChain(http);
        assertNotNull(chain);
    }
}
