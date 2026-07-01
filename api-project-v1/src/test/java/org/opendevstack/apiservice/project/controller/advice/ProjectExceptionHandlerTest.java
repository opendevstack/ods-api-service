package org.opendevstack.apiservice.project.controller.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.project.client.model.GetProjectsResponse;
import org.opendevstack.apiservice.project.controller.ProjectController;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
    void handle_method_argument_not_valid_returns_bad_request_for_invalid_page() {
        MethodArgumentNotValidException ex = createValidationException("page", "must be >= 0", 0);

        ResponseEntity<GetProjectsResponse> result = sut.handleMethodArgumentNotValidException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_PAGE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorMessage.INVALID_PAGE);
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_method_argument_not_valid_returns_bad_request_for_invalid_size() {
        MethodArgumentNotValidException ex = createValidationException("size", "must be >= 20", 0);

        ResponseEntity<GetProjectsResponse> result = sut.handleMethodArgumentNotValidException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getError()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.INVALID_SIZE.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorMessage.INVALID_SIZE);
        assertThat(result.getBody().getLocation()).isEqualTo(ProjectController.API_BASE_PATH);
    }

    @Test
    void handle_method_argument_not_valid_returns_bad_request_for_unknown_field() {
        MethodArgumentNotValidException ex = createValidationException("unknownField", "invalid value", "bad");

        ResponseEntity<GetProjectsResponse> result = sut.handleMethodArgumentNotValidException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getErrorKey()).isEqualTo(ErrorKey.BAD_REQUEST_BODY.getKey());
        assertThat(result.getBody().getMessage()).isEqualTo(ErrorMessage.BAD_REQUEST);
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

    private MethodArgumentNotValidException createValidationException(String field, String defaultMessage, Object rejectedValue) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", field, rejectedValue, false, null, null, defaultMessage));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        return ex;
    }
}
