package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.request.CalenderSubjectRequest;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.error.articles.ArticleNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static site.codemonster.comon.domain.article.enums.ArticleCategory.getSubjectCategories;

@Transactional
@Service
@RequiredArgsConstructor
public class ArticleLowService {

    private final ArticleRepository articleRepository;

    public Article save(Article article) {
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Page<Article> getMyVisibleArticlesUsingPaging(Long memberId, Long teamId, Pageable pageable){
        return articleRepository.findVisibleArticleByMemberIdAndByTeamIdUsingPage(memberId, teamId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Article> getAllVisibleArticlesByTeam(Long teamId){
        return articleRepository.findVisibleByTeamTeamIdWithImages(teamId);
    }

    @Transactional(readOnly = true)
    public Article findById(Long articleId){
        return articleRepository.findById(articleId).orElseThrow(ArticleNotFoundException::new);
    }

    public void delete(Article article) {
        articleRepository.delete(article);
    }

    public void deleteById(Long articleId) {
        articleRepository.deleteById(articleId);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long articleId) {
        return articleRepository.existsById(articleId);
    }

    @Transactional(readOnly = true)
    public Article findByIdWithImages(Long articleId) {
        return articleRepository.findByIdWithImages(articleId)
                .orElseThrow(ArticleNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Page<Article> findVisibleByTeamIdAndDateWithMember(Long teamId, LocalDate date, Pageable pageable) {

        return articleRepository.findVisibleByTeamIdAndDateWithMember(teamId, date, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Article> findTeamSubjectByTeamAndSelectedDate(Long teamId, LocalDate date, List<ArticleCategory> subjectCategories) {

        return articleRepository.findTeamSubjectByTeamAndSelectedDate(teamId, date, getSubjectCategories());
    }

    @Transactional(readOnly = true)
    public List<Article> findSubjectArticlesByTeamIdAndYearAndMonth(Long teamId, CalenderSubjectRequest calenderSubjectRequest) {
        return articleRepository.findSubjectArticlesByTeamIdAndYearAndMonth(teamId, calenderSubjectRequest.year(), calenderSubjectRequest.month(), getSubjectCategories());
    }

    @Transactional(readOnly = true)
    public boolean existsByTeamAndSelectedDateAndArticleCategoryIn(Team team, LocalDate selectedDate, List<ArticleCategory> subjectCategories) {
        return articleRepository.existsByTeamAndSelectedDateAndArticleCategoryIn(team, selectedDate, subjectCategories);
    }

    public void deleteByTeamTeamId(Long teamId) {
        articleRepository.deleteByTeamTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public List<Article> findArticleByTeamTeamIdAndMemberId(Long teamId, Long memberId) {
        return articleRepository.findArticleByTeamTeamIdAndMemberId(memberId, teamId);
    }

    public void deleteByMemberIdAndTeamId(Long memberId, Long teamId) {
        articleRepository.deleteByMemberIdAndTeamId(memberId, teamId);
    }


    public void deleteByMemberId(Long memberId) {
        articleRepository.deleteByMemberId(memberId);
    }
}
