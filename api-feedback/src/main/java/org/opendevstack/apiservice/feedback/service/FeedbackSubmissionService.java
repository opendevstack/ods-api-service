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
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.persistence.repository.FeedbackSubmissionRepository;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Handles persistence and retrieval of feedback submissions. */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackSubmissionService {

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
	 * Finds submissions for a client application registration within optional inclusive
	 * timestamp bounds. Results are ordered from newest to oldest.
	 * @param clientAppRegistrationId client application registration ID
	 * @param startDate inclusive lower bound, or {@code null} for no lower bound
	 * @param endDate inclusive upper bound, or {@code null} for no upper bound
	 * @return matching submissions as API models
	 */
	@Transactional(readOnly = true)
	public List<FeedbackSubmission> getSubmissionsByClientAppRegistrationId(UUID clientAppRegistrationId,
			OffsetDateTime startDate, OffsetDateTime endDate) {
		List<FeedbackSubmissionEntity> entities;
		if (startDate != null && endDate != null) {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(clientAppRegistrationId,
						startDate, endDate);
		}
		else if (startDate != null) {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdAndSubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(
						clientAppRegistrationId, startDate);
		}
		else if (endDate != null) {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdAndSubmittedAtLessThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
						endDate);
		}
		else {
			entities = feedbackSubmissionRepository
				.findByClientAppRegistrationIdOrderBySubmittedAtDesc(clientAppRegistrationId);
		}
		List<FeedbackSubmission> submissions = entities.stream()
			.map(entity -> mapper.toApiModel(entity, objectMapper))
			.toList();
		log.debug("Found {} feedback submissions for clientAppRegistrationId={} between {} and {}", submissions.size(),
				clientAppRegistrationId, startDate, endDate);
		return submissions;
	}

}