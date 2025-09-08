package site.codemonster.comon.domain.problem.collector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.enums.Platform;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeetcodeCollector implements ProblemCollector {

    private final RestTemplate restTemplate;

    @Value("${problem.collection.leetcode.graphql-url:https://leetcode.com/graphql}")
    private String leetcodeGraphqlUrl;

    @Override
    public ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request) {
        String url = request.getPlatformProblemId();

        try {
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

        } catch (Exception e) {
            log.error("리트코드 문제 정보 수집 실패: {}", e.getMessage());
            throw new RuntimeException("리트코드 문제 정보를 찾을 수 없습니다: " + url, e);
        }
    }

    @Override
    public boolean isValidProblem(String leetcodeGraphqlUrl) {
        if (leetcodeGraphqlUrl == null || leetcodeGraphqlUrl.trim().isEmpty()) {
            return false;
        }

        String trimmedStr = leetcodeGraphqlUrl.trim();

        return trimmedStr.startsWith("https://leetcode.com/problems/") &&
                trimmedStr.contains("/problems/") &&
                extractSlugFromUrl(trimmedStr, false) != null;
    }

    public String extractSlugFromUrl(String url, boolean throwException) {
        if (url == null || !url.contains("/problems/")) {
            if (throwException) {
                throw new IllegalArgumentException("유효하지 않은 LeetCode URL입니다: " + url);
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
                return slug.isEmpty() ? null : slug;
            }
        } catch (Exception e) {
            log.error("URL에서 slug 추출 실패: {}", url, e);
            if (throwException) {
                throw new IllegalArgumentException("유효하지 않은 LeetCode URL입니다: " + url, e);
            }
        }

        if (throwException) {
            throw new IllegalArgumentException("유효하지 않은 LeetCode URL입니다: " + url);
        }
        return null;
    }

    private Map<String, Object> fetchProblemInfoByGraphQL(String slug) {
        String query = """
            {
                question(titleSlug: "%s") {
                    questionId
                    title
                    titleSlug
                    difficulty
                    topicTags {
                        name
                    }
                    content
                    stats
                }
            }
            """.formatted(slug);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (compatible; ProblemCollector/1.0)");

        Map<String, Object> requestBody = Map.of("query", query);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(leetcodeGraphqlUrl, request, Map.class);

            if (response == null || !response.containsKey("data")) {
                throw new RuntimeException("GraphQL 응답이 올바르지 않습니다");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            @SuppressWarnings("unchecked")
            Map<String, Object> question = (Map<String, Object>) data.get("question");

            if (question == null) {
                throw new RuntimeException("문제를 찾을 수 없습니다: " + slug);
            }

            return question;

        } catch (Exception e) {
            log.error("LeetCode GraphQL API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("LeetCode 문제 정보를 가져오는 데 실패했습니다: " + slug, e);
        }
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
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

        } catch (Exception e) {
            log.warn("태그 정보 파싱 실패: {}", e.getMessage());
            return "";
        }
    }
}
