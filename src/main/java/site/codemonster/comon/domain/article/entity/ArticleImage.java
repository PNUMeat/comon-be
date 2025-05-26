package site.codemonster.comon.domain.article.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "article_image")
public class ArticleImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleImageId;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    public ArticleImage(String imageUrl, Article article) {
        this.imageUrl = imageUrl;
        this.article = article;
    }

    public ArticleImage() {

    }

    public void updateArticle(Article article) {
        this.article = article;
    }
}
