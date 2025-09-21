package site.codemonster.comon.domain.recommendation.repository;

import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.codemonster.comon.domain.problem.enums.Platform;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    /**
     * 팀별 플랫폼의 추천된 문제 ID들 조회
     */
    @Query("SELECT rh.problem.problemId FROM RecommendationHistory rh " +
            "WHERE rh.team.teamId = :teamId AND rh.problem.platform = :platform")
    Set<Long> findRecommendedProblemIdsByTeamAndPlatform(@Param("teamId") Long teamId,
                                                         @Param("platform") Platform platform);

    /**
     * 팀의 특정 날짜 추천 기록 존재 여부
     */
    boolean existsByTeamTeamIdAndRecommendedAt(Long teamId, LocalDate date);

    /**
     * 팀의 추천 기록 삭제
     */
    void deleteByTeamTeamId(Long teamId);

    @Query("select rh from RecommendationHistory rh where rh.recommendedAt = :today")
    List<RecommendationHistory> findByLocalDate(LocalDate today);

    @Query("select rh from RecommendationHistory rh where rh.team.teamId = :teamId")
    List<RecommendationHistory> findByTeamId(Long teamId);
}
