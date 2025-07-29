package site.codemonster.comon.domain.problem.repository;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    // 중복 체크
    boolean existsByPlatformAndPlatformProblemId(Platform platform, String platformProblemId);

    // 플랫폼별 문제 조회
    @Query("SELECT p FROM Problem p WHERE p.platform = :platform ORDER BY p.createdAt DESC")
    List<Problem> findProblemsByPlatform(@Param("platform") Platform platform);

    // 특정 문제 찾기
    Optional<Problem> findByPlatformAndPlatformProblemId(Platform platform, String platformProblemId);

    // 추천용 - 특정 팀이 받지 않은 문제들 조회 (랜덤 추천용)
    @Query("SELECT p FROM Problem p " +
            "WHERE p.platform = :platform " +
            "AND p.problemId NOT IN (" +
            "    SELECT rh.problem.problemId FROM RecommendationHistory rh " +
            "    WHERE rh.team.teamId = :teamId" +
            ") " +
            "ORDER BY FUNCTION('RAND')")
    List<Problem> findAvailableProblemsForTeamRandom(@Param("platform") Platform platform,
                                                     @Param("teamId") Long teamId);

    // 전체 문제 수 (플랫폼별)
    @Query("SELECT COUNT(p) FROM Problem p WHERE p.platform = :platform")
    Long countByPlatform(@Param("platform") Platform platform);
}
