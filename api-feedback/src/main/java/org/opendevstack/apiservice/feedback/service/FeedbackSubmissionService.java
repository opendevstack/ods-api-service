package org.opendevstack.apiservice.feedback.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.feedback.mapper.FeedbackSubmissionMapper;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionsMetadata;
import org.opendevstack.apiservice.feedback.model.GetFeedbackSubmissionsResponse;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.persistence.repository.FeedbackSubmissionRepository;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Handles persistence and retrieval of feedback submissions. */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackSubmissionService {

	private static final int DEFAULT_PAGE = 0;

	private static final int DEFAULT_SIZE = 20;

	private static final int MAX_SIZE = 100;

	private final FeedbackSubmissionRepository feedbackSubmissionRepository;

	private final ClientAppRepository clientAppRepository;

	private final ProjectRepository projectRepository;

	private final FeedbackSubmissionMapper mapper;

	private final ObjectMapper objectMapper;

	/**
	 * Persists a submission with metadata from its registered client application.
	 * @param request validated feedback submission data
	 * @param clientAppRegistrationId client application registration ID extracted from
	 * the caller's token
	 * @return the persisted submission as an API model
	 * @throws ResponseStatusException if the client application is not registered
	 */
	@Transactional
	public FeedbackSubmission createSubmission(FeedbackSubmissionRequest request, UUID clientAppRegistrationId) {
		ClientAppEntity clientApp = clientAppRepository.findByClientId(clientAppRegistrationId).orElseThrow(() -> {
			log.warn("Feedback submission rejected for unregistered clientAppRegistrationId={}",
					clientAppRegistrationId);
			return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client application is not registered");
		});
		if (!clientApp.isEnabled()) {
			log.warn("Feedback submission rejected for disabled clientAppRegistrationId={}", clientAppRegistrationId);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Client application is disabled");
		}

		FeedbackSubmissionEntity submission = mapper.toEntity(request, clientApp, clientAppRegistrationId,
				objectMapper);

		if (request.getProjectKey() != null) {
			submission.setProjectKey(request.getProjectKey());
			projectRepository.findByProjectKeyIgnoreCase(request.getProjectKey()).ifPresent(project -> {
				submission.setProjectId(project.getId());
				submission.setProjectKey(project.getProjectKey());
			});
		}

		FeedbackSubmissionEntity savedSubmission = feedbackSubmissionRepository.saveAndFlush(submission);
		log.info("Registered feedback submission id={} projectKey={}", savedSubmission.getId(),
				savedSubmission.getProjectKey());
		return mapper.toApiModel(savedSubmission, objectMapper);
	}

	/**
	 * Finds a page of submissions for a client application registration within optional
	 * inclusive timestamp bounds. Results are ordered from newest to oldest.
	 * @param clientAppRegistrationId client application registration ID
	 * @param startDate inclusive lower bound, or {@code null} for no lower bound
	 * @param endDate inclusive upper bound, or {@code null} for no upper bound
	 * @param page zero-based page number, or {@code null} for the first page
	 * @param size page size, or {@code null} for the default size
	 * @return matching submissions and pagination metadata
	 */
	@Transactional(readOnly = true)
	public GetFeedbackSubmissionsResponse getSubmissionsByClientAppRegistrationId(UUID clientAppRegistrationId,
			OffsetDateTime startDate, OffsetDateTime endDate, Integer page, Integer size) {
		int pageNumber = page == null ? DEFAULT_PAGE : page;
		int pageSize = size == null ? DEFAULT_SIZE : size;
		if (pageNumber < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be greater than or equal to 0.");
		}
		if (pageSize < 1 || pageSize > MAX_SIZE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be between 1 and 100.");
		}
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<FeedbackSubmissionEntity> entities;
		if (startDate != null && endDate != null) {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(clientAppRegistrationId,
						startDate, endDate, pageable);
		}
		else if (startDate != null) {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdAndSubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(
						clientAppRegistrationId, startDate, pageable);
		}
		else if (endDate != null) {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdAndSubmittedAtLessThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
						endDate, pageable);
		}
		else {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdOrderBySubmittedAtDesc(clientAppRegistrationId, pageable);
		}
		if (!(pageNumber == 0 && entities.getTotalPages() == 0) && pageNumber >= entities.getTotalPages()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested feedback page does not exist.");
		}
		List<FeedbackSubmission> submissions = entities.getContent().stream()
			.map(entity -> mapper.toApiModel(entity, objectMapper))
			.toList();
		FeedbackSubmissionsMetadata metadata = new FeedbackSubmissionsMetadata()
			.page(entities.getNumber())
			.size(entities.getSize())
			.totalElements((int) entities.getTotalElements())
			.totalPages(entities.getTotalPages())
			.last(entities.isLast());
		log.debug("Found {} of {} feedback submissions for clientAppRegistrationId={} on page {}", submissions.size(),
				entities.getTotalElements(), clientAppRegistrationId, pageNumber);
		return new GetFeedbackSubmissionsResponse().submissions(submissions).metadata(metadata);
	}

}