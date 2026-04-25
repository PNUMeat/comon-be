package site.codemonster.comon.domain.article.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.request.ArticleCommentRequest;
import site.codemonster.comon.domain.article.dto.response.ArticleCommentResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleCreateCommentResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.global.error.articlecomment.CommentNotAuthorException;
import site.codemonster.comon.global.error.articlecomment.CommentNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleCommentHighService {

    private final ArticleCommentLowService articleCommentLowService;
    private final ArticleLowService articleLowService;
    private final TeamMemberLowService teamMemberLowService;

    public ArticleCreateCommentResponse createComment(Long articleId, Member member, ArticleCommentRequest request) {
        Article article = articleLowService.findById(articleId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);

        ArticleComment articleComment = articleCommentLowService.save(new ArticleComment(article, member, request.description()));
        return new ArticleCreateCommentResponse(articleComment.getCommentId(), article.getMember().getId(), article.getArticleTitle(), articleComment.getDescription());
    }

    @Transactional(readOnly = true)
    public Page<ArticleCommentResponse> getComments(Long articleId, Member member, Pageable pageable) {
        Article article = articleLowService.findById(articleId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);

        Page<ArticleComment> commentPages = articleCommentLowService.findActiveCommentsByArticleId(articleId, pageable);

        return commentPages.map(ArticleCommentResponse::new);
    }

    public ArticleComment updateComment(Long articleId, Long commentId, Member member, ArticleCommentRequest request) {
        Article article = articleLowService.findById(articleId);
        ArticleComment comment = articleCommentLowService.findById(commentId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);
        validateCommentOwnership(comment, articleId, member);

        comment.updateDescription(request.description());
        return comment;
    }

    public void deleteComment(Long articleId, Long commentId, Member member) {
        Article article = articleLowService.findById(articleId);
        ArticleComment comment = articleCommentLowService.findById(commentId);

        teamMemberLowService.validateTeamMember(article.getTeam().getTeamId(), member);
        validateCommentOwnership(comment, articleId, member);

        comment.softDelete();
    }

    private void validateCommentOwnership(ArticleComment comment, Long articleId, Member member) {
        if (!comment.getArticle().getArticleId().equals(articleId)) {
            throw new CommentNotFoundException();
        }
        if (!comment.isAuthor(member)) {
            throw new CommentNotAuthorException();
        }
    }
}
