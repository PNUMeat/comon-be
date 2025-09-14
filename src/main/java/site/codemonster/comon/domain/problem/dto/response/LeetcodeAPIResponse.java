package site.codemonster.comon.domain.problem.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeetcodeAPIResponse {
    private Map<String, Object> data;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getQuestion() {
        if (data == null) return null;
        return (Map<String, Object>) data.get("question");
    }

    public String getTitle() {
        Map<String, Object> question = getQuestion();
        return question != null ? (String) question.get("title") : null;
    }

    public String getDifficulty() {
        Map<String, Object> question = getQuestion();
        return question != null ? (String) question.get("difficulty") : null;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTopicTags() {
        Map<String, Object> question = getQuestion();
        return question != null ? (List<Map<String, Object>>) question.get("topicTags") : null;
    }
}
