package site.codemonster.comon.domain.article.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.codemonster.comon.global.entityListeners.TimeStamp;

@Getter
@Entity
@Table(name = "article_feedback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleFeedback extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", unique = true)
    private Article article;

    @Column(columnDefinition = "TEXT")
    private String feedbackBody;

    public ArticleFeedback(Article article, String feedbackBody) {
        this.article = article;
        this.feedbackBody = feedbackBody;
    }
}
