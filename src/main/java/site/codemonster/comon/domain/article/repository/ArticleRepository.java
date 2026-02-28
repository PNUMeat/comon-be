package site.codemonster.comon.domain.article.repository;

import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByTeamAndSelectedDateAndArticleCategoryIn(Team team, LocalDate selectedDate, List<ArticleCategory> articleCategories);

    List<Article> findArticleByTeamTeamIdAndMemberId(Long teamId, Long memberId);

    @Query("SELECT a FROM Article a "
            + "JOIN FETCH a.member "
            + "JOIN FETCH a.team "
            + "WHERE a.member.id = :memberId AND a.team.teamId = :teamId AND a.articleCategory = 'NORMAL' AND a.isVisible = true")
    Page<Article> findVisibleArticleByMemberIdAndByTeamIdUsingPage(@Param("memberId") Long memberId, @Param("teamId") Long teamId, Pageable pageable);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.team.teamId = :teamId AND a.articleCategory = 'NORMAL' AND a.isVisible = true")
    List<Article> findVisibleByTeamTeamIdWithImages(@Param("teamId") Long teamId);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.images WHERE a.articleId = :articleId AND a.articleCategory = 'NORMAL'")
    Optional<Article>  findByIdWithImages(@Param("articleId") Long articleId);

    @Query("SELECT a FROM Article a " +
        "JOIN FETCH a.member " +
        "WHERE a.team.teamId = :teamId " +
        "AND DATE(a.createdDate) = :date " +
        "AND a.articleCategory = 'NORMAL' " +
        "AND a.isVisible = true " +
        "ORDER BY a.createdDate DESC")
    Page<Article> findVisibleByTeamIdAndDateWithMember(@Param("teamId") Long teamId, @Param("date") LocalDate date, Pageable pageable);



    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.images " +
            "WHERE a.team.teamId = :teamId AND DATE(a.selectedDate) = :selectedDate " +
            "AND a.articleCategory IN :articleSubjectCategories")
    Optional<Article> findTeamSubjectByTeamAndSelectedDate(@Param("teamId")Long teamId, @Param("selectedDate") LocalDate selectedDate, @Param("articleSubjectCategories")List<ArticleCategory> articleSubjectCategories);

    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.images " +
            "WHERE a.team.teamId = :teamId AND YEAR (a.createdDate) = :year and MONTH (a.createdDate) = :month " +
            "AND a.articleCategory IN :articleSubjectCategories")
    List<Article> findSubjectArticlesByTeamIdAndYearAndMonth(@Param("teamId")Long teamId, @Param("year") int year, @Param("month") int month, @Param("articleSubjectCategories")List<ArticleCategory> articleSubjectCategories);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Article a WHERE a.articleId in :ids")
    void deleteByIds(@Param("ids") List<Long> ids);

    @Query("SELECT a FROM Article a where a.team.teamId = :teamId")
    List<Article> findByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT a FROM Article a where a.member.id = :memberId")
    List<Article> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT a FROM Article a WHERE a.member.id = :memberId AND a.team.teamId = :teamId")
    List<Article> findByMemberIdAndTeamId(@Param("memberId") Long memberId, @Param("teamId") Long teamId);
}
