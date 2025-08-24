package site.codemonster.comon.domain.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationSettingsResponse;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.RecommendationHistoryRepository;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecommendationService {

    private final TeamService teamService;
    private final TeamRecommendationRepository teamRecommendationRepository;
    private final ProblemRepository problemRepository;
    private final RecommendationHistoryRepository recommendationHistoryRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.system-admin-id:1}")
    private Long systemAdminId;

    @Transactional
    public void saveRecommendationSettings(TeamRecommendationRequest request) {
        Team team = teamService.getTeamByTeamId(request.teamId());

        TeamRecommendation teamRecommendation = findOrCreateTeamRecommendation(team);
        updateTeamRecommendation(teamRecommendation, request);

        teamRecommendationRepository.save(teamRecommendation);
    }

    public TeamRecommendationSettingsResponse getRecommendationSettings(Long teamId) {
        Team team = teamService.getTeamByTeamId(teamId);

        TeamRecommendation teamRecommendation = teamRecommendationRepository
                .findByTeamIdWithPlatforms(teamId)
                .orElse(createDefaultTeamRecommendation(team));

        return TeamRecommendationSettingsResponse.of(teamRecommendation, objectMapper);
    }

    @Transactional
    public void resetRecommendationSettings(Long teamId) {
        Team team = teamService.getTeamByTeamId(teamId);

        teamRecommendationRepository.findByTeam(team)
                .ifPresentOrElse(
                        TeamRecommendation::resetRecommendationSetting,
                        () -> {
                            TeamRecommendation newRecommendation = createDefaultTeamRecommendation(team);
                            teamRecommendationRepository.save(newRecommendation);
                        }
                );
    }

    @Transactional
    public ManualRecommendationResponse executeManualRecommendation(ManualRecommendationRequest request) {
        Team team = teamService.getTeamByTeamId(request.teamId());
        Member systemAdmin = findSystemAdmin();

        TeamRecommendation teamRecommendation = teamRecommendationRepository
                .findByTeamIdWithPlatforms(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("팀의 추천 설정이 존재하지 않습니다."));

        int totalRecommended = 0;
        List<String> createdArticleTitles = new ArrayList<>();

        for (LocalDate date : request.selectedDates()) {
            if (isRecommendationAlreadyExists(team, date))
                continue;

            // 문제 추천 실행
            List<Problem> recommendedProblems = recommendProblemsForTeam(team, teamRecommendation, date);

            if (!recommendedProblems.isEmpty()) {
                // Article 생성
                String articleTitle = createRecommendationArticle(team, systemAdmin, recommendedProblems, date);

                // 추천 기록 저장
                saveRecommendationHistory(team, recommendedProblems, date);

                createdArticleTitles.add(articleTitle);
                totalRecommended += recommendedProblems.size();
            }
        }

        return ManualRecommendationResponse.of(totalRecommended, createdArticleTitles);
    }

    // === Private Methods ===

    private TeamRecommendation findOrCreateTeamRecommendation(Team team) {
        return teamRecommendationRepository.findByTeam(team)
                .orElse(createDefaultTeamRecommendation(team));
    }

    private TeamRecommendation createDefaultTeamRecommendation(Team team) {
        return TeamRecommendation.builder()
                .team(team)
                .autoRecommendationEnabled(false)
                .recommendationAt(9)
                .totalProblemCount(0)
                .build();
    }

    private Member findSystemAdmin() {
        return memberRepository.findById(systemAdminId)
                .orElseThrow(() -> new IllegalArgumentException("시스템 관리자를 찾을 수 없습니다."));
    }

    private void updateTeamRecommendation(TeamRecommendation teamRecommendation, TeamRecommendationRequest request) {
        teamRecommendation.setAutoRecommendationEnabled(request.autoRecommendationEnabled());
        teamRecommendation.setRecommendationAt(request.recommendationAt());
        teamRecommendation.setTotalProblemCount(calculateTotalProblemCount(request.platformSettings()));
        teamRecommendation.setRecommendationDays(request.recommendDays());

        List<PlatformRecommendation> platformRecommendations = request.platformSettings().stream()
                .map(this::createPlatformRecommendation)
                .collect(Collectors.toList());

        teamRecommendation.replacePlatformRecommendations(platformRecommendations);
    }

    private PlatformRecommendation createPlatformRecommendation(TeamRecommendationRequest.PlatformRecommendationSetting setting) {
        return PlatformRecommendation.builder()
                .platform(setting.platform())
                .difficulties(convertListToJson(setting.difficulties()))
                .tags(convertListToJson(setting.tags()))
                .problemCount(setting.problemCount())
                .enabled(setting.enabled())
                .build();
    }

    private Integer calculateTotalProblemCount(List<TeamRecommendationRequest.PlatformRecommendationSetting> settings) {
        return settings.stream()
                .filter(TeamRecommendationRequest.PlatformRecommendationSetting::enabled)
                .mapToInt(TeamRecommendationRequest.PlatformRecommendationSetting::problemCount)
                .sum();
    }

    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    // === 수동 추천 로직 ===

    private boolean isRecommendationAlreadyExists(Team team, LocalDate date) {
        return articleRepository.existsByTeamAndSelectedDateAndArticleCategory(
                team, date, ArticleCategory.CODING_TEST);
    }

    private List<Problem> recommendProblemsForTeam(Team team, TeamRecommendation teamRecommendation, LocalDate date) {
        List<PlatformRecommendation> enabledPlatforms = teamRecommendation.getPlatformRecommendations()
                .stream()
                .filter(PlatformRecommendation::getEnabled)
                .toList();

        if (enabledPlatforms.isEmpty()) {
            // 기본값: 각 플랫폼에서 2개씩
            return Arrays.stream(Platform.values())
                    .flatMap(platform -> recommendProblemsByPlatform(team, platform, 2, null, null).stream())
                    .collect(Collectors.toList());
        }

        return enabledPlatforms.stream()
                .flatMap(platformRec -> {
                    List<String> difficulties = parseJsonToList(platformRec.getDifficulties());
                    List<String> tags = parseJsonToList(platformRec.getTags());

                    return recommendProblemsByPlatform(
                            team,
                            platformRec.getPlatform(),
                            platformRec.getProblemCount(),
                            difficulties,
                            tags
                    ).stream();
                })
                .collect(Collectors.toList());
    }

    private List<Problem> recommendProblemsByPlatform(Team team, Platform platform, int count,
                                                      List<String> difficulties, List<String> tags) {
        // 이미 추천된 문제들 조회
        Set<Long> recommendedProblemIds = recommendationHistoryRepository
                .findRecommendedProblemIdsByTeamAndPlatform(team.getTeamId(), platform);

        // 추천 가능한 문제들 조회
        List<Problem> availableProblems;
        if ((difficulties != null && !difficulties.isEmpty()) || (tags != null && !tags.isEmpty())) {
            availableProblems = problemRepository.findFilteredProblems(platform, difficulties, tags, recommendedProblemIds);
        } else {
            availableProblems = problemRepository.findAvailableProblemsByPlatform(platform, recommendedProblemIds);
        }

        if (availableProblems.isEmpty()) {
            return Collections.emptyList();
        }

        // 랜덤 선택
        Collections.shuffle(availableProblems);
        return availableProblems.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.trim().equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String createRecommendationArticle(Team team, Member systemAdmin, List<Problem> problems, LocalDate date) {
        String articleTitle = createArticleTitle(date);
        String articleBody = createArticleBody(problems);

        Article article = Article.builder()
                .team(team)
                .member(systemAdmin)
                .articleTitle(articleTitle)
                .articleBody(articleBody)
                .articleCategory(ArticleCategory.CODING_TEST)
                .selectedDate(date)
                .build();

        articleRepository.save(article);
        return articleTitle;
    }

    private void saveRecommendationHistory(Team team, List<Problem> problems, LocalDate date) {
        List<RecommendationHistory> histories = problems.stream()
                .map(problem -> RecommendationHistory.of(team, problem, date))
                .collect(Collectors.toList());

        recommendationHistoryRepository.saveAll(histories);
    }

    private String createArticleTitle(LocalDate date) {
        String dayOfWeek = getDayOfWeekInKorean(date.getDayOfWeek());
        return String.format("%s(%s) 오늘의 문제",
                date.format(DateTimeFormatter.ofPattern("MM/dd")), dayOfWeek);
    }

    private String createArticleBody(List<Problem> problems) {
        StringBuilder body = new StringBuilder();
        body.append("<p dir=\"ltr\">");

        // 플랫폼별로 그룹핑
        Map<Platform, List<Problem>> problemsByPlatform = problems.stream()
                .collect(Collectors.groupingBy(Problem::getPlatform));

        for (Platform platform : Platform.values()) {
            List<Problem> platformProblems = problemsByPlatform.get(platform);
            if (platformProblems == null || platformProblems.isEmpty()) continue;

            body.append("<span style=\"font-size: 18px;\">✅ ")
                    .append(platform.getName())
                    .append("</span>");

            for (Problem problem : platformProblems) {
                body.append("<a href=\"").append(problem.getUrl())
                        .append("\" target=\"_blank\" rel=\"noreferrer\" class=\"editor-link\">")
                        .append("<span style=\"\">").append(problem.getTitle())
                        .append("</span></a><span style=\"\"></span>");
            }
        }

        body.append("</p>");
        return body.toString();
    }

    private String getDayOfWeekInKorean(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
