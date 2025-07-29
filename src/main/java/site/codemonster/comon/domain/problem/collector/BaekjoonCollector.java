package site.codemonster.comon.domain.problem.collector;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import site.codemonster.comon.domain.problem.dto.SolvedAcProblemResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import site.codemonster.comon.domain.problem.service.ProblemService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaekjoonCollector implements ProblemCollector {

    private final RestTemplate restTemplate;
    private final ProblemService problemService;

    @Value("${problem.collection.baekjoon.problem-url}")
    private String solvedAcProblemApiUrl;

    @Override
    public Platform getPlatformName() {
        return Platform.BAEKJOON;
    }

    @Override
    public List<Problem> collectNewProblems(int limit) {
        try {
            Optional<String> maxProblemId = problemService.findMaxProblemIdByPlatform(Platform.BAEKJOON);
            int startId = maxProblemId.map(Integer::parseInt).orElse(1000) + 1;

            return collectProblemsInRange(startId, startId + limit - 1);
        } catch (Exception e) {
            log.error("백준 문제 수집 실패", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean hasNewProblems() {
        try {
            Optional<String> maxProblemId = problemService.findMaxProblemIdByPlatform(Platform.BAEKJOON);
            int currentMaxId = maxProblemId.map(Integer::parseInt).orElse(1000);

            Problem testProblem = fetchProblemFromSolvedAc(currentMaxId + 1);
            return testProblem != null;
        } catch (Exception e) {
            log.error("백준 신규 문제 확인 실패", e);
            return false;
        }
    }

    @Override
    public List<Problem> collectProblemsInRange(int startId, int endId) {
        List<Problem> problems = new ArrayList<>();

        for (int i = startId; i <= endId; i++) {
            try {
                if (problemService.existsProblem(Platform.BAEKJOON, String.valueOf(i))) {
                    continue;
                }

                Problem problem = fetchProblemFromSolvedAc(i);
                if (problem != null) {
                    problems.add(problem);
                }

                Thread.sleep(200);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("백준 문제 수집 중단됨", e);
                break;
            } catch (Exception e) {
                log.warn("백준 {}번 문제 수집 실패: {}", i, e.getMessage());
            }
        }

        log.info("백준 문제 {}개 수집 완료", problems.size());
        return problems;
    }

    private Problem fetchProblemFromSolvedAc(int problemId) {
        try {
            String apiUrl = solvedAcProblemApiUrl + "?problemId=" + problemId;
            log.debug("solved.ac API 호출: {}", apiUrl);

            SolvedAcProblemResponse response = restTemplate
                    .getForObject(apiUrl, SolvedAcProblemResponse.class);

            if (response == null || response.getProblemId() == null) {
                log.debug("백준 {}번 문제를 찾을 수 없음", problemId);
                return null;
            }

            return Problem.builder()
                    .platform(Platform.BAEKJOON)
                    .platformProblemId(String.valueOf(problemId))
                    .title(response.getTitleKo())
                    .difficulty(convertLevelToDifficulty(response.getLevel()))
                    .url("https://www.acmicpc.net/problem/" + problemId)
                    .build();

        } catch (Exception e) {
            log.error("solved.ac API 호출 실패 - 문제 {}: {}", problemId, e.getMessage());
            return null;
        }
    }

    private String convertLevelToDifficulty(Integer level) {
        if (level == null) return "Unknown";

        if (level >= 1 && level <= 5) return "Bronze";
        if (level >= 6 && level <= 10) return "Silver";
        if (level >= 11 && level <= 15) return "Gold";
        if (level >= 16 && level <= 20) return "Platinum";
        if (level >= 21 && level <= 25) return "Diamond";
        if (level >= 26 && level <= 30) return "Ruby";
        return "Unknown";
    }
}
