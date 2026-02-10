package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.article.repository.ArticleCommentRepository;

@Transactional
@Service
@RequiredArgsConstructor
public class ArticleCommentLowService {

    private final ArticleCommentRepository articleCommentRepository;

    public ArticleComment save(ArticleComment comment) {
        return articleCommentRepository.save(comment);
    }
}
