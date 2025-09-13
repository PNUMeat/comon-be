package site.codemonster.comon.domain.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.ProblemQueryService;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationSettingsResponse;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import site.codemonster.comon.global.error.recommendation.TeamRecommendationNotFoundException;
import site.codemonster.comon.global.util.convertUtils.ConvertUtils;
import site.codemonster.comon.global.util.responseUtils.ResponseUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecommendationService {

    private final TeamService teamService;
    private final ArticleService articleService;
    private final RecommendationHistoryService recommendationHistoryService;
    private final TeamRecommendationRepository teamRecommendationRepository;
    private final PlatformRecommendationService platformRecommendationService;
    private final MemberService memberService;
    private final ProblemQueryService problemQueryService;
    private final ObjectMapper objectMapper;
    private final ConvertUtils convertUtils;

    @Value("${app.system-admin-id:1}")
    private Long adminId;

    @Transactional
    public void saveRecommendationSettings(TeamRecommendation teamRecommendation, TeamRecommendationRequest request) {
        teamRecommendation.updateInitialSettings(request);
        teamRecommendationRepository.save(teamRecommendation);
    }

    public TeamRecommendationSettingsResponse getRecommendationSettings(TeamRecommendation teamRecommendation) {
        List<PlatformRecommendation> platformRecommendations =
                platformRecommendationService.findByTeamRecommendation(teamRecommendation);
        return TeamRecommendationSettingsResponse.of(teamRecommendation, platformRecommendations, objectMapper);
    }

    @Transactional
    public void resetRecommendationSettings(Team team) {
        teamRecommendationRepository.findByTeam(team)
                .ifPresent(teamRecommendation -> {
                    platformRecommendationService.deleteByTeamRecommendation(teamRecommendation);
                    teamRecommendationRepository.delete(teamRecommendation);
                });

        TeamRecommendation defaultTeamRecommendation = createDefaultTeamRecommendation(team);
        teamRecommendationRepository.save(defaultTeamRecommendation);
    }

    @Transactional
    public ManualRecommendationResponse executeManualRecommendation(ManualRecommendationRequest request) {
        Team team = teamService.getTeamByTeamId(request.teamId());
        Member systemAdmin = memberService.getMemberById(adminId);

        TeamRecommendation teamRecommendation = getTeamRecommendationByTeam(team);
        List<PlatformRecommendation> platformRecommendations = platformRecommendationService.findByTeamRecommendation(teamRecommendation);

        int totalRecommended = 0;
        List<String> createdArticleTitles = new ArrayList<>();
        StringBuilder failMessageBuilder = new StringBuilder();

        for (LocalDate date : request.selectedDates()) {
            if (articleService.isRecommendationAlreadyExists(team, date)) {
                failMessageBuilder.append(String.format("날짜 %s에 이미 추천 기록이 존재합니다.\n", date));
                continue;
            }

            List<Problem> recommendedProblems = recommendProblemsForTeam(team, platformRecommendations); // 팀 설정에 따른 문제 추천 로직 실행

            if (!recommendedProblems.isEmpty()) {
                String articleTitle = articleService.createRecommendationArticle(team, systemAdmin, recommendedProblems, date);
                recommendationHistoryService.saveRecommendationHistory(team, recommendedProblems, date);
                createdArticleTitles.add(articleTitle);
                totalRecommended += recommendedProblems.size();
            } else {
                failMessageBuilder.append(String.format("날짜 %s에 추천할 문제가 없습니다. 설정 또는 문제 데이터를 확인하세요.\n", date));
            }
        }

        String responseMessage = ResponseUtils.createRecommendationResponseMessage(totalRecommended, failMessageBuilder);
        return ManualRecommendationResponse.of(totalRecommended, createdArticleTitles, responseMessage);
    }

    public TeamRecommendation getTeamRecommendationByTeam(Team team) {
        return teamRecommendationRepository.findByTeam(team)
                .orElseThrow(TeamRecommendationNotFoundException::new);
    }

    @Transactional
    public TeamRecommendation getOrCreateTeamRecommendation(Team team) {
        return teamRecommendationRepository.findByTeam(team)
                .orElseGet(() -> {
                    TeamRecommendation newRecommendation = createDefaultTeamRecommendation(team);
                    return teamRecommendationRepository.save(newRecommendation);
                });
    }

    private TeamRecommendation createDefaultTeamRecommendation(Team team) {
        return TeamRecommendation.builder()
                .team(team)
                .autoRecommendationEnabled(false)
                .recommendationAt(9)
                .totalProblemCount(0)
                .build();
    }

    public List<Problem> recommendProblemsForTeam(Team team, List<PlatformRecommendation> platformRecommendations) {
        List<PlatformRecommendation> enabledPlatforms = platformRecommendations.stream()
                .filter(PlatformRecommendation::getEnabled)
                .toList();

        if (enabledPlatforms.isEmpty()) {
            return Arrays.stream(Platform.values())
                    .flatMap(platform -> recommendProblemsByPlatform(team, platform, 2, null, null).stream())
                    .collect(Collectors.toList());
        }

        return enabledPlatforms.stream()
                .flatMap(platformRec -> {
                    List<String> difficulties = convertUtils.parseJsonToList(platformRec.getDifficulties());
                    List<String> tags = convertUtils.parseJsonToList(platformRec.getTags());
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
        Set<Long> recommendedProblemIds = recommendationHistoryService.getRecommendedProblemIds(team, platform);
        List<Problem> allProblemsForPlatform = problemQueryService.getProblemsByPlatform(platform);

        List<Problem> availableProblems = allProblemsForPlatform.stream()
                .filter(p -> !recommendedProblemIds.contains(p.getProblemId()))
                .collect(Collectors.toList());

        List<Problem> filteredProblems = availableProblems.stream()
                .filter(p -> {
                    // 난이도 필터링 (OR 조건)
                    boolean difficultyMatch = (difficulties == null || difficulties.isEmpty())
                            || difficulties.contains(p.getDifficulty());

                    // 태그 필터링 (OR 조건) - 개선된 로직
                    boolean tagsMatch = true; // 기본값을 true로 변경
                    if (tags != null && !tags.isEmpty()) {
                        if (p.getTags() == null || p.getTags().trim().isEmpty()) {
                            tagsMatch = false;
                        } else {
                            Set<String> problemTagsSet = Arrays.stream(p.getTags().split(","))
                                    .map(String::trim)
                                    .filter(tag -> !tag.isEmpty())
                                    .collect(Collectors.toSet());

                            // 선택한 태그 중 하나라도 문제에 있으면 매치
                            tagsMatch = tags.stream()
                                    .anyMatch(problemTagsSet::contains);
                        }
                    }

                    return difficultyMatch && tagsMatch;
                })
                .collect(Collectors.toList());

        if (filteredProblems.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(filteredProblems);
        return filteredProblems.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<TeamRecommendation> getSchedulingActiveTeamRecommendations() {
        return teamRecommendationRepository.findByAutoRecommendationEnabledTrue();
    }
}
