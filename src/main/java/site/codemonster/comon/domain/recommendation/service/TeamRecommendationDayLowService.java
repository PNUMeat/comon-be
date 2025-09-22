package site.codemonster.comon.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendationDay;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationDayRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
public class TeamRecommendationDayLowService {

    private final TeamRecommendationDayRepository teamRecommendationDayRepository;

    public List<TeamRecommendationDay> saveAll(TeamRecommendation teamRecommendation, Set<DayOfWeek> dayOfWeeks) {

        List<TeamRecommendationDay> teamRecommendationDays = dayOfWeeks
                .stream()
                .map(dayOfWeek -> new TeamRecommendationDay(dayOfWeek, teamRecommendation))
                .toList();

        return teamRecommendationDayRepository.saveAll(teamRecommendationDays);
    }

    public void deleteByTeamRecommendationId(Long teamRecommendationId) {
            teamRecommendationDayRepository.deleteByTeamRecommendationId(teamRecommendationId);
    }
}
