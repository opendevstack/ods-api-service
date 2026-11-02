package org.opendevstack.apiservice.feedback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.core.security.jwt.JwtUtils;
import org.opendevstack.apiservice.feedback.api.FeedbackSubmissionsApi;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.feedback.model.GetFeedbackSubmissionsResponse;
import org.opendevstack.apiservice.feedback.service.FeedbackSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

/** REST endpoints for creating and querying feedback submissions. */
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackSubmissionController implements FeedbackSubmissionsApi {

	private static final int DEFAULT_PAGE = 0;

	private static final int DEFAULT_SIZE = 20;

	private static final int MAX_SIZE = 100;

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
	 * Lists a page of submissions across clients, optionally bounded by inclusive timestamps.
	 * @param startDate inclusive lower bound for submission time, or {@code null}
	 * @param endDate inclusive upper bound for submission time, or {@code null}
	 * @param page zero-based page number
	 * @param size maximum number of submissions per page
	 * @return matching submissions and pagination metadata
	 */
	@Override
	public ResponseEntity<GetFeedbackSubmissionsResponse> getFeedbackSubmissions(
			OffsetDateTime startDate, OffsetDateTime endDate, Integer page, Integer size) {
		validateDateRange(startDate, endDate);
		int validPage = validatePage(page);
		int validSize = validateSize(size);
		GetFeedbackSubmissionsResponse submissions = feedbackSubmissionService.getFeedbackSubmissions(startDate,
				endDate, validPage, validSize);
		return ResponseEntity.ok(submissions);
	}

	private void validateDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"startDate must be before or equal to endDate.");
		}
	}

	private int validatePage(Integer page) {
		int pageNumber = page == null ? DEFAULT_PAGE : page;
		if (pageNumber < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be greater than or equal to 0.");
		}
		return pageNumber;
	}

	private int validateSize(Integer size) {
		int pageSize = size == null ? DEFAULT_SIZE : size;
		if (pageSize < 1 || pageSize > MAX_SIZE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be between 1 and 100.");
		}
		return pageSize;
	}

}