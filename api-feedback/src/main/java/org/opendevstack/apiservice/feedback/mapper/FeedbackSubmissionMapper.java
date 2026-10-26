package org.opendevstack.apiservice.feedback.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

/** Maps persisted feedback submissions to their public API representation. */
@Component
public class FeedbackSubmissionMapper {

	private static final TypeReference<List<Map<String, Object>>> QUESTIONS_TYPE = new TypeReference<>() {
	};

	private static final TypeReference<Map<String, Object>> ANSWERS_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;

	public FeedbackSubmissionMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Converts a persisted submission to an API response, including its JSON questions
	 * and answers. The persistence identifier is intentionally not exposed in the
	 * response.
	 * @param entity persisted feedback submission
	 * @return API representation of the submission
	 */
	public FeedbackSubmission toApiModel(FeedbackSubmissionEntity entity) {
		FeedbackSubmission submission = new FeedbackSubmission();
		submission.setClientAppRegistrationId(entity.getClientAppRegistrationId());
		submission.setClientName(entity.getClientName());
		submission.setProjectKey(JsonNullable.of(entity.getProjectKey()));
		submission.setSubmitter(entity.getSubmitter());
		submission.setSubmittedAt(entity.getSubmittedAt());
		submission.setQuestions(objectMapper.convertValue(entity.getQuestions(), QUESTIONS_TYPE));
		submission.setAnswers(objectMapper.convertValue(entity.getAnswers(), ANSWERS_TYPE));
		return submission;
	}

}