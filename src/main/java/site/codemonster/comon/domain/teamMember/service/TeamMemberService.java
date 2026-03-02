package site.codemonster.comon.domain.teamMember.service;

import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.service.ArticleImageLowService;
import site.codemonster.comon.domain.article.service.ArticleLowService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberLowService;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamLowService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitLowService;
import site.codemonster.comon.global.error.Team.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService {
    private final ArticleLowService articleLowService;
    private final ArticleImageLowService articleImageLowService;
    private final MemberLowService memberLowService;
    private final TeamLowService teamLowService;
    private final TeamRecruitLowService teamRecruitLowService;
    private final TeamMemberLowService teamMemberLowService;


    public void removeTeamMember(Member member, Long teamId){
        if(!teamMemberLowService.existsByTeamIdAndMemberId(teamId, member)){
            throw new TeamMemberInvalidException();
        }

        teamMemberLowService.deleteByTeamTeamIdAndMember(teamId, member);

        // 내가 쓴 팀 모집글 삭제
        deleteTeamRecruit(teamId, member);

        // 내가 쓴 글 삭제
        articleLowService.deleteByMemberIdAndTeamId(member.getId(), teamId);
    }

    public Boolean addTeamManager(Long teamId, Member manager, String memberUUID){
        if(!teamMemberLowService.checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        Member notManager = memberLowService.getMemberByUUID(memberUUID);

        TeamMember teamMemberOfNotManager = teamMemberLowService.getTeamMemberByTeamIdAndMemberId(teamId, notManager);

        if(teamMemberOfNotManager.getIsTeamManager()){
            throw new AlreadyTeamManagerException();
        }

        return teamMemberOfNotManager.updateTeamManagerStatus(true);
    }

    public Boolean transferTeamManager(Long teamId, Member manager, String memberUUID){
        TeamMember teamMemberOfManager = teamMemberLowService.getTeamMemberByTeamIdAndMemberId(teamId, manager);

        if(!teamMemberOfManager.getIsTeamManager()){
            throw new TeamManagerInvalidException();
        }

        Member notManager = memberLowService.getMemberByUUID(memberUUID);

        TeamMember teamMemberOfNotManager = teamMemberLowService.getTeamMemberByTeamIdAndMemberId(teamId, notManager);

        if(teamMemberOfNotManager.getIsTeamManager()){
            throw new AlreadyTeamManagerException();
        }

        deleteTeamRecruit(teamId, manager);

        teamMemberOfManager.updateTeamManagerStatus(false);
        return teamMemberOfNotManager.updateTeamManagerStatus(true);
    }

    public Boolean demoteTeamManager(Long teamId, Member manager, String memberUUID){
        if(!teamMemberLowService.checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        Member notManager = memberLowService.getMemberByUUID(memberUUID);

        TeamMember teamMemberOfNotManager = teamMemberLowService.getTeamMemberByTeamIdAndMemberId(teamId, notManager);

        if(!teamMemberOfNotManager.getIsTeamManager()){
            throw new TeamManagerInvalidException();
        }

        deleteTeamRecruit(teamId, notManager);

        return teamMemberOfNotManager.updateTeamManagerStatus(false);
    }

    public void removeMemberByManger(Long teamId, Member manager, String memberUUID){
        if(!teamMemberLowService.checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        Member removedMember = memberLowService.getMemberByUUID(memberUUID);

        removeTeamMember(removedMember, teamId);
    }

    public void deleteTeamRecruit(Long teamId, Member member) {
        Team team = teamLowService.findTeamByTeamIdWithTeamRecruit(teamId);

        TeamRecruit teamRecruit = team.getTeamRecruit();
        if(teamRecruit != null && team.getTeamRecruit().isTeamRecruitOwner(member)){
            teamRecruitLowService.forceDeleteTeamRecruit(teamRecruit.getTeamRecruitId());
        }
    }
}
