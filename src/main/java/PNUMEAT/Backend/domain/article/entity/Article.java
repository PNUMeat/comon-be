package PNUMEAT.Backend.domain.article.entity;

import PNUMEAT.Backend.domain.article.enums.ArticleCategory;
import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.global.util.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "article")
public class Article extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String articleTitle;

    @Column(length = 3000)
    private String articleBody;

    @Enumerated(value = EnumType.STRING)
    private ArticleCategory articleCategory;

    private LocalDate selectedDate;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleImage> images = new ArrayList<>();

    public void updateTitle(String title){
        this.articleTitle = title;
    }
    public void updateBody(String body){
        this.articleBody = body;
    }
    public void updateCategory(ArticleCategory articleCategory){
        this.articleCategory = articleCategory;
    }

    public Article() {}

    public Article(Team team, Member member, String articleTitle, String articleBody,
                   ArticleCategory articleCategory, List<ArticleImage> images) {
        this.team = team;
        this.member = member;
        this.articleTitle = articleTitle;
        this.articleBody = articleBody;
        this.articleCategory = articleCategory;
        this.selectedDate = null;
        this.images = images;
    }

    @Builder
    public Article(Team team, Member member, String articleTitle, String articleBody,
                   ArticleCategory articleCategory, LocalDate selectedDate) {
        this.team = team;
        this.member = member;
        this.articleTitle = articleTitle;
        this.articleBody = articleBody;
        this.articleCategory = articleCategory;
        this.selectedDate = selectedDate;
    }

    public void addImage(ArticleImage image) {
        images.add(image);
        image.updateArticle(this);
    }
}
