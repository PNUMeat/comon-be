package site.codemonster.comon.domain.teamApply.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.global.error.TeamApply.TeamApplyDeleteForbiddenException;
import site.codemonster.comon.global.error.TeamApply.TeamApplyNotFoundException;
import site.codemonster.comon.global.error.TeamApply.TeamApplyUpdateForbiddenException;

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

    public TeamApply save(TeamApply teamApply){
        return teamApplyRepository.save(teamApply);
    }

    public TeamApply findTeamApplyByIdOrThrow(Long teamApplyId){
        return teamApplyRepository.findById(teamApplyId)
                .orElseThrow(TeamApplyNotFoundException::new);
    }

    @Transactional
    public void deleteTeamApply(Member member, TeamApply teamApply){
        TeamRecruit teamRecruit = teamApply.getTeamRecruit();

        if(!teamApply.isTeamApplyOwner(member) && !teamRecruit.isTeamRecruitOwner(member)){
            throw new TeamApplyDeleteForbiddenException();
        }

        teamApply.getTeamRecruit().getTeamApplies().remove(teamApply);
        teamApplyRepository.deleteById(teamApply.getTeamApplyId());
    }

    @Transactional
    public void updateTeamApply(Member member, TeamApply teamApply, String teamApplyBody) {
        if (!teamApply.isTeamApplyOwner(member)) {
            throw new TeamApplyUpdateForbiddenException();
        }

        teamApply.updateTeamApplyBody(teamApplyBody);
    }

    public void deleteTeamAppliesByTeamRecruitIds(List<Long> teamRecruitIds){
        teamApplyRepository.deleteTeamAppliesByTeamRecruitIds(teamRecruitIds);
    }

    public void deleteTeamAppliesByMemberId(Long memberId) {
        teamApplyRepository.deleteTeamAppliesByMemberId(memberId);
    }
}
