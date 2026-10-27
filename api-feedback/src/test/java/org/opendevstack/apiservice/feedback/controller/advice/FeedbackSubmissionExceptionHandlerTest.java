package org.opendevstack.apiservice.feedback.controller.advice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.feedback.model.RestErrorMessage;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class FeedbackSubmissionExceptionHandlerTest {

	@Test
	void responseStatusExceptionReturnsItsStatusAndMessage() {
		FeedbackSubmissionExceptionHandler handler = new FeedbackSubmissionExceptionHandler();
		ResponseStatusException exception = new ResponseStatusException(HttpStatus.UNAUTHORIZED,
				"Client application is not registered");

		ResponseEntity<RestErrorMessage> response = handler.handleResponseStatusException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody().getMessage()).isEqualTo("Client application is not registered");
	}

	@Test
	void unexpectedExceptionFallsBackToInternalServerError() {
		FeedbackSubmissionExceptionHandler handler = new FeedbackSubmissionExceptionHandler();

		ResponseEntity<RestErrorMessage> response = handler.handleGenericException(new IllegalStateException("boom"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().getMessage()).isEqualTo("An error occurred while processing the request.");
	}

	@Test
	void validationLogDoesNotContainRejectedValue(CapturedOutput output) {
		String rejectedEmail = "private@example.org";
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
		bindingResult.addError(new FieldError("request", "submitter", rejectedEmail, false, new String[] { "Email" },
				null, "must be a well-formed email address"));
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getBindingResult()).thenReturn(bindingResult);

		new FeedbackSubmissionExceptionHandler().handleMethodArgumentNotValidException(exception);

		String logs = output.getOut() + output.getErr();
		assertThat(logs)
			.contains("submitter: must be a well-formed email address")
			.doesNotContain(rejectedEmail);
	}

	@Test
	void constrainedParameterReturnsBadRequestWithoutLoggingValue(CapturedOutput output) {
		ConstraintViolation<?> violation = mock(ConstraintViolation.class);
		Path path = mock(Path.class);
		Path.Node parameter = mock(Path.Node.class);
		when(path.iterator()).thenReturn(List.of(parameter).iterator());
		when(parameter.getName()).thenReturn("size");
		when(violation.getPropertyPath()).thenReturn(path);
		when(violation.getMessage()).thenReturn("must be greater than or equal to 1");
		ConstraintViolationException exception = new ConstraintViolationException("Rejected value 0", Set.of(violation));

		ResponseEntity<RestErrorMessage> response = new FeedbackSubmissionExceptionHandler()
			.handleConstraintViolationException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().getMessage()).isEqualTo("size: must be greater than or equal to 1");
		assertThat(output.getOut() + output.getErr()).doesNotContain("Rejected value 0");
	}

}