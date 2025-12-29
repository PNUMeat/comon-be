package site.codemonster.comon.domain.team.service;

import site.codemonster.comon.domain.article.repository.ArticleImageRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.article.service.ArticleImageLowService;
import site.codemonster.comon.domain.article.service.ArticleLowService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberLowService;
import site.codemonster.comon.domain.recommendation.service.RecommendationHistoryLowService;
import site.codemonster.comon.domain.recommendation.service.TeamRecommendationHighService;
import site.codemonster.comon.domain.team.dto.request.TeamInfoEditRequest;
import site.codemonster.comon.domain.team.dto.request.TeamCreateRequest;
import site.codemonster.comon.domain.team.dto.response.MyTeamMyPageResponse;
import site.codemonster.comon.domain.team.dto.response.TeamMemberResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.enums.Topic;
import site.codemonster.comon.domain.teamApply.service.TeamApplyLowService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitLowService;
import site.codemonster.comon.global.error.Team.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {

    private final TeamLowService teamLowService;
    private final MemberLowService memberLowService;
    private final TeamMemberLowService teamMemberLowService;
    private final ArticleLowService articleLowService;
    private final ArticleImageLowService articleImageLowService;
    private final TeamRecruitLowService teamRecruitLowService;
    private final TeamRecommendationHighService teamRecommendationHighService;
    private final RecommendationHistoryLowService recommendationHistoryLowService;
    private final TeamApplyLowService teamApplyLowService;

    public Team createTeam(TeamCreateRequest teamRequest, Member manager) {

        List<String> memberUuids = teamRequest.teamMemberUuids();

        List<Member> applyMembers = new ArrayList<>();
        if(memberUuids != null){
            for (String memberUuid : memberUuids) {
                applyMembers.add(memberLowService.getMemberByUUID(memberUuid));
            }
        }

        if(teamRequest.teamRecruitId() != null){
            TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdOrThrow(teamRequest.teamRecruitId());
            teamRecruitLowService.isAuthorOrThrow(teamRecruit, manager);
        }

        Team team = new Team(teamRequest.teamName(), Topic.fromName(teamRequest.topic()), teamRequest.teamExplain(), teamRequest.memberLimit(), teamRequest.password());

        Team savedTeam = teamLowService.save(team);

		if (teamRequest.teamIconUrl() != null){ // null이라면 기본 이미지!
			team.updateTeamIconUrl(S3ImageUtil.convertImageUrlToObjectKey(teamRequest.teamIconUrl()));
		}

        teamMemberLowService.saveTeamMember(savedTeam, manager, true);
        for (Member applyMember : applyMembers) {
            teamMemberLowService.saveTeamMember(savedTeam, applyMember, false);
        }

        if(teamRequest.teamRecruitId() != null){
            TeamRecruit teamRecruit = teamRecruitLowService.findByTeamRecruitIdOrThrow(teamRequest.teamRecruitId());
            teamRecruit.addTeam(savedTeam);
            teamApplyLowService.deleteTeamApplyAfterTeamMake(teamRecruit);
        }

        return savedTeam;
    }

    @Transactional(readOnly = true)
    public Page<Team> getAllTeamsUsingPaging(Pageable pageable){
        return teamLowService.findAllWithPagination(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Team> getAllTeamsByKeywordUsingPaging(Pageable pageable, String keyword){
        return teamLowService.findByTeamNameContaining(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<Team> getMyTeams(Member member){
        List<TeamMember> teamMembers = teamMemberLowService.getTeamMembersByMember(member);
        return teamMembers.stream()
                .map(TeamMember::getTeam)
                .collect(Collectors.toList());
    }

    public TeamMember joinTeam(Member member, String password, Long teamId){
        Team team = teamLowService.findTeamsByTeamIdWithTeamMembers(teamId);

        validatePassword(password, team);
        validateTeamMembership(team, member);

        if(team.checkExceedTeamSize()){
            throw new ExceedMaxMembersException();
        }

        return teamMemberLowService.saveTeamMember(team, member, false);
    }

    public Team updateTeamAnnouncement(Member member, String teamAnnouncement, Long teamId){
        Team team = teamLowService.getTeamByTeamId(teamId);

        if(!teamMemberLowService.checkMemberIsTeamManager(teamId, member)) {
            throw new TeamManagerInvalidException();
        }

        team.updateTeamAnnouncement(teamAnnouncement);

        return team;
    }

    public void deleteTeamByOwner(Member member, Long teamId) {

        Team team = teamLowService.findById(teamId);

        if(!teamMemberLowService.checkMemberIsTeamManager(team.getTeamId(), member)){
            throw new TeamMemberInvalidException();
        }

        TeamRecruit teamRecruit = team.getTeamRecruit();
        if(teamRecruit != null){
            teamRecruitLowService.forceDeleteTeamRecruit(teamRecruit.getTeamRecruitId());
        }

        recommendationHistoryLowService.deleteByTeamId(team.getTeamId());

        teamRecommendationHighService.deleteTeamRecommendation(team.getTeamId());

        articleImageLowService.deleteByTeamTeamId(team.getTeamId());

        articleLowService.deleteByTeamTeamId(team.getTeamId());

        teamMemberLowService.deleteTeamMemberByTeamId(team.getTeamId());

        teamLowService.deleteById(team.getTeamId());

    }

    @Transactional(readOnly = true)
    public List<Team> getTeamsManagedByMember(Long memberId) {

        return teamLowService.findByTeamManagerId(memberId);
    }

    public Team updateTeamInfo(TeamInfoEditRequest teamInfoEditRequest, Member member, Long teamId){
        Team team = teamLowService.findTeamsByTeamIdWithTeamMembers(teamId);

        if(!teamMemberLowService.checkMemberIsTeamManager(teamId, member)){
            throw new TeamManagerInvalidException();
        }

        if(teamInfoEditRequest.memberLimit() < team.getTeamMembers().size()){
            throw new MemberLimitBelowCurrentException();
        }

        team.updateTeamInfo(teamInfoEditRequest);

        if (teamInfoEditRequest.teamIconUrl() != null){
            team.updateTeamIconUrl(S3ImageUtil.convertImageUrlToObjectKey(teamInfoEditRequest.teamIconUrl()));
        }

        return team;
    }

    @Transactional(readOnly = true)
    public Team getTeamInfo(Long teamId, Member member){
        Team team = teamLowService.getTeamByTeamId(teamId);

        if(!teamMemberLowService.checkMemberIsTeamManager(teamId, member)){
            throw new TeamManagerInvalidException();
        }

        return team;
    }


    private void validatePassword(String password, Team team) {
        if (!password.equals(team.getTeamPassword())) {
            throw new TeamPasswordInvalidException();
        }
    }

    private void validateTeamMembership(Team team, Member member) {
        if (teamMemberLowService.existsByTeamIdAndMemberId(team.getTeamId(), member)) {
            throw new TeamAlreadyJoinException();
        }
    }

    @Transactional(readOnly = true)
    public List<Team> getAllTeams() {
        return teamLowService.findAll();
    }

    @Transactional(readOnly = true)
    public List<MyTeamMyPageResponse> findMyTeamAtMyPage(Member member) {
        List<TeamMember> teamMembers =  teamMemberLowService.getTeamMemberAndTeamByMember(member);

        return teamMembers.stream()
                .map(MyTeamMyPageResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> findTeamMemberResponseByTeamId(Long teamId, Member member) {

        return teamMemberLowService.getTeamMembersByTeamId(teamId, member)
                .stream()
                .map(TeamMemberResponse::new)
                .toList();
    }
}
