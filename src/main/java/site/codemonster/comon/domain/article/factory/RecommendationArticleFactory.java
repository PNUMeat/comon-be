package site.codemonster.comon.domain.article.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.util.dateUtils.DateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendationArticleFactory {

    public static String createTitle(LocalDate date) {
        String dayOfWeek = DateUtils.getDayOfWeekInKorean(date.getDayOfWeek());
        return String.format("%s(%s) 오늘의 문제",
                date.format(DateTimeFormatter.ofPattern("MM/dd")), dayOfWeek);
    }

    public static String createBody(List<Problem> problems) {
        StringBuilder body = new StringBuilder();
        body.append("<p dir=\"ltr\">");

        Map<Platform, List<Problem>> problemsByPlatform = problems.stream()
                .collect(Collectors.groupingBy(Problem::getPlatform));

        for (Platform platform : Platform.values()) {
            List<Problem> platformProblems = problemsByPlatform.get(platform);
            if (platformProblems == null || platformProblems.isEmpty()) {
                continue;
            }

            body.append("<span style=\"font-size: 18px; font-weight: bold;\">")
                    .append(platform.getName())
                    .append("</span><br>");

            for (Problem problem : platformProblems) {
                body.append("<span style=\"margin-left: 12px;\">")
                        .append("[")
                        .append(problem.getProblemStep())
                        .append("] ")
                        .append("</span>");

                body.append("<a href=\"")
                        .append(problem.getUrl())
                        .append("\" target=\"_blank\" rel=\"noreferrer\" class=\"editor-link\">")
                        .append(problem.getTitle())
                        .append("</a><br>");
            }

            body.append("<br>");
        }

        body.append("</p>");
        return body.toString();
    }

    public static RecommendationArticleContent createContent(List<Problem> problems, LocalDate date) {
        return new RecommendationArticleContent(
                createTitle(date),
                createBody(problems)
        );
    }

    public record RecommendationArticleContent(String title, String body) {
    }
}
