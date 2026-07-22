package org.opendevstack.apiservice.projectv1.controller.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.projectv1.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.projectv1.controller.ProjectController;
import org.opendevstack.apiservice.projectv1.exception.ErrorKey;
import org.opendevstack.apiservice.projectv1.exception.PageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectExceptionHandlerTest {

    private ProjectExceptionHandler sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectExceptionHandler();
    }

    @Test
    void handle_constraint_violation_returns_bad_request_for_invalid_page() {
        ConstraintViolationException ex = createConstraintViolationException("page");

        ResponseEntity<GetProjectsResponse> result = sut.handleConstraintViolationException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_PAGE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.INVALID_PAGE.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_constraint_violation_returns_bad_request_for_invalid_size() {
        ConstraintViolationException ex = createConstraintViolationException("size");

        ResponseEntity<GetProjectsResponse> result = sut.handleConstraintViolationException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_SIZE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.INVALID_SIZE.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_constraint_violation_returns_bad_request_for_unknown_field() {
        ConstraintViolationException ex = createConstraintViolationException("unknownField");

        ResponseEntity<GetProjectsResponse> result = sut.handleConstraintViolationException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.BAD_REQUEST_BODY.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.BAD_REQUEST_BODY.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_method_argument_type_mismatching_returns_bad_request_for_invalid_page() {
        MethodArgumentTypeMismatchException ex = createMethodArgumentTypeMismatchException("page");

        ResponseEntity<GetProjectsResponse> result = sut.handleMethodArgumentTypeMismatchException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_PAGE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.INVALID_PAGE.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_method_argument_type_mismatching_returns_bad_request_for_invalid_size() {
        MethodArgumentTypeMismatchException ex = createMethodArgumentTypeMismatchException("size");

        ResponseEntity<GetProjectsResponse> result = sut.handleMethodArgumentTypeMismatchException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_SIZE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.INVALID_SIZE.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_method_argument_type_mismatching_returns_bad_request_for_unknown_field() {
        MethodArgumentTypeMismatchException ex = createMethodArgumentTypeMismatchException("unknownField");

        ResponseEntity<GetProjectsResponse> result = sut.handleMethodArgumentTypeMismatchException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.BAD_REQUEST_BODY.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.BAD_REQUEST_BODY.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_page_not_found_exception_returns_not_found() {
        PageNotFoundException ex = new PageNotFoundException(ErrorKey.PAGE_NOT_FOUND);

        ResponseEntity<GetProjectsResponse> result = sut.handlePageNotFoundException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.PAGE_NOT_FOUND.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorKey.PAGE_NOT_FOUND.getMessage());
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_generic_exception_returns_internal_server_error() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ResponseEntity<GetProjectsResponse> result = sut.handleGenericException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(ErrorKey.INTERNAL_ERROR.getMessage());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INTERNAL_ERROR.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo("An error occurred while processing the request.");
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    // ------- helpers -------

    private ConstraintViolationException createConstraintViolationException(String fieldName) {
        Path.Node node = mock(Path.Node.class);
        when(node.getName()).thenReturn(fieldName);

        Path path = mock(Path.class);
        when(path.iterator()).thenReturn(List.of(node).iterator());

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);

        return new ConstraintViolationException(Set.of(violation));
    }

    private MethodArgumentTypeMismatchException createMethodArgumentTypeMismatchException(String fieldName) {
        return new MethodArgumentTypeMismatchException(
                "invalid-value",
                Integer.class,
                fieldName,
                null,
                null
        );
    }
}
