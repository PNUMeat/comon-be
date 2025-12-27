package site.codemonster.comon.domain.teamApply.service;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.teamApply.dto.request.TeamApplyCreateRequest;
import site.codemonster.comon.domain.teamApply.dto.request.TeamApplyUpdateRequest;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitLowService;
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
@Transactional
public class TeamApplyService {

    private final TeamMemberLowService teamMemberLowService;
    private final TeamApplyLowService teamApplyLowService;
    private final TeamRecruitLowService teamRecruitLowService;

    public TeamApply createTeamApply(TeamApplyCreateRequest teamApplyCreateRequest, Member member){

        TeamRecruit foundTeamRecruit = teamRecruitLowService.findByTeamRecruitIdOrThrow(teamApplyCreateRequest.recruitmentId());

        if(foundTeamRecruit.existsTeam()){
            Team team = foundTeamRecruit.getTeam();
            teamMemberLowService.throwIfMemberAlreadyInTeam(team.getTeamId(), member);
        }

        if(!foundTeamRecruit.isRecruiting()){
            throw new TeamRecruitNotRecruitException();
        }

        TeamApply teamApply = TeamApply.builder()
                .member(member)
                .teamRecruit(foundTeamRecruit)
                .teamApplyBody(teamApplyCreateRequest.teamApplyBody())
                .build();

        return teamApplyLowService.save(teamApply);
    }

    public void deleteByTeamApplyId(Long applyId, Member member){
        TeamApply teamApply = teamApplyLowService.findTeamApplyByIdOrThrow(applyId);

        teamApplyLowService.deleteTeamApply(member, teamApply);
    }

    public void updateByTeamApplyId(Long applyId, Member member, TeamApplyUpdateRequest teamApplyUpdateRequest){
        TeamApply teamApply = teamApplyLowService.findTeamApplyByIdOrThrow(applyId);

        teamApplyLowService.updateTeamApply(member, teamApply, teamApplyUpdateRequest.teamApplyBody());
    }
}
