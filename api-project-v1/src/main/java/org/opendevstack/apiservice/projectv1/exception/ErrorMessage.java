package org.opendevstack.apiservice.projectv1.exception;

public class ErrorMessage {

    public static final String BAD_REQUEST = "Bad Request";
    public static final String INTERNAL_ERROR = "Internal error";

    public static final String INVALID_PAGE = "page must be an integer greater or equal to 0";
    public static final String INVALID_SIZE = "size must be an integer between 1 and 1000";
    public static final String PAGE_NOT_FOUND = "page not found";
    
    private ErrorMessage() {
        // prevent instantiation
    }
}
