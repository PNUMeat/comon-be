package site.codemonster.comon.domain.team.service;

import site.codemonster.comon.domain.article.repository.ArticleImageRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.dto.request.TeamInfoEditRequest;
import site.codemonster.comon.domain.team.dto.request.TeamRequest;
import site.codemonster.comon.domain.team.dto.response.TeamInfoResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.enums.Topic;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamApply.service.TeamApplyService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitService;
import site.codemonster.comon.global.error.Team.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import site.codemonster.comon.global.util.convertUtils.ImageFieldConvertUtils;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberService teamMemberService;
    private final ArticleRepository articleRepository;
    private final ArticleImageRepository articleImageRepository;
    private final TeamRecruitService teamRecruitService;
    private final TeamApplyService teamApplyService;
    private final ImageFieldConvertUtils imageFieldConvertUtils;

    @Transactional
    public Team createTeam(TeamRequest teamRequest, Member manager, List<Member> applyMembers, Long teamRecruitId) {
        Team team = Team.builder()
                .teamName(teamRequest.teamName())
                .teamExplain(teamRequest.teamExplain())
                .teamTopic(Topic.fromName(teamRequest.topic()))
                .maxParticipant(teamRequest.memberLimit())
                .teamPassword(teamRequest.password())
                .build();

        Team savedTeam = teamRepository.save(team);

		if (teamRequest.teamIconUrl() != null){ // null이라면 기본 이미지!
			team.updateTeamIconUrl(S3ImageUtil.convertImageUrlToObjectKey(teamRequest.teamIconUrl()));
		}

        teamMemberService.saveTeamMember(savedTeam, manager, true);
        for (Member applyMember : applyMembers) {
            teamMemberService.saveTeamMember(savedTeam, applyMember, false);
        }

        if(teamRecruitId != null){
            TeamRecruit teamRecruit = teamRecruitService.findByTeamRecruitIdOrThrow(teamRecruitId);
            teamRecruit.addTeam(savedTeam);
            teamApplyService.deleteTeamApplyAfterTeamMake(teamRecruit);
        }

        return savedTeam;
    }

    public Page<Team> getAllTeamsUsingPaging(Pageable pageable){
        return teamRepository.findAllWithPagination(pageable);
    }

    public Page<Team> getAllTeamsByKeywordUsingPaging(Pageable pageable, String keyword){
        return teamRepository.findByTeamNameContaining(keyword, pageable);
    }

    public List<Team> getMyTeams(Member member){
        List<TeamMember> teamMembers = teamMemberService.getTeamMembersByMember(member);
        return teamMembers.stream()
                .map(TeamMember::getTeam)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamMember joinTeam(Member member, String password, Long teamId){
        Team team = teamRepository.findTeamsByTeamIdWithTeamMembers(teamId)
                .orElseThrow(TeamNotFoundException::new);

        validatePassword(password, team);
        validateTeamMembership(team, member);

        if(team.checkExceedTeamSize()){
            throw new ExceedMaxMembersException();
        }

        return teamMemberService.saveTeamMember(team, member, false);
    }

    @Transactional
    public Team updateTeamAnnouncement(Member member, String teamAnnouncement, Long teamId){
        Team team = getTeamByTeamId(teamId);

        if(!teamMemberService.checkMemberIsTeamManager(teamId, member)) {
            throw new TeamManagerInvalidException();
        }

        team.updateTeamAnnouncement(teamAnnouncement);

        return team;
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        getTeamByTeamId(teamId);

        articleImageRepository.deleteByTeamTeamId(teamId);

        articleRepository.deleteByTeamTeamId(teamId);

        teamMemberService.deleteTeamMemberByTeamId(teamId);

        teamRepository.deleteById(teamId);
    }

    @Transactional
    public void deleteTeamByOwner(Member member, Team team) {
        if(!teamMemberService.checkMemberIsTeamManager(team.getTeamId(), member)){
            throw new TeamMemberInvalidException();
        }

        TeamRecruit teamRecruit = team.getTeamRecruit();
        if(teamRecruit != null){
            teamRecruitService.forceDeleteTeamRecruit(teamRecruit);
        }

        articleImageRepository.deleteByTeamTeamId(team.getTeamId());

        articleRepository.deleteByTeamTeamId(team.getTeamId());

        teamMemberService.deleteTeamMemberByTeamId(team.getTeamId());

        teamRepository.deleteById(team.getTeamId());
    }

    @Transactional(readOnly = true)
    public List<Team> getTeamsManagedByMember(Long memberId) {

        return teamRepository.findByTeamManagerId(memberId);
    }

    @Transactional
    public Team updateTeamInfo(TeamInfoEditRequest teamInfoEditRequest, Member member, Long teamId){
        Team team = teamRepository.findTeamsByTeamIdWithTeamMembers(teamId)
                .orElseThrow(TeamNotFoundException::new);

        if(!teamMemberService.checkMemberIsTeamManager(teamId, member)){
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

    public Team getTeamInfo(Long teamId, Member member){
        Team team = getTeamByTeamId(teamId);

        if(!teamMemberService.checkMemberIsTeamManager(teamId, member)){
            throw new TeamManagerInvalidException();
        }

        return team;
    }

    public Team getTeamByTeamId(Long teamId){
        return teamRepository.findTeamByTeamIdWithTeamRecruit(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    private void validatePassword(String password, Team team) {
        if (!password.equals(team.getTeamPassword())) {
            throw new TeamPasswordInvalidException();
        }
    }

    private void validateTeamMembership(Team team, Member member) {
        if (teamMemberService.existsByTeamIdAndMemberId(team.getTeamId(), member)) {
            throw new TeamAlreadyJoinException();
        }
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}
