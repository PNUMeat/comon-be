package site.codemonster.comon.domain.teamApply.service;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamApply.dto.request.TeamApplyCreateRequest;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.global.error.TeamApply.TeamApplyDeleteForbiddenException;
import site.codemonster.comon.global.error.TeamApply.TeamApplyNotFoundException;
import site.codemonster.comon.global.error.TeamApply.TeamApplyUpdateForbiddenException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotRecruitException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamApplyService {
    private final TeamApplyRepository teamApplyRepository;

    @Transactional
    public TeamApply createTeamApply(TeamApplyCreateRequest teamApplyCreateRequest, TeamRecruit teamRecruit, Member member){
        if(!teamRecruit.isRecruiting()){
            throw new TeamRecruitNotRecruitException();
        }

        TeamApply teamApply = TeamApply.builder()
                .member(member)
                .teamRecruit(teamRecruit)
                .teamApplyBody(teamApplyCreateRequest.teamApplyBody())
                .build();

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
}
