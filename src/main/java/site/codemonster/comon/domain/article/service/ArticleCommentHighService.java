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
import site.codemonster.comon.global.error.ArticleComment.CommentNotAuthorException;
import site.codemonster.comon.global.error.ArticleComment.CommentNotTeamMemberException;
import site.codemonster.comon.global.error.ArticleComment.CommentReadNotTeamMemberException;

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

    @Transactional(readOnly = true)
    public ArticleCommentListResponse getComments(Long articleId, Member member) {
        Article article = articleLowService.findById(articleId);

        Long teamId = article.getTeam().getTeamId();
        if (!teamMemberLowService.existsByTeamIdAndMemberId(teamId, member)) {
            throw new CommentReadNotTeamMemberException();
        }

        List<ArticleComment> comments = articleCommentLowService.findAllByArticleIdWithMember(articleId);

        List<ArticleCommentResponse> responses = comments.stream()
                .map(ArticleCommentResponse::new)
                .toList();
        return new ArticleCommentListResponse(responses);
    }

    public ArticleComment updateComment(Long articleId, Long commentId, Member member, ArticleCommentUpdateRequest request) {
        Article article = articleLowService.findById(articleId);

        Long teamId = article.getTeam().getTeamId();
        if (!teamMemberLowService.existsByTeamIdAndMemberId(teamId, member)) {
            throw new CommentNotTeamMemberException();
        }

        ArticleComment comment = articleCommentLowService.findById(commentId);

        if (!comment.isAuthor(member)) {
            throw new CommentNotAuthorException();
        }

        comment.updateDescription(request.description());
        return comment;
    }
}
