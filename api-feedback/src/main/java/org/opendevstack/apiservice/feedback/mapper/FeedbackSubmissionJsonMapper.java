package org.opendevstack.apiservice.feedback.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/** Converts feedback submission JSON fields using the application's configured mapper. */
@Component
@RequiredArgsConstructor
public class FeedbackSubmissionJsonMapper {

	private static final TypeReference<List<Map<String, Object>>> QUESTIONS_TYPE = new TypeReference<>() {
	};

	private static final TypeReference<Map<String, Object>> ANSWERS_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;

	@Named("toQuestionsJson")
	public JsonNode toQuestionsJson(List<Map<String, Object>> questions) {
		return objectMapper.valueToTree(questions);
	}

	@Named("toAnswersJson")
	public JsonNode toAnswersJson(Map<String, Object> answers) {
		return objectMapper.valueToTree(answers);
	}

	@Named("toQuestionsList")
	public List<Map<String, Object>> toQuestionsList(JsonNode questions) {
		return objectMapper.convertValue(questions, QUESTIONS_TYPE);
	}

	@Named("toAnswersMap")
	public Map<String, Object> toAnswersMap(JsonNode answers) {
		return objectMapper.convertValue(answers, ANSWERS_TYPE);
	}

}