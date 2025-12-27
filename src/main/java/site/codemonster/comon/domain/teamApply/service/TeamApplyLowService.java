package site.codemonster.comon.domain.teamApply.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class TeamApplyLowService {

    private final TeamApplyRepository teamApplyRepository;

    @Transactional(readOnly = true)
    public List<TeamApply> getTeamApplies(TeamRecruit teamRecruit, Member member){
        return teamApplyRepository.findTeamAppliesWithAuthorFirst(teamRecruit.getTeamRecruitId(), member.getId());
    }

    public void deleteTeamApplyAfterTeamMake(TeamRecruit teamRecruit){
        teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruit.getTeamRecruitId());
    }

    public void deleteTeamAppliesByTeamRecruitId(Long teamRecruitId){
        teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruitId);
    }
}
