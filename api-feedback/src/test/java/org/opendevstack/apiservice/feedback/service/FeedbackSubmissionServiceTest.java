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
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opendevstack.apiservice.feedback.mapper.FeedbackSubmissionMapper;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.feedback.model.GetFeedbackSubmissionsResponse;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.FeedbackSubmissionEntity;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ClientAppRepository;
import org.opendevstack.apiservice.persistence.repository.FeedbackSubmissionRepository;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
				Mappers.getMapper(FeedbackSubmissionMapper.class), new ObjectMapper());
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
		FeedbackSubmissionRequest request = new FeedbackSubmissionRequest();
		when(clientAppRepository.findByClientId(registrationId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.createSubmission(request, registrationId))
			.isInstanceOf(ResponseStatusException.class)
			.hasMessageContaining("Client application is not registered")
			.satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED));
		verifyNoInteractions(feedbackSubmissionRepository, projectRepository);
	}

	@Test
	void createSubmissionWithDisabledClientReturnsForbidden() {
		UUID registrationId = UUID.randomUUID();
		FeedbackSubmissionRequest request = new FeedbackSubmissionRequest();
		ClientAppEntity clientApp = ClientAppEntity.builder()
			.id(UUID.randomUUID())
			.clientId(registrationId)
			.clientName("Disabled feedback client")
			.enabled(false)
			.build();
		when(clientAppRepository.findByClientId(registrationId)).thenReturn(Optional.of(clientApp));

		assertThatThrownBy(() -> service.createSubmission(request, registrationId))
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
		Pageable pageable = PageRequest.of(0, 20);
		when(feedbackSubmissionRepository.findByClientAppRegistrationIdOrderBySubmittedAtDesc(clientAppRegistrationId,
				pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

		GetFeedbackSubmissionsResponse response = service.getSubmissionsByClientAppRegistrationId(
				clientAppRegistrationId, null, null, null, null);

		assertThat(response.getSubmissions()).hasSize(1);
		assertThat(response.getSubmissions().get(0).getProjectKey().orElse(null)).isEqualTo("PROJECT");
		assertThat(response.getSubmissions().get(0).getQuestions()).hasSize(1);
		assertThat(response.getSubmissions().get(0).getAnswers()).containsKey("q1");
		assertThat(response.getMetadata().getPage()).isZero();
		assertThat(response.getMetadata().getSize()).isEqualTo(20);
		assertThat(response.getMetadata().getTotalElements()).isEqualTo(1);
		assertThat(response.getMetadata().getTotalPages()).isEqualTo(1);
		assertThat(response.getMetadata().getLast()).isTrue();
	}

	@Test
	void getSubmissionsByClientAppRegistrationIdFiltersFromStartDate() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime startDate = OffsetDateTime.parse("2025-01-01T00:00:00Z");
		Pageable pageable = PageRequest.of(2, 5);
		when(feedbackSubmissionRepository
			.findByClientAppRegistrationIdAndSubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					startDate, pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 30));

		service.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, startDate, null, 2, 5);

		verify(feedbackSubmissionRepository)
			.findByClientAppRegistrationIdAndSubmittedAtGreaterThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					startDate, pageable);
	}

	@Test
	void getSubmissionsByClientAppRegistrationIdFiltersUntilEndDate() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime endDate = OffsetDateTime.parse("2025-12-31T23:59:59Z");
		Pageable pageable = PageRequest.of(2, 5);
		when(feedbackSubmissionRepository
			.findByClientAppRegistrationIdAndSubmittedAtLessThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					endDate, pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 30));

		service.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, null, endDate, 2, 5);

		verify(feedbackSubmissionRepository)
			.findByClientAppRegistrationIdAndSubmittedAtLessThanEqualOrderBySubmittedAtDesc(clientAppRegistrationId,
					endDate, pageable);
	}

	@Test
	void getSubmissionsByClientAppRegistrationIdFiltersBetweenDates() {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime startDate = OffsetDateTime.parse("2025-01-01T00:00:00Z");
		OffsetDateTime endDate = OffsetDateTime.parse("2025-12-31T23:59:59Z");
		Pageable pageable = PageRequest.of(2, 5);
		when(feedbackSubmissionRepository
			.findByClientAppRegistrationIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(clientAppRegistrationId,
					startDate, endDate, pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 30));

		service.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, startDate, endDate, 2, 5);

		verify(feedbackSubmissionRepository).findByClientAppRegistrationIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
				clientAppRegistrationId, startDate, endDate, pageable);
	}

	@ParameterizedTest
	@CsvSource({ "-1, 20", "0, 0", "0, 101" })
	void getSubmissionsRejectsOutOfRangePaginationParameters(int page, int size) {
		assertThatThrownBy(() -> service.getSubmissionsByClientAppRegistrationId(UUID.randomUUID(), null, null, page,
				size))
			.isInstanceOf(ResponseStatusException.class)
			.satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
				.isEqualTo(HttpStatus.BAD_REQUEST));
		verifyNoInteractions(feedbackSubmissionRepository);
	}

	private void stubSaveAndFlush() {
		when(feedbackSubmissionRepository.saveAndFlush(any(FeedbackSubmissionEntity.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
	}

}