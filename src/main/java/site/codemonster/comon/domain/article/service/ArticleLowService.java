package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.error.articles.ArticleNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static site.codemonster.comon.domain.article.enums.ArticleCategory.getSubjectCategories;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleLowService {

    private final ArticleRepository articleRepository;

    @Transactional
    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(Article article) {
        articleRepository.delete(article);
    }

    @Transactional
    public void deleteArticleById(Long articleId) {
        articleRepository.deleteById(articleId);
    }

    public Article getArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);
    }

    public Article getTeamSubject(Long teamId, LocalDate date) {
        return articleRepository.findTeamSubjectByTeamAndSelectedDate(teamId, date, getSubjectCategories())
                .orElse(null);
    }

    public Page<Article> getMyArticles(Long memberId, Long teamId, Pageable pageable) {
        return articleRepository.findArticleByMemberIdAndByTeamIdUsingPage(memberId, teamId, pageable);
    }

    public Page<Article> getArticlesByDate(Long teamId, LocalDate date, Pageable pageable) {
        return articleRepository.findByTeamIdAndDateWithMember(teamId, date, pageable);
    }

    public List<Article> getTeamArticles(Long teamId) {
        return articleRepository.findByTeamTeamIdWithImages(teamId);
    }

    public List<Article> getSubjectArticlesByDate(Long teamId, int year, int month) {
        return articleRepository.findSubjectArticlesByTeamIdAndYearAndMonth(teamId, year, month, getSubjectCategories());
    }

    public boolean isArticleExists(Long articleId) {
        return articleRepository.existsById(articleId);
    }

    public boolean isSubjectExists(Team team, LocalDate selectedDate) {
        return articleRepository.existsByTeamAndSelectedDateAndArticleCategoryIn(team, selectedDate, getSubjectCategories());
    }
}
