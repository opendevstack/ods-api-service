package org.opendevstack.apiservice.project.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.UUID;

public class SecurityUtils {

    private SecurityUtils() {
        // to avoid instantiation
    }

    public static UUID getClientId() {
        String appId = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Jwt) {
            appId = ((Jwt) principal).getClaimAsString("appid");
        } else {
            throw new InvalidBearerTokenException("Invalid authentication token: " + principal.getClass().getName());
        }

        return UUID.fromString(appId);
    }
}
