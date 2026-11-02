package org.opendevstack.apiservice.feedback.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.openapitools.jackson.nullable.JsonNullable;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackSubmissionMapperTest {

	private final FeedbackSubmissionMapper mapper = Mappers.getMapper(FeedbackSubmissionMapper.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void mapsRequestToEntityIncludingClientMetadataAndJsonFields() {
		UUID internalClientId = UUID.randomUUID();
		UUID clientAppRegistrationId = UUID.randomUUID();
		List<Map<String, Object>> questions = List.of(Map.of("id", "rating", "options", List.of("good", "bad")));
		Map<String, Object> answers = Map.of("rating", Map.of("value", "good"));
		ClientAppEntity clientApp = ClientAppEntity.builder()
			.id(internalClientId)
			.clientId(clientAppRegistrationId)
			.clientName("Feedback client")
			.build();
		FeedbackSubmissionRequest request = new FeedbackSubmissionRequest().projectKey("PROJECT")
			.submitter("submitter@example.org")
			.questions(questions)
			.answers(answers);

		FeedbackSubmissionEntity entity = mapper.toEntity(request, clientApp, clientAppRegistrationId, objectMapper);

		assertThat(entity.getClientId()).isEqualTo(internalClientId);
		assertThat(entity.getClientAppRegistrationId()).isEqualTo(clientAppRegistrationId);
		assertThat(entity.getClientName()).isEqualTo("Feedback client");
		assertThat(entity.getProjectKey()).isEqualTo("PROJECT");
		assertThat(entity.getSubmitter()).isEqualTo("submitter@example.org");
		assertThat(entity.getQuestions()).isEqualTo(objectMapper.valueToTree(questions));
		assertThat(entity.getAnswers()).isEqualTo(objectMapper.valueToTree(answers));
		assertThat(entity.getId()).isNull();
		assertThat(entity.getProjectId()).isNull();
		assertThat(entity.getSubmittedAt()).isNull();
	}

	@Test
	void mapsEntityToApiModelIncludingJsonFieldsAndNullableProjectKey() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime submittedAt = OffsetDateTime.parse("2025-02-01T12:30:00Z");
		List<Map<String, Object>> questions = List.of(Map.of("id", "rating", "options", List.of("good", "bad")));
		Map<String, Object> answers = Map.of("rating", Map.of("value", "good"));
		FeedbackSubmissionEntity entity = new FeedbackSubmissionEntity();
		entity.setClientAppRegistrationId(clientAppRegistrationId);
		entity.setClientName("Feedback client");
		entity.setProjectKey("PROJECT");
		entity.setSubmitter("submitter@example.org");
		entity.setSubmittedAt(submittedAt);
		entity.setQuestions(objectMapper.valueToTree(questions));
		entity.setAnswers(objectMapper.valueToTree(answers));

		FeedbackSubmission submission = mapper.toApiModel(entity, objectMapper);

		assertThat(submission.getClientAppRegistrationId()).isEqualTo(clientAppRegistrationId);
		assertThat(submission.getClientName()).isEqualTo("Feedback client");
		assertThat(submission.getProjectKey()).isEqualTo(JsonNullable.of("PROJECT"));
		assertThat(submission.getSubmitter()).isEqualTo("submitter@example.org");
		assertThat(submission.getSubmittedAt()).isEqualTo(submittedAt);
		assertThat(submission.getQuestions()).isEqualTo(questions);
		assertThat(submission.getAnswers()).isEqualTo(answers);
	}

}