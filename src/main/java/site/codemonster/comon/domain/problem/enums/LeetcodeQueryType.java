package site.codemonster.comon.domain.problem.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeetcodeQueryType {

    PROBLEM_DETAIL("""
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
        """);

    private final String queryTemplate;

    public String formatQuery(String slug) {
        return String.format(queryTemplate, slug);
    }
}
