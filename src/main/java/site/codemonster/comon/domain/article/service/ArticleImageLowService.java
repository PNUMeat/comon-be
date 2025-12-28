package site.codemonster.comon.domain.article.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.repository.ArticleImageRepository;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class ArticleImageLowService {

    private final ArticleImageRepository articleImageRepository;

    public void deleteByArticleId(Long articleId) {
        articleImageRepository.deleteByArticleId(articleId);
    }

    public void deleteByTeamTeamId(Long teamId) {
        articleImageRepository.deleteByTeamTeamId(teamId);
    }

    public void deleteArticleImagesInArticleIds(List<Long> articleIds) {
        articleImageRepository.deleteArticleImagesInArticleIds(articleIds);
    }

    public void deleteByMemberId(Long memberId) {
        articleImageRepository.deleteByMemberId(memberId);
    }
}
