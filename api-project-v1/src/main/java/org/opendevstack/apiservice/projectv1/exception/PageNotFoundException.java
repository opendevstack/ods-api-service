package org.opendevstack.apiservice.projectv1.exception;

import lombok.Getter;

@Getter
public class PageNotFoundException extends RuntimeException {

    private final ErrorKey errorKey;

    public PageNotFoundException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.errorKey = errorKey;
    }

    public PageNotFoundException(ErrorKey errorKey, String additionalMessage) {
        super(errorKey.getMessage() + " " + additionalMessage);
        this.errorKey = errorKey;
    }
}
