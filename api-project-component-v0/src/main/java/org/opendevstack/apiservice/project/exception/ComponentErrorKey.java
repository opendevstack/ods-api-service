package org.opendevstack.apiservice.project.exception;

public enum ComponentErrorKey {

    OK("000", "Success"),
    ACCESS_DENIED("002", "Forbidden"),
    INTERNAL_ERROR("003", "Internal error"),
    INVALID_PARAMETERS("006", badRequest()),
    COMPONENT_PARAM_NOT_MEET_REGEX("010", badRequest()),
    PROJECT_NOT_FOUND("012", notFound()),
    COMPONENT_NOT_FOUND("013", notFound()),
    BAD_REQUEST_BODY("014", badRequest()),
    COMPONENT_PARAM_INVALID_FORMAT("017", badRequest());

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

    private static String badRequest() {
        return "Bad Request";
    }

    private static String notFound() {
        return "Not Found";
    }
}
