package site.codemonster.comon.domain.problem.collector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.enums.LeetcodeQueryType;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.error.problem.ProblemInvalidInputException;
import site.codemonster.comon.global.error.problem.ProblemNotFoundException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LeetcodeCollector implements ProblemCollector {

    private static final String LEETCODE_PROBLEM_URL_PREFIX = "https://leetcode.com/problems/";
    private final RestTemplate restTemplate;
    @Value("${problem.collection.leetcode.graphql-url:https://leetcode.com/graphql}")
    private String leetcodeGraphqlUrl;

    @Override
    public ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request) {
        String url = request.getPlatformProblemId();

        if (isValidProblemId(url)) {
            throw new ProblemInvalidInputException();
        }

        String slug = extractSlugFromUrl(url, true);
        Map<String, Object> problemInfo = fetchProblemInfoByGraphQL(slug);

        return ProblemInfoResponse.builder()
                .platform(Platform.LEETCODE)
                .platformProblemId(slug)
                .title((String) problemInfo.get("title"))
                .difficulty((String) problemInfo.get("difficulty"))
                .url(url)
                .tags(formatTags(problemInfo))
                .isDuplicate(false)
                .success(true)
                .build();
    }

    @Override
    public boolean isValidProblemId(String leetcodeUrl) {
        if (leetcodeUrl == null || leetcodeUrl.trim().isEmpty()) {
            return true;
        }

        String trimmedUrl = leetcodeUrl.trim();

        return !trimmedUrl.startsWith(LEETCODE_PROBLEM_URL_PREFIX) ||
                !trimmedUrl.contains("/problems/") ||
                extractSlugFromUrl(trimmedUrl, false) == null;
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

    private Map<String, Object> fetchProblemInfoByGraphQL(String slug) {
        String query = LeetcodeQueryType.PROBLEM_DETAIL.formatQuery(slug);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (compatible; ProblemCollector/1.0)");

        Map<String, Object> requestBody = Map.of("query", query);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(leetcodeGraphqlUrl, request, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> question = (Map<String, Object>) data.get("question");

        if (question == null || question.get("title") == null || question.get("difficulty") == null) {
            throw new ProblemNotFoundException();
        }

        return question;
    }

    private String formatTags(Map<String, Object> problemInfo) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> topicTags =
                    (List<Map<String, Object>>) problemInfo.get("topicTags");

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
