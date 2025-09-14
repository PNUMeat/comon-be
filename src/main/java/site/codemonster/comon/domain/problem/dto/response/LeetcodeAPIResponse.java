package site.codemonster.comon.domain.problem.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeetcodeAPIResponse {
    private Data data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Question question;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private String title;
        private String difficulty;
        private List<TopicTag> topicTags;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicTag {
        private String name;
    }

    public String getTitle() {
        return (data != null && data.question != null) ? data.question.title : null;
    }

    public String getDifficulty() {
        return (data != null && data.question != null) ? data.question.difficulty : null;
    }

    public List<TopicTag> getTopicTags() {
        return (data != null && data.question != null) ? data.question.topicTags : null;
    }
}
