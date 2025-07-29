package site.codemonster.comon.domain.problem.repository;

import site.codemonster.comon.domain.problem.entity.RecommendationHistory;
import site.codemonster.comon.domain.problem.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {

    // 팀별 추천 기록 조회 (최신순)
    @Query("SELECT rh FROM RecommendationHistory rh " +
            "JOIN FETCH rh.problem " +
            "WHERE rh.team.teamId = :teamId " +
            "ORDER BY rh.recommendedAt DESC, rh.createdAt DESC")
    List<RecommendationHistory> findRecommendationHistoriesByTeamId(@Param("teamId") Long teamId);

    // 팀이 특정 문제를 받았는지 확인
    @Query("SELECT COUNT(rh) > 0 FROM RecommendationHistory rh " +
            "WHERE rh.team.teamId = :teamId AND rh.problem.problemId = :problemId")
    boolean existsRecommendationByTeamAndProblem(@Param("teamId") Long teamId,
                                                 @Param("problemId") Long problemId);

    // 특정 날짜의 모든 추천 기록
    @Query("SELECT rh FROM RecommendationHistory rh " +
            "JOIN FETCH rh.team " +
            "JOIN FETCH rh.problem " +
            "WHERE rh.recommendedAt = :date " +
            "ORDER BY rh.team.teamId")
    List<RecommendationHistory> findRecommendationsByDate(@Param("date") LocalDate date);

    // 팀의 추천 히스토리 초기화 (특정 플랫폼)
    @Modifying
    @Query("DELETE FROM RecommendationHistory rh " +
            "WHERE rh.team.teamId = :teamId " +
            "AND rh.problem.platform = :platform")
    void deleteRecommendationHistoryByTeamAndPlatform(@Param("teamId") Long teamId,
                                                      @Param("platform") Platform platform);

    // 팀의 전체 추천 히스토리 초기화
    @Modifying
    @Query("DELETE FROM RecommendationHistory rh WHERE rh.team.teamId = :teamId")
    void deleteRecommendationHistoryByTeamId(@Param("teamId") Long teamId);

    // 팀별 추천받은 문제 개수 (통계용)
    @Query("SELECT COUNT(rh) FROM RecommendationHistory rh WHERE rh.team.teamId = :teamId")
    Long countRecommendationsByTeamId(@Param("teamId") Long teamId);
}
