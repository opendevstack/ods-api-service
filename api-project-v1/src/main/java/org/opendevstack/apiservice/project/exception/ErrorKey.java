package org.opendevstack.apiservice.project.exception;

public enum ErrorKey {

    INTERNAL_ERROR("003", ErrorMessage.INTERNAL_ERROR),
    BAD_REQUEST_BODY("014", ErrorMessage.BAD_REQUEST),
    INVALID_PAGE("031", ErrorMessage.INVALID_PAGE),
    INVALID_SIZE("032", ErrorMessage.INVALID_SIZE);

    private String key;
    private String message;

    ErrorKey(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return this.key;
    }

    public String getMessage() {
        return this.message;
    }
}
