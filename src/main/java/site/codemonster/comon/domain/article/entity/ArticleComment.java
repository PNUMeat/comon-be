package site.codemonster.comon.domain.article.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.entityListeners.TimeStamp;

@Getter
@Entity
@Table(name = "article_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleComment extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 300)
    private String description;

    public ArticleComment(Article article, Member member, String description) {
        this.article = article;
        this.member = member;
        this.description = description;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public boolean isAuthor(Member member) {
        return this.member.getId().equals(member.getId());
    }
}
