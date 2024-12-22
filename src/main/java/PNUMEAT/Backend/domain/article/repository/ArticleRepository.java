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
    boolean existsByTeamAndCreatedDateAndArticleCategoryIn(Team team, LocalDate createdDate, List<ArticleCategory> articleCategories);


    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.member.id = :memberId")
    List<Article> findByMemberIdWithImages(@Param("memberId") Long memberId);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.team.teamId = :teamId")
    List<Article> findByTeamTeamIdWithImages(@Param("teamId") Long teamId);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.articleId = :articleId")
    Optional<Article> findByIdWithImages(@Param("articleId") Long articleId);

    @Query("SELECT a FROM Article a WHERE a.team.teamId = :teamId AND DATE(a.createdDate) = :date")
    Page<Article> findByTeamIdAndDate(@Param("teamId") Long teamId, @Param("date") LocalDate date, Pageable pageable);
}
