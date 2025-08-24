package site.codemonster.comon.domain.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("수동 추천 요청 시작 - Team ID: {}, Selected Dates: {}", request.teamId(), request.selectedDates());
        Team team = teamService.getTeamByTeamId(request.teamId());
        Member systemAdmin = findSystemAdmin();

        TeamRecommendation teamRecommendation = teamRecommendationRepository
                .findByTeamIdWithPlatforms(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("팀의 추천 설정이 존재하지 않습니다."));

        log.info("팀 추천 설정 존재 여부: {}", teamRecommendation != null);

        int totalRecommended = 0;
        List<String> createdArticleTitles = new ArrayList<>();
        StringBuilder failureMessageBuilder = new StringBuilder(); // 실패 메시지 빌더

        for (LocalDate date : request.selectedDates()) {
            log.info("날짜 {}에 대한 추천 확인", date);
            if (isRecommendationAlreadyExists(team, date)) {
                log.info("날짜 {}에 이미 추천 기록이 존재하여 건너뜁니다.", date);
                failureMessageBuilder.append(String.format("날짜 %s에 이미 추천 기록이 존재합니다.\n", date));
                continue;
            }
            log.info("날짜 {}에 대한 추천 기록이 존재하지 않아 추천을 진행합니다.", date);

            // 문제 추천 실행
            List<Problem> recommendedProblems = recommendProblemsForTeam(team, teamRecommendation, date);
            log.info("추천된 문제 수: {}", recommendedProblems.size());

            if (!recommendedProblems.isEmpty()) {
                String articleTitle = createRecommendationArticle(team, systemAdmin, recommendedProblems, date);
                saveRecommendationHistory(team, recommendedProblems, date);
                createdArticleTitles.add(articleTitle);
                totalRecommended += recommendedProblems.size();
                log.info("게시글 '{}'가 생성되었습니다.", articleTitle);
            } else {
                log.info("추천된 문제가 없어 게시글을 생성하지 않습니다.");
                failureMessageBuilder.append(String.format("날짜 %s에 추천할 문제가 없습니다. 설정 또는 문제 데이터를 확인하세요.\n", date));
            }
        }

        log.info("수동 추천 완료 - 총 추천 문제 수: {}", totalRecommended);

        String finalMessage;
        if (totalRecommended > 0) {
            String successMessage = String.format("총 %d개의 문제가 추천 완료되었습니다.\n", totalRecommended);
            if (failureMessageBuilder.length() > 0) {
                finalMessage = successMessage + "그러나 일부 날짜는 실패했습니다:\n" + failureMessageBuilder.toString();
            } else {
                finalMessage = successMessage;
            }
        } else {
            if (failureMessageBuilder.length() > 0) {
                finalMessage = "수동 추천에 실패했습니다. \n원인: " + failureMessageBuilder.toString();
            } else {
                finalMessage = "수동 추천에 실패했습니다. (추천할 문제가 없거나, 설정이 올바르지 않습니다.)";
            }
        }

        return ManualRecommendationResponse.of(totalRecommended, createdArticleTitles, finalMessage);
    }

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
        log.info("추천 문제 로직 시작 - 팀 ID: {}", team.getTeamId());
        List<PlatformRecommendation> enabledPlatforms = teamRecommendation.getPlatformRecommendations()
                .stream()
                .filter(PlatformRecommendation::getEnabled)
                .toList();
        log.info("활성화된 플랫폼 수: {}", enabledPlatforms.size());
        if (enabledPlatforms.isEmpty()) {
            log.info("활성화된 플랫폼이 없어 기본값으로 추천합니다.");
            return Arrays.stream(Platform.values())
                    .flatMap(platform -> recommendProblemsByPlatform(team, platform, 2, null, null).stream())
                    .collect(Collectors.toList());
        }
        log.info("설정된 플랫폼에 따라 추천을 진행합니다.");
        return enabledPlatforms.stream()
                .flatMap(platformRec -> {
                    List<String> difficulties = parseJsonToList(platformRec.getDifficulties());
                    List<String> tags = parseJsonToList(platformRec.getTags());
                    log.info("플랫폼: {}, 난이도: {}, 태그: {}, 문제 수: {}",
                            platformRec.getPlatform().name(), difficulties, tags, platformRec.getProblemCount());
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
        log.info("플랫폼 {}에서 추천 가능한 문제 찾기 시작. 개수: {}", platform.name(), count);
        Set<Long> recommendedProblemIds = recommendationHistoryRepository
                .findRecommendedProblemIdsByTeamAndPlatform(team.getTeamId(), platform);
        log.info("이미 추천된 문제 ID 수: {}", recommendedProblemIds.size());

        List<Problem> allProblemsForPlatform = problemRepository.findByPlatform(platform);
        log.info("DB에서 조회된 모든 문제 수: {}", allProblemsForPlatform.size());

        List<Problem> availableProblems = allProblemsForPlatform.stream()
                .filter(p -> !recommendedProblemIds.contains(p.getProblemId()))
                .collect(Collectors.toList());

        log.info("추천 기록을 제외한 문제 수: {}", availableProblems.size());

        List<Problem> filteredProblems = availableProblems.stream()
                .filter(p -> {
                    boolean difficultyMatch = (difficulties == null || difficulties.isEmpty()) || difficulties.contains(p.getDifficulty());
                    boolean tagsMatch = false;
                    if (tags == null || tags.isEmpty()) {
                        tagsMatch = true;
                    } else if (p.getTags() != null) {
                        String[] problemTags = p.getTags().split(",");
                        for (String problemTag : problemTags) {
                            if (tags.contains(problemTag.trim())) {
                                tagsMatch = true;
                                break;
                            }
                        }
                    }
                    return difficultyMatch && tagsMatch;
                })
                .collect(Collectors.toList());
        log.info("조회된 추천 가능한 문제 수: {}", filteredProblems.size());
        if (filteredProblems.isEmpty()) {
            log.warn("플랫폼 {}에 추천 가능한 문제가 없습니다.", platform.name());
            return Collections.emptyList();
        }
        log.info("문제 {}개를 랜덤으로 선택합니다.", count);
        Collections.shuffle(filteredProblems);
        return filteredProblems.stream()
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
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private String createRecommendationArticle(Team team, Member systemAdmin, List<Problem> problems, LocalDate date) {
        String articleTitle = createArticleTitle(date);
        String articleBody = createArticleBody(problems);
        log.info("게시글 생성 시작 - 제목: {}", articleTitle);
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
        log.info("추천 기록 저장 시작 - 문제 수: {}", problems.size());
        List<RecommendationHistory> histories = problems.stream()
                .map(problem -> RecommendationHistory.of(team, problem, date))
                .collect(Collectors.toList());
        recommendationHistoryRepository.saveAll(histories);
        log.info("추천 기록 {}건 저장 완료.", histories.size());
    }

    private String createArticleTitle(LocalDate date) {
        String dayOfWeek = getDayOfWeekInKorean(date.getDayOfWeek());
        return String.format("%s(%s) 오늘의 문제",
                date.format(DateTimeFormatter.ofPattern("MM/dd")), dayOfWeek);
    }

    private String createArticleBody(List<Problem> problems) {
        StringBuilder body = new StringBuilder();
        body.append("<p dir=\"ltr\">");
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
