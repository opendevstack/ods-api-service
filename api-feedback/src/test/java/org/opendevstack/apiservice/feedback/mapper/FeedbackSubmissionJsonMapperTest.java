package org.opendevstack.apiservice.feedback.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackSubmissionJsonMapperTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final FeedbackSubmissionJsonMapper mapper = new FeedbackSubmissionJsonMapper(objectMapper);

	@Test
	void convertsQuestionsToJson() throws JsonProcessingException {
		List<Map<String, Object>> questions = List.of(Map.of("id", "rating", "options", List.of("good", "bad")));

		JsonNode result = mapper.toQuestionsJson(questions);

		assertThat(result).isEqualTo(objectMapper.readTree("""
			[
			  {"id":"rating","options":["good","bad"]}
			]
			"""));
	}

	@Test
	void convertsAnswersToJson() throws JsonProcessingException {
		Map<String, Object> answers = Map.of("rating", Map.of("value", "good", "score", 5));

		JsonNode result = mapper.toAnswersJson(answers);

		assertThat(result).isEqualTo(objectMapper.readTree("""
			{"rating":{"value":"good","score":5}}
			"""));
	}

	@Test
	void convertsJsonToQuestions() throws JsonProcessingException {
		JsonNode questions = objectMapper.readTree("""
			[
			  {"id":"rating","options":["good","bad"]}
			]
			""");

		List<Map<String, Object>> result = mapper.toQuestionsList(questions);

		assertThat(result).containsExactly(Map.of("id", "rating", "options", List.of("good", "bad")));
	}

	@Test
	void convertsJsonToAnswers() throws JsonProcessingException {
		JsonNode answers = objectMapper.readTree("""
			{"rating":{"value":"good","score":5}}
			""");

		Map<String, Object> result = mapper.toAnswersMap(answers);

		assertThat(result).containsExactly(Map.entry("rating", Map.of("value", "good", "score", 5)));
	}

}