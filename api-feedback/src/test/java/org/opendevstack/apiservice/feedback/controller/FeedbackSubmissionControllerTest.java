package org.opendevstack.apiservice.feedback.controller;

import java.util.List;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opendevstack.apiservice.feedback.controller.advice.FeedbackSubmissionExceptionHandler;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmission;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionRequest;
import org.opendevstack.apiservice.feedback.model.FeedbackSubmissionsMetadata;
import org.opendevstack.apiservice.feedback.model.GetFeedbackSubmissionsResponse;
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
	void missingQuestionsReturnsBadRequestWithMessage() throws Exception {
		feedbackSubmissionMvc()
			.perform(post("/api/v1/feedback/submissions").contentType(MediaType.APPLICATION_JSON).content("""
					{
					  "submitter": "submitter@example.org",
					  "answers": {}
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("questions: must not be null"));

		verifyNoInteractions(feedbackSubmissionService);
	}

	@Test
	void missingAnswersReturnsBadRequestWithMessage() throws Exception {
		feedbackSubmissionMvc()
			.perform(post("/api/v1/feedback/submissions").contentType(MediaType.APPLICATION_JSON).content("""
					{
					  "submitter": "submitter@example.org",
					  "questions": []
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("answers: must not be null"));

		verifyNoInteractions(feedbackSubmissionService);
	}

	@Test
	void getSubmissionsAcceptsClientAppRegistrationIdAndDateBounds() throws Exception {
		OffsetDateTime startDate = OffsetDateTime.parse("2025-01-01T00:00:00Z");
		OffsetDateTime endDate = OffsetDateTime.parse("2025-12-31T23:59:59Z");
		GetFeedbackSubmissionsResponse response = new GetFeedbackSubmissionsResponse().submissions(List.of())
			.metadata(new FeedbackSubmissionsMetadata().page(0).size(20).totalElements(0).totalPages(0).last(true));
		when(feedbackSubmissionService.getFeedbackSubmissions(startDate, endDate, 0, 20)).thenReturn(response);

		feedbackSubmissionMvc()
			.perform(get("/api/v1/feedback/submissions")
				.param("startDate", startDate.toString())
				.param("endDate", endDate.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.submissions").isArray())
			.andExpect(jsonPath("$.metadata.page").value(0))
			.andExpect(jsonPath("$.metadata.size").value(20))
			.andExpect(jsonPath("$.metadata.last").value(true));

		verify(feedbackSubmissionService).getFeedbackSubmissions(startDate, endDate, 0, 20);
	}

	@Test
	void invalidStartDateReturnsBadRequest() throws Exception {
		feedbackSubmissionMvc()
			.perform(get("/api/v1/feedback/submissions")
				.param("startDate", "not-a-timestamp"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Invalid value for parameter 'startDate'."));
	}

	@ParameterizedTest
	@CsvSource({ "2025-01-02T00:00:00Z, 2025-01-01T00:00:00Z",
			"2025-06-02T00:00:00Z, 2025-06-01T00:00:00Z" })
	void startDateAfterEndDateReturnsBadRequest(String startDateValue, String endDateValue) throws Exception {
		feedbackSubmissionMvc()
			.perform(get("/api/v1/feedback/submissions")
				.param("startDate", startDateValue)
				.param("endDate", endDateValue))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("startDate must be before or equal to endDate."));

		verifyNoInteractions(feedbackSubmissionService);
	}

	@ParameterizedTest
	@CsvSource({ "-1, 20, Page must be greater than or equal to 0.",
			"0, 0, Size must be between 1 and 100.", "0, 101, Size must be between 1 and 100." })
	void invalidPaginationParametersReturnBadRequest(int page, int size, String expectedMessage) throws Exception {
		feedbackSubmissionMvc()
			.perform(get("/api/v1/feedback/submissions")
				.param("page", Integer.toString(page))
				.param("size", Integer.toString(size)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(expectedMessage));

		verifyNoInteractions(feedbackSubmissionService);
	}

	private MockMvc feedbackSubmissionMvc() {
		return MockMvcBuilders.standaloneSetup(new FeedbackSubmissionController(feedbackSubmissionService))
			.setControllerAdvice(new FeedbackSubmissionExceptionHandler())
			.build();
	}

}