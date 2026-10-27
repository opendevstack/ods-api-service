package org.opendevstack.apiservice.feedback.controller.advice;

import java.util.List;
import org.opendevstack.apiservice.feedback.model.RestErrorMessage;
import org.opendevstack.apiservice.feedback.controller.FeedbackSubmissionController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/** Converts exceptions from Feedback endpoints into API error responses. */
@RestControllerAdvice(assignableTypes = FeedbackSubmissionController.class)
@Slf4j
public class FeedbackSubmissionExceptionHandler {

	/** Returns the status and reason carried by a request rejection. */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<RestErrorMessage> handleResponseStatusException(ResponseStatusException exception) {
		String message = exception.getReason() == null ? "Request failed." : exception.getReason();
		log.warn("Feedback request rejected with status {}: {}", exception.getStatusCode().value(), message);
		return ResponseEntity.status(exception.getStatusCode()).body(new RestErrorMessage(message));
	}

	/** Returns a 400 response with the first Bean Validation field error. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RestErrorMessage> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException exception) {
		List<String> validationErrors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();
		String message = validationErrors.stream().findFirst().orElse("Invalid request body.");
		log.warn("Feedback request validation failed: {}", String.join("; ", validationErrors));
		return ResponseEntity.badRequest().body(new RestErrorMessage(message));
	}

	/** Returns a 400 response when the request body cannot be parsed as JSON. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<RestErrorMessage> handleHttpMessageNotReadableException(
			HttpMessageNotReadableException exception) {
		log.warn("Feedback request body could not be read as valid JSON.");
		return ResponseEntity.badRequest().body(new RestErrorMessage("Request body must be valid JSON."));
	}

	/** Returns a 400 response when a path or query parameter cannot be converted. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<RestErrorMessage> handleMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException exception) {
		String message = "Invalid value for parameter '" + exception.getName() + "'.";
		log.warn("Feedback request parameter validation failed: {}", message);
		return ResponseEntity.badRequest().body(new RestErrorMessage(message));
	}

	/** Returns a generic 500 response for unexpected Feedback endpoint failures. */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<RestErrorMessage> handleGenericException(Exception exception) {
		log.error("Unexpected error while processing feedback request", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new RestErrorMessage("An error occurred while processing the request."));
	}

}