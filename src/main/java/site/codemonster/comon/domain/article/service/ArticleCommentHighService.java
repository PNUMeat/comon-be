package site.codemonster.comon.domain.article.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.request.ArticleCommentCreateRequest;
import site.codemonster.comon.domain.article.dto.request.ArticleCommentUpdateRequest;
import site.codemonster.comon.domain.article.dto.response.ArticleCommentListResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleCommentResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.global.error.ArticleComment.CommentDeleteNotAuthorException;
import site.codemonster.comon.global.error.ArticleComment.CommentNotAuthorException;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleCommentHighService {

    private final ArticleCommentLowService articleCommentLowService;
    private final ArticleLowService articleLowService;
    private final TeamMemberLowService teamMemberLowService;

    public ArticleComment createComment(Long articleId, Member member, ArticleCommentCreateRequest request) {
        Article article = articleLowService.findById(articleId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);

        return articleCommentLowService.save(new ArticleComment(article, member, request.description()));
    }

    @Transactional(readOnly = true)
    public ArticleCommentListResponse getComments(Long articleId, Member member) {
        Article article = articleLowService.findById(articleId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);

        List<ArticleComment> comments = articleCommentLowService.findAllByArticleIdWithMember(articleId);
        List<ArticleCommentResponse> responses = comments.stream()
                .map(ArticleCommentResponse::new)
                .toList();
        return new ArticleCommentListResponse(responses);
    }

    public ArticleComment updateComment(Long articleId, Long commentId, Member member, ArticleCommentUpdateRequest request) {
        Article article = articleLowService.findById(articleId);
        ArticleComment comment = articleCommentLowService.findById(commentId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);
        validateCommentAuthor(comment, member);

        comment.updateDescription(request.description());
        return comment;
    }

    public void deleteComment(Long articleId, Long commentId, Member member) {
        Article article = articleLowService.findById(articleId);
        ArticleComment comment = articleCommentLowService.findById(commentId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);
        validateCommentAuthor(comment, member);

        articleCommentLowService.delete(comment);
    }

    private void validateCommentAuthor(ArticleComment comment, Member member) {
        if (!comment.isAuthor(member)) {
            throw new CommentNotAuthorException();
        }
    }
}
