package org.opendevstack.apiservice.projectusers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invalid token is provided.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTokenException extends ProjectUserException {
    public InvalidTokenException(String message) {
        super(message, ErrorCodes.INVALID_TOKEN);
    }
}
