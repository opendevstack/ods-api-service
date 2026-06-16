package org.opendevstack.apiservice.core.security.jwt;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class JwtUtils {

    private JwtUtils() {
    }

    /**
     * Extracts the raw JWT token value from the current SecurityContext.
     *
     * @return the JWT token string
     * @throws InvalidBearerTokenException if the principal is not a JWT
     */
    public static String getTokenValue() {
        Object principal = currentPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }
        throw new InvalidBearerTokenException("Invalid authentication token");
    }

    private static Object currentPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new InvalidBearerTokenException("No authentication present in security context");
        }
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new InvalidBearerTokenException("No principal present in security context");
        }
        return principal;
    }

    /**
     * Extracts the Azure AD client ID from the current SecurityContext JWT.
     * Checks the {@code azp} claim first, then falls back to {@code appid}.
     *
     * @return the client ID as UUID
     * @throws InvalidBearerTokenException if the principal is not a JWT or no client ID claim is found
     */
    public static UUID getClientId() {
        Object principal = currentPrincipal();
        if (principal instanceof Jwt jwt) {
            return extractClientId(jwt);
        }
        throw new InvalidBearerTokenException("Invalid authentication token");
    }

    /**
     * Extracts the Azure AD client ID from the given JWT.
     */
    public static UUID extractClientId(Jwt jwt) {
        String clientId = jwt.getClaimAsString("azp");
        if (clientId == null || clientId.isBlank()) {
            clientId = jwt.getClaimAsString("appid");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new InvalidBearerTokenException("Client ID not found in token claims");
        }
        return UUID.fromString(clientId);
    }

    public static List<String> extractAudiences(Jwt jwt) {
        Object audClaim = jwt.getClaims().get("aud");
        List<String> audiences = new ArrayList<>();

        if (audClaim instanceof String audString) {
            for (String audiencePart : audString.trim().split("\\s+")) {
                if (!audiencePart.isBlank()) {
                    audiences.add(audiencePart);
                }
            }
        } else if (audClaim instanceof List<?> audList) {
            for (Object value : audList) {
                if (value != null) {
                    String audience = value.toString().trim();
                    if (!audience.isBlank()) {
                        audiences.add(audience);
                    }
                }
            }
        }

        if (audiences.isEmpty()) {
            throw new InvalidBearerTokenException("Audience not found in token claims");
        }
        return audiences;
    }


    public static List<String> getAudiences() {
        Object principal = currentPrincipal();
        if (principal instanceof Jwt jwt) {
            return extractAudiences(jwt);
        }
        throw new InvalidBearerTokenException("Invalid authentication token");
    }

    public static boolean tokenMatchesScopeAudience(String oboScope) {
        Set<String> tokenAudiences = new LinkedHashSet<>(getAudiences());
        Set<String> scopeAudienceCandidates = extractAudienceCandidatesFromScope(oboScope);
        return tokenAudiences.stream().anyMatch(scopeAudienceCandidates::contains);
    }

    private static Set<String> extractAudienceCandidatesFromScope(String oboScope) {
        if (oboScope == null || oboScope.isBlank()) {
            return Set.of();
        }

        Set<String> candidates = new LinkedHashSet<>();
        String[] scopes = oboScope.trim().split("\\s+");

        for (String scope : scopes) {
            if (scope.isBlank()) {
                continue;
            }
            candidates.add(scope);

            int lastSlash = scope.lastIndexOf('/');
            if (lastSlash > 0) {
                candidates.add(scope.substring(0, lastSlash));
            }

            if (scope.startsWith("api://")) {
                String scopeWithoutPrefix = scope.substring("api://".length());
                int firstSlash = scopeWithoutPrefix.indexOf('/');
                if (firstSlash > 0) {
                    candidates.add(scopeWithoutPrefix.substring(0, firstSlash));
                } else {
                    candidates.add(scopeWithoutPrefix);
                }
            }
        }

        candidates.removeIf(Objects::isNull);
        return candidates;
    }
}
