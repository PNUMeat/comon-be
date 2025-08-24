package site.codemonster.comon.domain.problem.repository;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsByPlatformAndPlatformProblemId(Platform platform, String platformProblemId);

    long countByPlatform(Platform platform);

    /**
     * 특정 플랫폼의 추천 가능한 문제들 조회 (이미 추천된 문제 제외)
     */
    @Query("SELECT p FROM Problem p WHERE p.platform = :platform AND p.problemId NOT IN :excludedIds")
    List<Problem> findAvailableProblemsByPlatform(@Param("platform") Platform platform,
                                                  @Param("excludedIds") Set<Long> excludedIds);

    /**
     * 특정 플랫폼의 필터링된 문제들 조회 (난이도, 태그 조건 + 이미 추천된 문제 제외)
     */
    @Query("SELECT p FROM Problem p WHERE p.platform = :platform " +
            "AND p.problemId NOT IN :excludedIds " +
            "AND (:difficulties IS NULL OR p.difficulty IN :difficulties) " +
            "AND (:tags IS NULL OR p.tags IN :tags)")
    List<Problem> findFilteredProblems(@Param("platform") Platform platform,
                                       @Param("difficulties") List<String> difficulties,
                                       @Param("tags") List<String> tags,
                                       @Param("excludedIds") Set<Long> excludedIds);
}
