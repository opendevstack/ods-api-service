package org.opendevstack.apiservice.feedback.controller.advice;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.feedback.model.RestErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

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

}