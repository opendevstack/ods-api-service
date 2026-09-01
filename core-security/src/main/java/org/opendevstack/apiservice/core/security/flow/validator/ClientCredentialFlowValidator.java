package org.opendevstack.apiservice.core.security.flow.validator;

import org.opendevstack.apiservice.core.contracts.auth.AuthType;
import org.opendevstack.apiservice.core.security.client.credentials.ClientCredentialsTokenProperties;
import org.opendevstack.apiservice.core.security.flow.AuthFlowValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class ClientCredentialFlowValidator implements AuthFlowValidator {

    private final ClientCredentialsTokenProperties clientCredentialsTokenProperties;

    public ClientCredentialFlowValidator(ClientCredentialsTokenProperties clientCredentialsTokenProperties) {
        this.clientCredentialsTokenProperties = clientCredentialsTokenProperties;
    }

    @Override
    public AuthType getSupportedFlow() {
        return AuthType.CLIENT_CREDENTIALS;
    }

    @Override
    public boolean validate(Jwt jwt) {
        // v1 tokens use "appid", v2 tokens use "azp"; either is acceptable
        String appid = jwt.getClaimAsString("appid");
        String azp = jwt.getClaimAsString("azp");
        if ((appid == null || appid.isBlank()) && (azp == null || azp.isBlank())) {
            return false;
        }

        Object aud = jwt.getClaim("aud");
        if (aud == null) {
            return false;
        }

        // The audience must contain the configured client-id (app.security.client-credentials.client-id)
        String configuredClientId = clientCredentialsTokenProperties.getClientId();
        if (configuredClientId == null || configuredClientId.isBlank() || !aud.toString().contains(configuredClientId)) {
            return false;
        }

        String scp = jwt.getClaimAsString("scp");
        if (scp != null && !scp.isBlank()) {
            return false;
        }

        String upn = jwt.getClaimAsString("upn");
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if ((upn != null && !upn.isBlank()) || (preferredUsername != null && !preferredUsername.isBlank())) {
            return false;
        }

        String sub = jwt.getSubject();
        String oid = jwt.getClaimAsString("oid");
        return sub != null && oid != null && sub.equals(oid);
    }
}
