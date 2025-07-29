package site.codemonster.comon.domain.problem.collector;

import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;

import java.util.List;

public interface ProblemCollector {
    Platform getPlatformName();

    List<Problem> collectNewProblems(int limit);

    boolean hasNewProblems();

    List<Problem> collectProblemsInRange(int startId, int endId);
}
