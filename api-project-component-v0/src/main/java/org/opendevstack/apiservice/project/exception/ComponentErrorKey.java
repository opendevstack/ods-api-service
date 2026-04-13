package org.opendevstack.apiservice.project.exception;

public enum ComponentErrorKey {

    OK("000", "Success"),
    ACCESS_DENIED("002", "Forbidden"),
    INTERNAL_ERROR("003", "Internal error"),
    INVALID_PARAMETERS("006", "Bad Request"),
    COMPONENT_PARAM_NOT_MEET_REGEX("010", "Bad Request"),
    PROJECT_NOT_FOUND("012", "Not Found"),
    COMPONENT_NOT_FOUND("013", "Not Found"),
    BAD_REQUEST_BODY("014", "Bad Request"),
    COMPONENT_PARAM_INVALID_FORMAT("017", "Bad Request");

    private final String key;
    private final String message;

    ComponentErrorKey(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }
}
