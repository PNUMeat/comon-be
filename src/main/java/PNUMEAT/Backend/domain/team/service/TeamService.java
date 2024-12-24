package PNUMEAT.Backend.domain.team.service;

import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.team.dto.request.TeamRequest;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.enums.Topic;
import PNUMEAT.Backend.domain.team.repository.TeamRepository;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import PNUMEAT.Backend.domain.teamMember.repository.TeamMemberRepository;
import PNUMEAT.Backend.domain.teamMember.service.TeamMemberService;
import PNUMEAT.Backend.global.error.Team.TeamAlreadyJoinException;
import PNUMEAT.Backend.global.error.Team.TeamManagerInvalidException;
import PNUMEAT.Backend.global.error.Team.TeamNotFoundException;
import PNUMEAT.Backend.global.error.Team.TeamPasswordInvalidException;
import PNUMEAT.Backend.global.images.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ImageService imageService;
    private final TeamMemberService teamMemberService;

    @Transactional
    public Team createTeam(TeamRequest teamRequest,
                           MultipartFile teamIcon, Member member){

        Team team = Team.builder()
                .teamName(teamRequest.teamName())
                .teamExplain(teamRequest.teamExplain())
                .teamTopic(Topic.fromName(teamRequest.topic()))
                .maxParticipant(teamRequest.memberLimit())
                .teamPassword(teamRequest.password())
                .teamManager(member)
                .build();

        if (teamIcon != null){
            String teamIconUrl = imageService.teamImageUpload(teamIcon);
            team.updateTeamIconUrl(teamIconUrl);
        }

        TeamMember teamMember = new TeamMember(team, member);

        teamMemberRepository.save(teamMember);

        return teamRepository.save(team);
    }

    public Page<Team> getAllTeams(Pageable pageable){
        return teamRepository.findAll(pageable);
    }

    public Page<Team> getAllTeamsByKeyword(Pageable pageable, String keyword){
        return teamRepository.findByTeamNameContaining(keyword, pageable);
    }

    public List<Team> getMyTeam(Member member){
        List<TeamMember> teamMembers = teamMemberService.getTeamMembersByMember(member);
        return teamMembers.stream()
                .map(TeamMember::getTeam)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamMember joinTeam(Member member, String password, Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        validatePassword(password, team);
        validateTeamMembership(team, member);

        return teamMemberRepository.save(new TeamMember(team, member));
    }

    @Transactional
    public Team updateTeamAnnouncement(Member member, String teamAnnouncement, Long teamId){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        if(!member.getUuid().equals(team.getTeamManager().getUuid())) {
            throw new TeamManagerInvalidException();
        }

        team.updateTeamAnnouncement(teamAnnouncement);

        return team;
    }

    private void validatePassword(String password, Team team) {
        if (!password.equals(team.getTeamPassword())) {
            throw new TeamPasswordInvalidException();
        }
    }

    private void validateTeamMembership(Team team, Member member) {
        if (teamMemberRepository.existsByTeamAndMember(team, member)) {
            throw new TeamAlreadyJoinException();
        }
    }
}
