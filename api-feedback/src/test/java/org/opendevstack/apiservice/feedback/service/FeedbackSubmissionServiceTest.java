package org.opendevstack.apiservice.feedback.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendevstack.apiservice.feedback.mapper.FeedbackSubmissionMapper;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.persistence.repository.FeedbackSubmissionRepository;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class FeedbackSubmissionServiceTest {

	@Mock
	private FeedbackSubmissionRepository feedbackSubmissionRepository;

	@Mock
	private ClientAppRepository clientAppRepository;

	@Mock
	private ProjectRepository projectRepository;

	private FeedbackSubmissionService service;

	@BeforeEach
	void setUp() {
		service = new FeedbackSubmissionService(feedbackSubmissionRepository, clientAppRepository, projectRepository,
				new FeedbackSubmissionMapper(new ObjectMapper()), new ObjectMapper());
	}

	@Test
	void createSubmissionWithoutProjectPersistsJsonAndClientMetadata() {
		UUID registrationId = UUID.randomUUID();
		UUID internalClientId = UUID.randomUUID();
		ClientAppEntity clientApp = ClientAppEntity.builder()
			.id(internalClientId)
			.clientId(registrationId)
			.clientName("Feedback client")
			.build();
		FeedbackSubmissionRequest request = new FeedbackSubmissionRequest().submitter("submitter@example.org")
			.questions(List.of(Map.of("id", "q1", "options", List.of("yes", "no"))))
			.answers(Map.of("q1", Map.of("value", "yes")));
		when(clientAppRepository.findByClientId(registrationId)).thenReturn(Optional.of(clientApp));
		stubSaveAndFlush();

		FeedbackSubmission response = service.createSubmission(request, registrationId);

		ArgumentCaptor<FeedbackSubmissionEntity> captor = ArgumentCaptor.forClass(FeedbackSubmissionEntity.class);
		verify(feedbackSubmissionRepository).saveAndFlush(captor.capture());
		FeedbackSubmissionEntity saved = captor.getValue();
		assertThat(saved.getClientId()).isEqualTo(internalClientId);
		assertThat(saved.getClientAppRegistrationId()).isEqualTo(registrationId);
		assertThat(saved.getClientName()).isEqualTo("Feedback client");
		assertThat(saved.getSubmitter()).isEqualTo("submitter@example.org");
		assertThat(response.getSubmitter()).isEqualTo("submitter@example.org");
		assertThat(saved.getProjectId()).isNull();
		assertThat(saved.getProjectKey()).isNull();
		assertThat(saved.getQuestions().get(0).get("options").get(0).asText()).isEqualTo("yes");
		assertThat(saved.getAnswers().get("q1").get("value").asText()).isEqualTo("yes");
		verifyNoInteractions(projectRepository);
	}

	@Test
	void createSubmissionWithProjectStoresProjectReference() {
		UUID registrationId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();
		ClientAppEntity clientApp = ClientAppEntity.builder()
			.id(UUID.randomUUID())
			.clientId(registrationId)
			.clientName("Feedback client")
			.build();
		ProjectEntity project = new ProjectEntity();
		project.setId(projectId);
		project.setProjectKey("PROJECT");
		FeedbackSubmissionRequest request = new FeedbackSubmissionRequest().projectKey("project")
			.submitter("submitter@example.org")
			.questions(List.of(Map.of("id", "q1")))
			.answers(Map.of("q1", "yes"));
		when(clientAppRepository.findByClientId(registrationId)).thenReturn(Optional.of(clientApp));
		when(projectRepository.findByProjectKeyIgnoreCase("project")).thenReturn(Optional.of(project));
		stubSaveAndFlush();

		service.createSubmission(request, registrationId);

		ArgumentCaptor<FeedbackSubmissionEntity> captor = ArgumentCaptor.forClass(FeedbackSubmissionEntity.class);
		verify(feedbackSubmissionRepository).saveAndFlush(captor.capture());
		assertThat(captor.getValue().getProjectId()).isEqualTo(projectId);
		assertThat(captor.getValue().getProjectKey()).isEqualTo("PROJECT");
	}

	@Test
	void createSubmissionWithUnknownProjectKeepsProjectKeyWithoutProjectReference() {
		UUID registrationId = UUID.randomUUID();
		ClientAppEntity clientApp = ClientAppEntity.builder()
			.id(UUID.randomUUID())
			.clientId(registrationId)
			.clientName("Feedback client")
			.build();
		FeedbackSubmissionRequest request = new FeedbackSubmissionRequest().projectKey("UNREGISTERED")
			.submitter("submitter@example.org")
			.questions(List.of(Map.of("id", "q1")))
			.answers(Map.of("q1", "yes"));
		when(clientAppRepository.findByClientId(registrationId)).thenReturn(Optional.of(clientApp));
		when(projectRepository.findByProjectKeyIgnoreCase("UNREGISTERED")).thenReturn(Optional.empty());
		stubSaveAndFlush();

		service.createSubmission(request, registrationId);

		ArgumentCaptor<FeedbackSubmissionEntity> captor = ArgumentCaptor.forClass(FeedbackSubmissionEntity.class);
		verify(feedbackSubmissionRepository).saveAndFlush(captor.capture());
		assertThat(captor.getValue().getProjectId()).isNull();
		assertThat(captor.getValue().getProjectKey()).isEqualTo("UNREGISTERED");
	}

	@Test
	void createSubmissionWithUnregisteredClientReturnsUnauthorized() {
		UUID registrationId = UUID.randomUUID();
		when(clientAppRepository.findByClientId(registrationId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.createSubmission(new FeedbackSubmissionRequest(), registrationId))
			.isInstanceOf(ResponseStatusException.class)
			.hasMessageContaining("Client application is not registered")
			.satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED));
		verifyNoInteractions(feedbackSubmissionRepository, projectRepository);
	}

	@Test
	void createSubmissionWithDisabledClientReturnsForbidden() {
		UUID registrationId = UUID.randomUUID();
		ClientAppEntity clientApp = ClientAppEntity.builder()
			.id(UUID.randomUUID())
			.clientId(registrationId)
			.clientName("Disabled feedback client")
			.enabled(false)
			.build();
		when(clientAppRepository.findByClientId(registrationId)).thenReturn(Optional.of(clientApp));

		assertThatThrownBy(() -> service.createSubmission(new FeedbackSubmissionRequest(), registrationId))
			.isInstanceOf(ResponseStatusException.class)
			.hasMessageContaining("Client application is disabled")
			.satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
				.isEqualTo(HttpStatus.FORBIDDEN));
		verifyNoInteractions(feedbackSubmissionRepository, projectRepository);
	}

	@Test
	void getSubmissionsByClientAppRegistrationIdWithoutDatesMapsStoredJsonAndMetadata() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		FeedbackSubmissionEntity entity = new FeedbackSubmissionEntity();
		entity.setId(UUID.randomUUID());
		entity.setClientId(UUID.randomUUID());
		entity.setClientAppRegistrationId(clientAppRegistrationId);
		entity.setClientName("Feedback client");
		entity.setProjectKey("PROJECT");
		entity.setQuestions(new ObjectMapper().valueToTree(List.of(Map.of("id", "q1"))));
		entity.setAnswers(new ObjectMapper().valueToTree(Map.of("q1", Map.of("value", "yes"))));
		when(feedbackSubmissionRepository.findByClientAppRegistrationIdOrderBySubmittedAtDesc(clientAppRegistrationId))
			.thenReturn(List.of(entity));

		List<FeedbackSubmission> submissions = service.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId,
				null, null);

		assertThat(submissions).hasSize(1);
		assertThat(submissions.get(0).getProjectKey().orElse(null)).isEqualTo("PROJECT");
		assertThat(submissions.get(0).getQuestions()).hasSize(1);
		assertThat(submissions.get(0).getAnswers()).containsKey("q1");
	}

	@Test
	void getSubmissionsByClientAppRegistrationIdFiltersFromStartDate() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime startDate = OffsetDateTime.parse("2025-01-01T00:00:00Z");

		service.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, startDate, null);

		verify(feedbackSubmissionRepository)
			.findByClientAppRegistrationIdAndSubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					startDate);
	}

	@Test
	void getSubmissionsByClientAppRegistrationIdFiltersUntilEndDate() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime endDate = OffsetDateTime.parse("2025-12-31T23:59:59Z");

		service.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, null, endDate);

		verify(feedbackSubmissionRepository)
			.findByClientAppRegistrationIdAndSubmittedAtLessThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					endDate);
	}

	@Test
	void getSubmissionsByClientAppRegistrationIdFiltersBetweenDates() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime startDate = OffsetDateTime.parse("2025-01-01T00:00:00Z");
		OffsetDateTime endDate = OffsetDateTime.parse("2025-12-31T23:59:59Z");

		service.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, startDate, endDate);

		verify(feedbackSubmissionRepository).findByClientAppRegistrationIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
				clientAppRegistrationId, startDate, endDate);
	}

	private void stubSaveAndFlush() {
		when(feedbackSubmissionRepository.saveAndFlush(any(FeedbackSubmissionEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
	}

}