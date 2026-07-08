package org.opendevstack.apiservice.projectv1.controller.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
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
import java.util.Optional;

@Component("projectExceptionHandlerV1")
@RestControllerAdvice(assignableTypes = ProjectController.class)
@Slf4j
public class ProjectExceptionHandler {

    private static final Map<String, ErrorKey> FIELD_ERROR_MAP = Map.of(
            "page", ErrorKey.INVALID_PAGE,
            "size", ErrorKey.INVALID_SIZE
    );

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GetProjectsResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("Request validation error: {}", ex.getMessage());

        Optional<ErrorKey> error = ex.getConstraintViolations()
                .stream()
                .map(this::formatConstraintViolation)
                .findFirst();

        GetProjectsResponse response = new GetProjectsResponse();
        response.setLocation(ProjectController.API_BASE_PATH);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        if (error.isPresent()) {
            ErrorKey key = error.get();
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

    private ErrorKey formatConstraintViolation(ConstraintViolation<?> violation) {
        Path path = violation.getPropertyPath();

        String lastNodeName = null;
        for (Path.Node node : path) {
            lastNodeName = node.getName();
        }

        return FIELD_ERROR_MAP.getOrDefault(lastNodeName, ErrorKey.BAD_REQUEST_BODY);
    }
}