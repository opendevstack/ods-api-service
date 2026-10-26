package org.opendevstack.apiservice.feedback.controller;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opendevstack.apiservice.feedback.controller.advice.FeedbackSubmissionExceptionHandler;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.feedback.service.FeedbackSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedbackSubmissionControllerTest {

	@Mock
	private FeedbackSubmissionService feedbackSubmissionService;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void createSubmissionReturnsCreated() {
		UUID registrationId = UUID.randomUUID();
		SecurityContextHolder.getContext()
			.setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token")
				.header("alg", "none")
				.claim("azp", registrationId.toString())
				.claim("preferred_username", "submitter@example.org")
				.build()));
		FeedbackSubmissionRequest request = new FeedbackSubmissionRequest().submitter("body@example.org");
		FeedbackSubmission submission = new FeedbackSubmission().submitter("body@example.org");
		when(feedbackSubmissionService.createSubmission(request, registrationId)).thenReturn(submission);
		FeedbackSubmissionController controller = new FeedbackSubmissionController(feedbackSubmissionService);

		ResponseEntity<FeedbackSubmission> response = controller.registerFeedbackSubmission(request);

		assertThat(response.getStatusCode().value()).isEqualTo(201);
		assertThat(response.getBody()).isEqualTo(submission);
		verify(feedbackSubmissionService).createSubmission(request, registrationId);
	}

	@Test
	void invalidSubmitterEmailReturnsBadRequestWithMessage() throws Exception {
		MockMvc mockMvc = feedbackSubmissionMvc();

		mockMvc.perform(post("/api/v1/feedback/submissions").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "submitter": "not-an-email",
				  "questions": [],
				  "answers": {}
				}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("submitter: must be a well-formed email address"));

		verifyNoInteractions(feedbackSubmissionService);
	}

	@Test
	void missingSubmitterReturnsBadRequestWithMessage() throws Exception {
		MockMvc mockMvc = feedbackSubmissionMvc();

		mockMvc.perform(post("/api/v1/feedback/submissions").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "questions": [],
				  "answers": {}
				}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("submitter: must not be null"));

		verifyNoInteractions(feedbackSubmissionService);
	}

	@Test
	void getSubmissionsAcceptsClientAppRegistrationIdAndDateBounds() throws Exception {
		UUID clientAppRegistrationId = UUID.randomUUID();
		OffsetDateTime startDate = OffsetDateTime.parse("2025-01-01T00:00:00Z");
		OffsetDateTime endDate = OffsetDateTime.parse("2025-12-31T23:59:59Z");
		when(feedbackSubmissionService.getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, startDate,
				endDate))
			.thenReturn(List.of());

		feedbackSubmissionMvc()
			.perform(get("/api/v1/feedback/submissions/{client_app_registration_id}", clientAppRegistrationId)
				.param("startDate", startDate.toString())
				.param("endDate", endDate.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray());

		verify(feedbackSubmissionService).getSubmissionsByClientAppRegistrationId(clientAppRegistrationId, startDate,
				endDate);
	}

	@Test
	void invalidStartDateReturnsBadRequest() throws Exception {
		feedbackSubmissionMvc()
			.perform(get("/api/v1/feedback/submissions/{client_app_registration_id}", UUID.randomUUID())
				.param("startDate", "not-a-timestamp"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Invalid value for parameter 'startDate'."));
	}

	private MockMvc feedbackSubmissionMvc() {
		return MockMvcBuilders.standaloneSetup(new FeedbackSubmissionController(feedbackSubmissionService))
			.setControllerAdvice(new FeedbackSubmissionExceptionHandler())
			.build();
	}

}