package site.codemonster.comon.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.global.error.Team.TeamNotFoundException;

@RequiredArgsConstructor
@Transactional
@Service
public class TeamLowService {

    private final TeamRepository teamRepository;

    public Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(TeamNotFoundException::new);
    }

    public Team findByTeamIdWithTeamRecommendation(Long teamId) {
        return teamRepository.findByTeamIdWithTeamRecommendation(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }
}
