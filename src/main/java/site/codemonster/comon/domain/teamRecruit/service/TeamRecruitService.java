package site.codemonster.comon.domain.teamRecruit.service;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamLowService;
import site.codemonster.comon.domain.teamApply.repository.TeamApplyRepository;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitCreateRequest;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitInviteRequest;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitUpdateRequest;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitImageRepository;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitRepository;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitDuplicateException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotAuthorException;
import site.codemonster.comon.global.error.TeamRecruit.TeamRecruitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitService {

    private final TeamRecruitImageRepository teamRecruitImageRepository;
    private final TeamApplyRepository teamApplyRepository;
    private final TeamLowService teamLowService;
    private final TeamMemberService teamMemberService;
    private final MemberService memberService;
    private final TeamRecruitLowService teamRecruitLowService;

    @Transactional
    public TeamRecruit createTeamRecruit(TeamRecruitCreateRequest teamRecruitCreateRequest, Member member){

        Team team = null;
        if (teamRecruitCreateRequest.teamId() != null){
            team = teamLowService.getTeamByTeamId(teamRecruitCreateRequest.teamId());
            TeamMember teamMember = teamMemberService.getTeamMemberByTeamIdAndMemberId(team.getTeamId(), member);
            teamMemberService.checkMemberIsTeamManagerOrThrow(teamMember);
        }

        if (team != null && team.getTeamRecruit() != null) throw new TeamRecruitDuplicateException();

        TeamRecruit teamRecruit = new TeamRecruit(team, member, teamRecruitCreateRequest.teamRecruitTitle(), teamRecruitCreateRequest.teamRecruitBody(), teamRecruitCreateRequest.chatUrl());

        return teamRecruitLowService.save(teamRecruit);
    }

    @Transactional
    public void invite(TeamRecruitInviteRequest teamRecruitInviteRequest, Member member) {
        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdOrThrow(teamRecruitInviteRequest.recruitId());
        isAuthorOrThrow(teamRecruit, member);

        List<String> memberUuids = teamRecruitInviteRequest.memberUuids();
        List<Member> applyMembers = new ArrayList<>();
        for (String memberUuid : memberUuids) {
            applyMembers.add(memberService.getMemberByUUID(memberUuid));
        }

        Team team = teamLowService.getTeamByTeamId(teamRecruitInviteRequest.teamId());
        teamMemberService.inviteTeamMember(team, applyMembers, teamRecruit);
    }

    @Transactional
    public void changeTeamRecruitStatus(Long teamRecruitId, Member member){
        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdWithMemberOrThrow(teamRecruitId);

        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        if(teamRecruit.isRecruiting()){
            teamApplyRepository.deleteTeamAppliesByTeamRecruitId(teamRecruitId);
        }

        teamRecruit.changeRecruitingStatus();
    }

    @Transactional
    public void updateTeamRecruit(Long teamRecruitId, Member member, TeamRecruitUpdateRequest teamRecruitUpdateRequest){
        TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdWithMemberOrThrow(teamRecruitId);

        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }

        teamRecruit.updateTeamRecruit(teamRecruitUpdateRequest);
    }


    public void isAuthorOrThrow(TeamRecruit teamRecruit, Member member){
        if(!teamRecruit.isAuthor(member)){
            throw new TeamRecruitNotAuthorException();
        }
    }
}
