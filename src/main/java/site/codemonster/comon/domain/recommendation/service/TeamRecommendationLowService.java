package site.codemonster.comon.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationRepository;
import site.codemonster.comon.domain.team.entity.Team;

import java.util.*;

import site.codemonster.comon.global.error.recommendation.TeamRecommendationNotFoundException;

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
