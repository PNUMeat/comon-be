package site.codemonster.comon.domain.problem.repository;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    boolean existsByPlatformAndPlatformProblemId(Platform platform, String platformProblemId);

    long countByPlatform(Platform platform);

    List<Problem> findByPlatform(Platform platform);
}
