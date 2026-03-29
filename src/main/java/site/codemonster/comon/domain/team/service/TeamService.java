package site.codemonster.comon.domain.team.service;

import site.codemonster.comon.domain.article.service.ArticleLowService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberLowService;
import site.codemonster.comon.domain.recommendation.service.RecommendationHistoryLowService;
import site.codemonster.comon.domain.recommendation.service.TeamRecommendationHighService;
import site.codemonster.comon.domain.team.dto.request.TeamInfoEditRequest;
import site.codemonster.comon.domain.team.dto.request.TeamCreateRequest;
import site.codemonster.comon.domain.team.dto.response.*;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {

    private final TeamLowService teamLowService;
    private final MemberLowService memberLowService;
    private final TeamMemberLowService teamMemberLowService;
    private final ArticleLowService articleLowService;
    private final TeamRecruitLowService teamRecruitLowService;
    private final TeamRecommendationHighService teamRecommendationHighService;
    private final RecommendationHistoryLowService recommendationHistoryLowService;
    private final TeamApplyLowService teamApplyLowService;

    public TeamCreateResponse createTeam(TeamCreateRequest teamRequest, Member manager) {

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

        return TeamCreateResponse.of(savedTeam);
    }

    @Transactional(readOnly = true)
    public Page<TeamAllResponse> getAllTeamsUsingPaging(Pageable pageable){
        Page<Team> teams = teamLowService.findAllWithPagination(pageable);
        Map<Long, Long> solveCountMap = getSolveCountMap(teams.getContent());
        return teams.map(team -> new TeamAllResponse(team, solveCountMap.getOrDefault(team.getTeamId(), 0L)));
    }

    @Transactional(readOnly = true)
    public Page<TeamAllResponse> getAllTeamsByKeywordUsingPaging(Pageable pageable, String keyword){
        Page<Team> teams = teamLowService.findByTeamNameContaining(keyword, pageable);
        Map<Long, Long> solveCountMap = getSolveCountMap(teams.getContent());
        return teams.map(team -> new TeamAllResponse(team, solveCountMap.getOrDefault(team.getTeamId(), 0L)));
    }

    @Transactional(readOnly = true)
    public List<MyTeamResponse> getMyTeams(Member member){
        List<Team> myTeams = teamMemberLowService.getTeamMembersByMember(member).stream()
                .map(TeamMember::getTeam)
                .collect(Collectors.toList());
        Map<Long, Long> solveCountMap = getSolveCountMap(myTeams);
        return myTeams.stream()
                .map(team -> MyTeamResponse.of(team, solveCountMap.getOrDefault(team.getTeamId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamCombinedResponse getCombinedTeamsInfo(Pageable pageable, Member member){
        Page<Team> teams = teamLowService.findAllWithPagination(pageable);
        List<Team> myTeams = teamMemberLowService.getTeamMembersByMember(member).stream()
                .map(TeamMember::getTeam)
                .collect(Collectors.toList());

        List<Long> distinctTeamIds = Stream.concat(
                teams.getContent().stream().map(Team::getTeamId),
                myTeams.stream().map(Team::getTeamId)
        ).distinct().toList();

        Map<Long, Long> solveCountMap = articleLowService.countCodingTestByTeamIds(distinctTeamIds);

        Page<TeamAllResponse> teamAllResponses = teams.map(team ->
                new TeamAllResponse(team, solveCountMap.getOrDefault(team.getTeamId(), 0L)));
        List<MyTeamResponse> myTeamResponses = myTeams.stream()
                .map(team -> MyTeamResponse.of(team, solveCountMap.getOrDefault(team.getTeamId(), 0L)))
                .collect(Collectors.toList());

        return new TeamCombinedResponse(myTeamResponses, teamAllResponses);
    }

    public TeamJoinResponse joinTeam(Member member, String password, Long teamId){
        Team team = teamLowService.findTeamsByTeamIdWithTeamMembers(teamId);

        validatePassword(password, team);
        validateTeamMembership(team, member);

        if(team.checkExceedTeamSize()){
            throw new ExceedMaxMembersException();
        }

        TeamMember teamMember = teamMemberLowService.saveTeamMember(team, member, false);
        return TeamJoinResponse.of(teamMember);
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

        articleLowService.deleteByTeamId(team.getTeamId());

        teamMemberLowService.deleteTeamMemberByTeamId(team.getTeamId());

        teamLowService.deleteById(team.getTeamId());

    }

    @Transactional(readOnly = true)
    public List<Team> getTeamsManagedByMember(Long memberId) {

        return teamLowService.findByTeamManagerId(memberId);
    }

    public TeamInfoResponse updateTeamInfo(TeamInfoEditRequest teamInfoEditRequest, Member member, Long teamId){
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

        return new TeamInfoResponse(team);
    }

    @Transactional(readOnly = true)
    public TeamInfoResponse getTeamInfo(Long teamId, Member member){
        Team team = teamLowService.getTeamByTeamId(teamId);

        if(!teamMemberLowService.checkMemberIsTeamManager(teamId, member)){
            throw new TeamManagerInvalidException();
        }

        return new TeamInfoResponse(team);
    }


    private Map<Long, Long> getSolveCountMap(List<Team> teams) {
        List<Long> teamIds = teams.stream().map(Team::getTeamId).toList();
        return articleLowService.countCodingTestByTeamIds(teamIds);
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
