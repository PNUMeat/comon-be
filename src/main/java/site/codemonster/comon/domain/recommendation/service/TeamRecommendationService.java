package site.codemonster.comon.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.service.ProblemQueryService;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.response.PlatformRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationResponse;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendationDay;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamLowService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.global.error.recommendation.TeamRecommendationDuplicateException;
import site.codemonster.comon.global.error.recommendation.TeamRecommendationProblemShortageException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecommendationService {

    private final TeamLowService teamLowService;
    private final TeamRecommendationLowService teamRecommendationLowService;
    private final TeamRecommendationDayLowService teamRecommendationDayLowService;
    private final PlatformRecommendationLowService platformRecommendationLowService;
    private final RecommendationHistoryLowService recommendationHistoryLowService;
    private final ProblemQueryService problemQueryService;
    private final ArticleService articleService;
    private final TeamMemberService teamMemberService;

    @Transactional
    public TeamRecommendation saveRecommendationSettings(TeamRecommendationRequest teamRecommendationRequest) {
        Long teamId = teamRecommendationRequest.teamId();
        Team findTeam = teamLowService.findById(teamId);

        if(teamRecommendationLowService.isExistByTeam(findTeam)) // 이미 존재하는 추천이 있으면 throw
            throw new TeamRecommendationDuplicateException();

        TeamRecommendation savedTeamRecommendation = teamRecommendationLowService.save(teamRecommendationRequest, findTeam);

        // 추천 요일 저장
        teamRecommendationDayLowService.saveAll(savedTeamRecommendation, teamRecommendationRequest.recommendDays());
        // 추천 플랫폼 저장
        platformRecommendationLowService.saveAll(teamRecommendationRequest.platformRecommendationRequests(), savedTeamRecommendation);

        return savedTeamRecommendation;
    }

    // 기존 팀 추천 조회
    public TeamRecommendationResponse getRecommendationSettings(Long teamId) {

        // 추천할 팀을 TeamRecommendation과 함께 fetch join해서 조회
        Team team = teamLowService.findByTeamIdWithTeamRecommendation(teamId);

        TeamRecommendation teamRecommendation = team.getTeamRecommendation();

        Set<DayOfWeek> recommendDays = teamRecommendation.getTeamRecommendationDays().stream().
                map(TeamRecommendationDay::getDayOfWeek)
                .collect(Collectors.toSet());

        List<PlatformRecommendationResponse> recommendationResponses = teamRecommendation.getPlatformRecommendations()
                .stream().map(PlatformRecommendationResponse::new).toList();

        return new TeamRecommendationResponse(teamRecommendation.getRecommendationAt(),
                recommendDays, recommendationResponses);
    }


    // 팀 추천 삭제
    @Transactional
    public void deleteTeamRecommendation(Long teamId) {

        Team team = teamLowService.findById(teamId);

        platformRecommendationLowService.deleteByTeamRecommendationId(team.getTeamRecommendation().getId());
        teamRecommendationDayLowService.deleteByTeamRecommendationId(team.getTeamRecommendation().getId());
        teamRecommendationLowService.deleteByTeamId(team.getTeamId());
    }


    @Transactional
    public ManualRecommendationResponse executeManualRecommendation(ManualRecommendationRequest request) {

        // 추천할 팀을 TeamRecommendation과 함께 fetch join해서 조회
        Team team = teamLowService.findByTeamIdWithTeamRecommendation(request.teamId());

        // 추천할 날짜 조회
        List<LocalDate> historyDates = recommendationHistoryLowService.findByTeamId(team.getTeamId())
                .stream().map(RecommendationHistory::getRecommendedAt).toList();

        // 이미 추천되었던 날이 있으면 예외 반환
        request.selectedDates()
                .forEach(selectedDate -> {
                    if(historyDates.contains(selectedDate))
                        throw new TeamRecommendationDuplicateException();
                });

        List<String> createdArticleTitles = new ArrayList<>();

        // 추천 실행
        request.selectedDates()
                        .forEach(selectedDate -> createdArticleTitles.add(executeRecommendation(team.getTeamRecommendation(), selectedDate)));


        return ManualRecommendationResponse.of(createdArticleTitles.size(), createdArticleTitles);

    }

    @Transactional
    public String executeRecommendation(TeamRecommendation teamRecommendation, LocalDate selectedDate) {

        List<PlatformRecommendation> platformRecommendations = teamRecommendation.getPlatformRecommendations();

        // 이미 사용한 Problem의 PK들
        List<Long> excludedProblemIds = recommendationHistoryLowService.findByTeamId(teamRecommendation.getTeam().getTeamId())
                .stream().map(recommendationHistory -> recommendationHistory.getProblem().getProblemId()).toList();

        // 추천 가능한 문제들
        List<Problem> recommendationProblems = new ArrayList<>();

        // 추천해야하는 문제 개수
        int totalProblemCount = 0;

        for (PlatformRecommendation platformRecommendation : platformRecommendations) {
            recommendationProblems.addAll(problemQueryService
                    .findRecommendationProblem(excludedProblemIds, platformRecommendation));
            totalProblemCount += platformRecommendation.getProblemCount();
        }

        // 추천해야하는 문제 개수보다 적다면 예외 반환
        if (recommendationProblems.size() < totalProblemCount)
            throw new TeamRecommendationProblemShortageException();

        // 문제 추천할 TeamManager 아무나 한 명 조회
        TeamMember findTeamManager = teamMemberService.getTeamManagerByTeamId(teamRecommendation.getTeam().getTeamId());

        // 추천 글 생성
        String articleTitle = articleService.createRecommendationArticle(
                   teamRecommendation.getTeam(), findTeamManager.getMember(), recommendationProblems, selectedDate);

        // 추천 기록 저장
        List<RecommendationHistory> recommendationHistories = recommendationProblems.stream()
                .map(problem -> new RecommendationHistory(teamRecommendation.getTeam(), problem, selectedDate))
                .toList();


        recommendationHistoryLowService.saveAll(recommendationHistories);

        return articleTitle;
    }

}
