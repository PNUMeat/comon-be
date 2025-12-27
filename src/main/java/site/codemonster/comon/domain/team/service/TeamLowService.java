package site.codemonster.comon.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.global.error.Team.TeamNotFoundException;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class TeamLowService {

    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(TeamNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Team getTeamByTeamId(Long teamId) {
        return teamRepository.findTeamByTeamIdWithTeamRecruit(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public Page<Team> findAllWithPagination(Pageable pageable) {
        return teamRepository.findAllWithPagination(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Team> findByTeamNameContaining(String keyword, Pageable pageable) {
        return teamRepository.findByTeamNameContaining(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Team findTeamsByTeamIdWithTeamMembers(Long teamId) {
        return teamRepository.findTeamsByTeamIdWithTeamMembers(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    public void deleteById(Long teamId) {
        teamRepository.deleteById(teamId);
    }

    @Transactional(readOnly = true)
    public List<Team> findByTeamManagerId(Long memberId) {
        return teamRepository.findByTeamManagerId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Team findTeamByTeamIdWithTeamRecruit(Long teamId) {
        return teamRepository.findTeamByTeamIdWithTeamRecruit(teamId).orElseThrow(TeamNotFoundException::new);
    }

}
