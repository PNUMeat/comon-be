package site.codemonster.comon.domain.article.entity;

import org.jsoup.Jsoup;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.entityListeners.TimeStamp;
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

    @Lob
    private String articleBody;

    @Enumerated(value = EnumType.STRING)
    private ArticleCategory articleCategory;

    private LocalDate selectedDate;

    @OneToMany(mappedBy = "article")
    private List<ArticleImage> images = new ArrayList<>();

    public void updateArticle(String articleTitle, String articleBody){
        if(articleTitle != null){
            this.articleTitle = articleTitle;
        }

        boolean isEmpty = Jsoup.parse(articleBody).text().isEmpty();

        if(!isEmpty){
            this.articleBody = articleBody;
        }
    }

    public void updateSubject(String articleTitle, String articleBody, String articleCategory){
        updateArticle(articleTitle, articleBody);

        if(articleCategory != null){
            this.articleCategory = ArticleCategory.fromName(articleCategory);
        }
    }

    protected Article() {}

    public Article(Team team, Member member, String articleTitle, String articleBody,
                   ArticleCategory articleCategory) {
        this.team = team;
        this.member = member;
        this.articleTitle = articleTitle;
        this.articleBody = articleBody;
        this.articleCategory = articleCategory;
        this.selectedDate = null;
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

    public boolean isAuthor(Member member){
        return this.member.getId().equals(member.getId());
    }
}
