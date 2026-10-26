package org.opendevstack.apiservice.feedback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.core.security.jwt.JwtUtils;
import org.opendevstack.apiservice.feedback.api.FeedbackSubmissionsApi;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.feedback.service.FeedbackSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** REST endpoints for creating and querying feedback submissions. */
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackSubmissionController implements FeedbackSubmissionsApi {

	private final FeedbackSubmissionService feedbackSubmissionService;

	/**
	 * Creates a submission and associates it with the client application registration ID
	 * from the caller's JWT.
	 */
	@Override
	public ResponseEntity<FeedbackSubmission> registerFeedbackSubmission(
			@Valid FeedbackSubmissionRequest feedbackSubmissionRequest) {
		FeedbackSubmission submission = feedbackSubmissionService.createSubmission(feedbackSubmissionRequest,
				JwtUtils.getClientId());
		return ResponseEntity.status(HttpStatus.CREATED).body(submission);
	}

	/**
	 * Lists submissions for a client, optionally bounded by inclusive timestamps.
	 * @param clientAppRegistrationId client application registration ID to filter by
	 * @param startDate inclusive lower bound for submission time, or {@code null}
	 * @param endDate inclusive upper bound for submission time, or {@code null}
	 * @return matching submissions, newest first
	 */
	@Override
	public ResponseEntity<List<FeedbackSubmission>> getFeedbackSubmissionsByClientAppRegistrationId(
			UUID clientAppRegistrationId, OffsetDateTime startDate, OffsetDateTime endDate) {
		List<FeedbackSubmission> submissions = feedbackSubmissionService
			.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, startDate, endDate);
		ResponseEntity<List<FeedbackSubmission>> response = ResponseEntity.ok(submissions);
		return response;
	}

}