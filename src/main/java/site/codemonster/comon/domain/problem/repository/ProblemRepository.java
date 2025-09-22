package site.codemonster.comon.domain.problem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsByPlatformAndPlatformProblemId(Platform platform, String platformProblemId);

    long countByPlatform(Platform platform);

    List<Problem> findByPlatform(Platform platform);

    @Query("select p from Problem p where p.problemId not in :excludedIds and p.platform = :platform and p.problemStep = :problemStep")
    Page<Problem> findRecommendationProblem(List<Long> excludedIds, Platform platform, ProblemStep problemStep, Pageable pageable);
}
