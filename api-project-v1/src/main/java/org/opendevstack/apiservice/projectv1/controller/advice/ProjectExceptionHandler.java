package org.opendevstack.apiservice.projectv1.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.controller.ProjectController;
import org.opendevstack.apiservice.projectv1.exception.ErrorKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Component("projectExceptionHandlerV1")
@RestControllerAdvice(assignableTypes = ProjectController.class)
@Slf4j
public class ProjectExceptionHandler {

    private static final Map<String, ErrorKey> FIELD_ERROR_MAP = Map.of(
            "page", ErrorKey.INVALID_PAGE,
            "size", ErrorKey.INVALID_SIZE
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GetProjectsResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        log.warn("Request body validation error: {}", ex.getMessage());

        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);

        GetProjectsResponse response = new GetProjectsResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        if (fieldError != null) {
            String field = fieldError.getField();

            ErrorKey key = FIELD_ERROR_MAP.getOrDefault(field, ErrorKey.BAD_REQUEST_BODY);

            response.setErrorKey(key.getKey());
            response.setMessage(key.getMessage());
        } else {
            response.setErrorKey(ErrorKey.BAD_REQUEST_BODY.getKey());
            response.setMessage(ErrorKey.BAD_REQUEST_BODY.getMessage());
        }

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GetProjectsResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        GetProjectsResponse response = new GetProjectsResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(ErrorKey.INTERNAL_ERROR.getMessage());
        response.setErrorKey(ErrorKey.INTERNAL_ERROR.getKey());
        response.setMessage("An error occurred while processing the request.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}