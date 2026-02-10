package site.codemonster.comon.domain.article.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.article.repository.ArticleCommentRepository;
import site.codemonster.comon.global.error.ArticleComment.CommentNotFoundException;

@Transactional
@Service
@RequiredArgsConstructor
public class ArticleCommentLowService {

    private final ArticleCommentRepository articleCommentRepository;

    public ArticleComment save(ArticleComment comment) {
        return articleCommentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<ArticleComment> findAllByArticleIdWithMember(Long articleId) {
        return articleCommentRepository.findAllByArticleIdWithMember(articleId);
    }

    @Transactional(readOnly = true)
    public ArticleComment findById(Long commentId) {
        return articleCommentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
    }

    public void delete(ArticleComment comment) {
        articleCommentRepository.delete(comment);
    }
}
