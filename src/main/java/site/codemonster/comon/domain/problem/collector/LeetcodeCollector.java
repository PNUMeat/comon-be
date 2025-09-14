package site.codemonster.comon.domain.problem.collector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.LeetcodeAPIResponse;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.enums.LeetcodeQueryType;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.error.problem.ProblemInvalidInputException;

import java.util.Map;
import site.codemonster.comon.global.error.problem.ProblemNotFoundException;

@Component
@RequiredArgsConstructor
public class LeetcodeCollector implements ProblemCollector {

    private static final String LEETCODE_PROBLEM_URL_PREFIX = "https://leetcode.com/problems/";
    private final RestClient restClient;
    @Value("${problem.collection.leetcode.graphql-url:https://leetcode.com/graphql}")
    private String leetcodeGraphqlUrl;

    @Override
    public ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request) {
        String url = request.getPlatformProblemId();

        if (!isValidProblem(url)) {
            throw new ProblemInvalidInputException();
        }

        String slug = extractSlugFromUrl(url, true);
        LeetcodeAPIResponse problemInfo = fetchProblemInfoByGraphQL(slug);

        return ProblemInfoResponse.builder()
                .platform(Platform.LEETCODE)
                .platformProblemId(slug)
                .title(problemInfo.getTitle())
                .difficulty(problemInfo.getDifficulty())
                .url(url)
                .tags(formatTags(problemInfo))
                .isDuplicate(false)
                .success(true)
                .build();
    }

    @Override
    public boolean isValidProblem(String leetcodeUrl) {
        if (leetcodeUrl == null || leetcodeUrl.trim().isEmpty()) {
            return false;
        }

        String trimmedUrl = leetcodeUrl.trim();

        return trimmedUrl.startsWith(LEETCODE_PROBLEM_URL_PREFIX) &&
                extractSlugFromUrl(trimmedUrl, false) != null;
    }

    public String extractSlugFromUrl(String url, boolean throwException) {
        if (url == null || !url.contains("/problems/")) {
            if (throwException) {
                throw new ProblemInvalidInputException();
            }
            return null;
        }

        try {
            String[] parts = url.split("/problems/");
            if (parts.length > 1) {
                String slug = parts[1].replaceAll("/$", "");

                // URL에서 추가 경로나 쿼리 파라미터 제거
                if (slug.contains("/")) {
                    slug = slug.substring(0, slug.indexOf("/"));
                }
                if (slug.contains("?")) {
                    slug = slug.substring(0, slug.indexOf("?"));
                }

                if (slug.isEmpty()) {
                    if (throwException) {
                        throw new ProblemInvalidInputException();
                    }
                    return null;
                }

                return slug;
            }
        } catch (Exception e) {
            if (throwException) {
                throw new ProblemInvalidInputException();
            }
        }

        if (throwException) {
            throw new ProblemInvalidInputException();
        }
        return null;
    }

    private LeetcodeAPIResponse fetchProblemInfoByGraphQL(String slug) {
        String query = LeetcodeQueryType.PROBLEM_DETAIL.formatQuery(slug);
        Map<String, Object> requestBody = Map.of("query", query);

        LeetcodeAPIResponse response = restClient.post()
                .uri(leetcodeGraphqlUrl)
                .header("User-Agent", "Mozilla/5.0 (compatible; ProblemCollector/1.0)")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(LeetcodeAPIResponse.class);

        if (response == null || response.getTitle() == null || response.getDifficulty() == null) {
            throw new ProblemNotFoundException();
        }

        return response;
    }

    private String formatTags(LeetcodeAPIResponse response) {
        try {
            List<Map<String, Object>> topicTags = response.getTopicTags();

            if (topicTags == null || topicTags.isEmpty()) {
                return "";
            }

            return topicTags.stream()
                    .map(tag -> (String) tag.get("name"))
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

        } catch (Exception e) {
            return "";
        }
    }
}
