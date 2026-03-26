package site.codemonster.comon.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.repository.RecommendationHistoryRepository;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationDayRepository;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.global.error.Team.TeamNotFoundException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
public class TeamLowService {

    private final TeamRepository teamRepository;
    private final TeamRecommendationRepository teamRecommendationRepository;
    private final TeamRecommendationDayRepository teamRecommendationDayRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final RecommendationHistoryRepository recommendationHistoryRepository;

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

        Optional<TeamRecommendation> deleteTeamRecommendation = teamRecommendationRepository.findByTeamId(teamId);

        if (deleteTeamRecommendation.isPresent()) {
            teamRecommendationDayRepository.deleteByTeamRecommendationId(deleteTeamRecommendation.get().getId());
        }

        teamRecommendationRepository.deleteByTeamId(teamId);

        recommendationHistoryRepository.deleteByTeamTeamId(teamId);
        teamMemberRepository.deleteByTeamTeamId(teamId);
        teamRepository.deleteById(teamId);
    }

    @Transactional(readOnly = true)
    public List<Team> findByTeamManagerId(Long memberId) {
        return teamRepository.findByTeamManagerId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Team> findByTeamMangerId(Long memberId) {
        return teamRepository.findByTeamMangerId(memberId);
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
