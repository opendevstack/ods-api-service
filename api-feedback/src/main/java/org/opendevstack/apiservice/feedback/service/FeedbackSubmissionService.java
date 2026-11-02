package org.opendevstack.apiservice.feedback.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.persistence.repository.FeedbackSubmissionRepository;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
		Optional<ProjectEntity> project = projectRepository.findByProjectKeyIgnoreCase(request.getProjectKey());

		FeedbackSubmissionEntity submission = mapper.toEntity(request, project, clientApp, objectMapper);

		FeedbackSubmissionEntity savedSubmission = feedbackSubmissionRepository.saveAndFlush(submission);
		log.info("Registered feedback submission id={} projectKey={}", savedSubmission.getId(),
				savedSubmission.getProjectKey());
		return mapper.toApiModel(savedSubmission, objectMapper);
	}

	/**
	 * Finds a page of submissions across client applications within optional inclusive
	 * timestamp bounds. Results are ordered from newest to oldest.
	 * @param startDate inclusive lower bound, or {@code null} for no lower bound
	 * @param endDate inclusive upper bound, or {@code null} for no upper bound
	 * @param page validated zero-based page number
	 * @param size validated page size
	 * @return matching submissions and pagination metadata
	 */
	@Transactional(readOnly = true)
	public GetFeedbackSubmissionsResponse getFeedbackSubmissions(OffsetDateTime startDate, OffsetDateTime endDate,
			int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
		Page<FeedbackSubmissionEntity> entities = feedbackSubmissionRepository
			.findBySubmittedAtRange(startDate, endDate, pageable);
		if (!(page == 0 && entities.getTotalPages() == 0) && page >= entities.getTotalPages()) {
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
		log.debug("Found {} of {} feedback submissions on page {}", submissions.size(), entities.getTotalElements(),
				page);
		return new GetFeedbackSubmissionsResponse().submissions(submissions).metadata(metadata);
	}

}