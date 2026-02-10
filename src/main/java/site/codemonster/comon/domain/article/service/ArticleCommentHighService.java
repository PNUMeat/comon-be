package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.request.ArticleCommentCreateRequest;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.global.error.ArticleComment.CommentNotTeamMemberException;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleCommentHighService {

    private final ArticleCommentLowService articleCommentLowService;
    private final ArticleLowService articleLowService;
    private final TeamMemberLowService teamMemberLowService;

    public ArticleComment createComment(Long articleId, Member member, ArticleCommentCreateRequest request) {
        Article article = articleLowService.findById(articleId);

        Long teamId = article.getTeam().getTeamId();
        if (!teamMemberLowService.existsByTeamIdAndMemberId(teamId, member)) {
            throw new CommentNotTeamMemberException();
        }

        ArticleComment comment = new ArticleComment(article, member, request.description());
        return articleCommentLowService.save(comment);
    }
}
