package site.codemonster.comon.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.domain.problem.dto.response.ProblemResponse;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.ProblemQueryService;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
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
@Transactional
public class TeamRecommendationLowService {

    private final TeamRecommendationRepository teamRecommendationRepository;

    public TeamRecommendation findByTeam(Team team) {
        return teamRecommendationRepository.findByTeam(team)
                .orElseThrow(TeamRecommendationNotFoundException::new);
    }

    public boolean isExistByTeam(Team team) {
        return teamRecommendationRepository.existsByTeam(team);
    }

    public List<TeamRecommendation> findAllWithRecommendationDays() {
        return teamRecommendationRepository.findAllWithRecommendationDays();
    }

    public TeamRecommendation save(TeamRecommendationRequest request, Team team) {

        TeamRecommendation teamRecommendation = new TeamRecommendation(team, request.recommendationAt());

        return teamRecommendationRepository.save(teamRecommendation);
    }

    public void deleteByTeamId(Long teamId) {
        teamRecommendationRepository.deleteByTeamId(teamId);
    }
}
