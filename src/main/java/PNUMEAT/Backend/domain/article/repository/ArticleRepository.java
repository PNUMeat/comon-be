package PNUMEAT.Backend.domain.article.repository;

import PNUMEAT.Backend.domain.article.entity.Article;
import java.time.LocalDate;

import PNUMEAT.Backend.domain.article.enums.ArticleCategory;
import PNUMEAT.Backend.domain.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByArticleId(Long articleId);
    boolean existsByTeamAndSelectedDateAndArticleCategoryIn(Team team, LocalDate selectedDate, List<ArticleCategory> articleCategories);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.member.id = :memberId AND a.articleCategory = 'NORMAL'")
    List<Article> findByMemberIdWithImages(@Param("memberId") Long memberId);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.team.teamId = :teamId AND a.articleCategory = 'NORMAL'")
    List<Article> findByTeamTeamIdWithImages(@Param("teamId") Long teamId);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.articleId = :articleId AND a.articleCategory = 'NORMAL'")
    Optional<Article> findByIdWithImages(@Param("articleId") Long articleId);

    @Query("SELECT a FROM Article a WHERE a.team.teamId = :teamId AND DATE(a.createdDate) = :date AND a.articleCategory = 'NORMAL'")
    Page<Article> findByTeamIdAndDate(@Param("teamId") Long teamId, @Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.images " +
            "WHERE a.team.teamId = :teamId AND DATE(a.selectedDate) = :selectedDate " +
            "AND a.articleCategory IN :articleSubjectCategories")
    Optional<Article> findTeamSubjectByTeamAndSelectedDate(@Param("teamId")Long teamId, @Param("selectedDate") LocalDate selectedDate, @Param("articleSubjectCategories")List<ArticleCategory> articleSubjectCategories);
}
