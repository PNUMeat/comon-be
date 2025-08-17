package site.codemonster.comon.domain.problem.collector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.dto.response.SolvedAcAPIResponse;
import site.codemonster.comon.domain.problem.enums.Platform;

@Slf4j
@Component
@RequiredArgsConstructor
public class BaekjoonCollector implements ProblemCollector {

    private static final List<String> BAEKJOON_LEVELS = List.of(
            "Unknown", "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby"
    );

    private final RestTemplate restTemplate;

    @Value("${problem.collection.baekjoon.problem-url}")
    private String solvedAcProblemApiUrl;

    @Override
    public ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request) {
        String problemId = request.getPlatformProblemId();

        try {
            SolvedAcAPIResponse problemInfo = fetchProblemInfoByAPI(problemId);

            return ProblemInfoResponse.builder()
                    .platform(Platform.BAEKJOON)
                    .platformProblemId(problemId)
                    .title(problemInfo.getTitleKo())
                    .difficulty(convertToDifficulty(problemInfo.getLevel()))
                    .url("https://www.acmicpc.net/problem/" + problemId)
                    .tags(problemInfo.getTagsAsString())
                    .isDuplicate(false)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("백준 문제 정보 수집 실패: {}", e.getMessage());
            throw new RuntimeException("백준 문제 정보를 찾을 수 없습니다: " + problemId, e);
        }
    }

    @Override
    public boolean isValidProblem(String problemId) {
        if (problemId == null || problemId.trim().isEmpty()) {
            return false;
        }

        try {
            int id = Integer.parseInt(problemId.trim());
            return id > 0 && id <= 999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private SolvedAcAPIResponse fetchProblemInfoByAPI(String problemId) {
        String apiUrl = solvedAcProblemApiUrl + "?problemId=" + problemId;
        log.info("solved.ac API 호출: {}", apiUrl);

        SolvedAcAPIResponse response = restTemplate.getForObject(apiUrl, SolvedAcAPIResponse.class);

        if (response == null || response.getProblemId() == null) {
            throw new RuntimeException("문제를 찾을 수 없습니다: " + problemId);
        }

        return response;
    }

    private String convertToDifficulty(Integer level) {
        if (level == null) return BAEKJOON_LEVELS.get(0);
        if (level >= 1 && level <= 5) return BAEKJOON_LEVELS.get(1);
        if (level >= 6 && level <= 10) return BAEKJOON_LEVELS.get(2);
        if (level >= 11 && level <= 15) return BAEKJOON_LEVELS.get(3);
        if (level >= 16 && level <= 20) return BAEKJOON_LEVELS.get(4);
        if (level >= 21 && level <= 25) return BAEKJOON_LEVELS.get(5);
        if (level >= 26 && level <= 30) return BAEKJOON_LEVELS.get(6);
        return BAEKJOON_LEVELS.get(0);
    }
}
