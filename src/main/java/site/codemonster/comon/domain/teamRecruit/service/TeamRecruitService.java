package site.codemonster.comon.domain.teamRecruit.service;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberLowService;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamLowService;
import site.codemonster.comon.domain.teamApply.dto.response.TeamApplyMemberResponse;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamApply.service.TeamApplyLowService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitCreateRequest;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitInviteRequest;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitUpdateRequest;
import site.codemonster.comon.domain.teamRecruit.dto.response.TeamRecruitGetResponse;
import site.codemonster.comon.domain.teamRecruit.dto.response.TeamRecruitParticularResponse;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.global.error.Team.TeamAlreadyJoinException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitDuplicateException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotAuthorException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamRecruitService {

    private final TeamApplyLowService teamApplyLowService;
    private final TeamLowService teamLowService;
    private final TeamMemberLowService teamMemberLowService;
    private final MemberLowService memberLowService;
    private final TeamRecruitLowService teamRecruitLowService;

    public TeamRecruit createTeamRecruit(TeamRecruitCreateRequest teamRecruitCreateRequest, Member member){

        Team team = null;
        if (teamRecruitCreateRequest.teamId() != null){
            team = teamLowService.getTeamByTeamId(teamRecruitCreateRequest.teamId());
            TeamMember teamMember = teamMemberLowService.getTeamMemberByTeamIdAndMemberId(team.getTeamId(), member);
            teamMemberLowService.checkMemberIsTeamManagerOrThrow(teamMember);
        }

        if (team != null && team.getTeamRecruit() != null) throw new TeamRecruitDuplicateException();

        TeamRecruit teamRecruit = new TeamRecruit(team, member, teamRecruitCreateRequest.teamRecruitTitle(), teamRecruitCreateRequest.teamRecruitBody(), teamRecruitCreateRequest.chatUrl());

        return teamRecruitLowService.save(teamRecruit);
    }

    public void invite(TeamRecruitInviteRequest teamRecruitInviteRequest, Member member) {
        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdOrThrow(teamRecruitInviteRequest.recruitId());
        teamRecruitLowService.isAuthorOrThrow(teamRecruit, member);

        List<String> memberUuids = teamRecruitInviteRequest.memberUuids();
        List<Member> applyMembers = new ArrayList<>();
        for (String memberUuid : memberUuids) {
            applyMembers.add(memberLowService.getMemberByUUID(memberUuid));
        }

        Team team = teamLowService.getTeamByTeamId(teamRecruitInviteRequest.teamId());

        for (Member applyMember : applyMembers) {
            if(teamMemberLowService.existsByTeamIdAndMemberId(team.getTeamId(), applyMember)){
                throw new TeamAlreadyJoinException();
            }

            teamMemberLowService.saveTeamMember(team, applyMember, false);
        }

        teamApplyLowService.deleteTeamApplyAfterTeamMake(teamRecruit);
    }

    public void changeTeamRecruitStatus(Long teamRecruitId, Member member){
        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdWithMemberOrThrow(teamRecruitId);

        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        teamRecruit.changeRecruitingStatus(); // 영속성 컨텍스트 지워지기 전에 변경

        if(!teamRecruit.isRecruiting()){
            teamApplyLowService.deleteTeamAppliesByTeamRecruitId(teamRecruitId);
        }
    }

    public Page<TeamRecruitGetResponse> findTeamRecruitmentWithPage(Pageable pageable, String status) {

        Page<TeamRecruit> teamRecruitmentsUsingPaging = teamRecruitLowService.getTeamRecruitmentsUsingPaging(pageable, status);

        return teamRecruitmentsUsingPaging.map(TeamRecruitGetResponse::of);
    }


    public void updateTeamRecruit(Long teamRecruitId, Member member, TeamRecruitUpdateRequest teamRecruitUpdateRequest){
        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdWithMemberOrThrow(teamRecruitId);

        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        teamRecruit.updateTeamRecruit(teamRecruitUpdateRequest);


    }

    @Transactional(readOnly = true)
    public TeamRecruitParticularResponse findTeamRecruitParticular(Long recruitId, Member member){

        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdWithMemberOrThrow(recruitId);
        List<TeamApply> teamApplies = teamApplyLowService.getTeamApplies(teamRecruit, member);

        List<String> teamMemberUuids = List.of();
        if(teamRecruit.isAuthor(member)){
            teamMemberUuids = TeamApplyMemberResponse.of(teamApplies);
        }

        return TeamRecruitParticularResponse.from(
                teamRecruit,
                teamApplies,
                member,
                teamMemberUuids
        );
    }

    public void deleteByRecruitId(Long recruitId,Member member){
        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdOrThrow(recruitId);
        teamRecruitLowService.deleteTeamRecruit(teamRecruit, member);
    }
}
