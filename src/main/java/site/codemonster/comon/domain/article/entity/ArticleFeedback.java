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

    @Builder
    public ArticleFeedback(Article article, String keyPoint, String strengths, String improvements, String learningPoint) {
        this.article = article;
        this.keyPoint = keyPoint;
        this.strengths = strengths;
        this.improvements = improvements;
        this.learningPoint = learningPoint;
    }
}
