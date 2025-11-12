package site.codemonster.comon.domain.article.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import site.codemonster.comon.global.entityListeners.TimeStamp;

@Getter
@Entity
@Table(name = "article_feedback")
public class ArticleFeedback extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", unique = true)
    private Article article;

    @Column(columnDefinition = "TEXT")
    private String keyPoint;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String improvements;

    @Column(columnDefinition = "TEXT")
    private String learningPoint;

    protected ArticleFeedback() {}

    public ArticleFeedback(
            Article article,
            String keyPoint,
            String strengths,
            String improvements,
            String learningPoint
    ) {
        this.article = article;
        this.keyPoint = keyPoint;
        this.strengths = strengths;
        this.improvements = improvements;
        this.learningPoint = learningPoint;
    }

    public void updateFeedbackContent(String keyPoint, String strengths, String improvements, String learningPoint) {
        this.keyPoint = keyPoint;
        this.strengths = strengths;
        this.improvements = improvements;
        this.learningPoint = learningPoint;
    }

    public static ArticleFeedback fromAiResponse(String aiResponse, Article article) {
        return new ArticleFeedback(
                article,
                extractSection(aiResponse, "1. 문제 핵심 포인트"),
                extractSection(aiResponse, "2. 잘한 부분"),
                extractSection(aiResponse, "3. 개선 제안"),
                extractSection(aiResponse, "4. 학습 포인트")
        );
    }

    private static String extractSection(String text, String header) {
        int start = text.indexOf(header);
        if (start == -1) return "";

        start = text.indexOf("\n", start) + 1;
        int end = text.indexOf("###", start);
        if (end == -1) end = text.length();

        String content = text.substring(start, end).trim();
        return content.contains("- ") ? joinListItems(content) : content;
    }

    private static String joinListItems(String content) {
        return String.join("|||", content.split("\n- "))
                .replace("- ", "");
    }
}
