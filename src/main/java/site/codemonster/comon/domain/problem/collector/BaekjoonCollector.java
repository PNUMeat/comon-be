package site.codemonster.comon.domain.problem.collector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.dto.response.SolvedAcAPIResponse;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.error.problem.ProblemInvalidInputException;
import site.codemonster.comon.global.error.problem.ProblemNotFoundException;

@Component
@RequiredArgsConstructor
public class BaekjoonCollector implements ProblemCollector {

    private static final List<String> BAEKJOON_LEVELS = List.of(
            "Unknown", "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby"
    );

    private static final int MIN_PROBLEM_ID = 1000;
    private static final int MAX_PROBLEM_ID = 99999;

    private final RestClient restClient;

    @Value("${problem.collection.baekjoon.problem-url}")
    private String solvedAcProblemApiUrl;

    @Override
    public ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request) {
        String problemId = request.getPlatformProblemId();

        if (!isValidProblem(problemId)) {
            throw new ProblemInvalidInputException();
        }

        SolvedAcAPIResponse problemInfo = fetchProblemInfoByAPI(problemId);

        return ProblemInfoResponse.builder()
                .platform(Platform.BAEKJOON)
                .platformProblemId(problemId)
                .title(problemInfo.getTitleKo())
                .problemStep(request.getProblemStep())
                .url("https://www.acmicpc.net/problem/" + problemId)
                .isDuplicate(false)
                .success(true)
                .build();
    }

    @Override
    public boolean isValidProblem(String problemId) {
        if (problemId == null || problemId.trim().isEmpty()) {
            return false;
        }

        try {
            int id = Integer.parseInt(problemId.trim());
            return id >= MIN_PROBLEM_ID && id <= MAX_PROBLEM_ID;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private SolvedAcAPIResponse fetchProblemInfoByAPI(String problemId) {
        String apiUrl = solvedAcProblemApiUrl + "?problemId=" + problemId;

        SolvedAcAPIResponse response = restClient.get()
                .uri(apiUrl)
                .header("User-Agent", "Mozilla/5.0 (compatible; ProblemCollector/1.0)")
                .retrieve()
                .body(SolvedAcAPIResponse.class);

        if (response == null || response.getProblemId() == null || response.getTitleKo() == null || response.getTitleKo().trim().isEmpty()) {
            throw new ProblemNotFoundException();
        }

        return response;
    }

    private String convertToDifficulty(Integer level) {
        if (level == null || level < 1) {
            return BAEKJOON_LEVELS.get(0); // Unknown
        }

        int tierIndex = Math.min((level - 1) / 5 + 1, BAEKJOON_LEVELS.size() - 1);
        int subTier = ((level - 1) % 5) + 1;

        String problemTier = BAEKJOON_LEVELS.get(tierIndex);

        // Unknown이 아닌 경우에만 세부 등급 추가
        if (tierIndex > 0) {
            return problemTier + " " + (6 - subTier);
        }

        return problemTier;
    }
}
