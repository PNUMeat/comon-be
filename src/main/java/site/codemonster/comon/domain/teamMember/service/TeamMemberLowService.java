package site.codemonster.comon.domain.teamMember.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.global.error.Team.TeamAlreadyJoinException;
import site.codemonster.comon.global.error.Team.TeamManagerInvalidException;
import site.codemonster.comon.global.error.Team.TeamManagerNotFoundException;
import site.codemonster.comon.global.error.Team.TeamMemberInvalidException;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class TeamMemberLowService {

    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public List<TeamMember> getTeamMembersByMember(Member member){
        return teamMemberRepository.findByMemberIdOrderByTeamInfoIdDesc(member.getId());
    }

    public TeamMember saveTeamMember(Team team, Member member, Boolean isTeamManager){
        TeamMember teamMember = new TeamMember(team, member, isTeamManager);

        return teamMemberRepository.save(teamMember);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> getTeamMemberAndTeamByMember(Member member){
        return teamMemberRepository.findTeamMemberByMemberIdWithTeamDesc(member.getId());
    }

    @Transactional(readOnly = true)
    public boolean existsByTeamIdAndMemberId(Long teamId, Member member){
        return teamMemberRepository.existsByTeamTeamIdAndMemberId(teamId, member.getId());
    }

    @Transactional(readOnly = true)
    public TeamMember getTeamMemberByTeamIdAndMemberId(Long teamId, Member member) {
        return teamMemberRepository.findTeamMemberByTeamTeamIdAndMemberId(teamId, member.getId())
                .orElseThrow(TeamMemberInvalidException::new);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> getTeamMembersByTeamId(Long teamId, Member manager) {
        if(!checkMemberIsTeamManager(teamId, manager)){
            throw new TeamManagerInvalidException();
        }

        return teamMemberRepository.findByTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public boolean checkMemberIsTeamManagerOrThrow(TeamMember teamMember){
        if(!teamMember.getIsTeamManager()){
            throw new TeamManagerInvalidException();
        }

        return true;
    }

    @Transactional(readOnly = true)
    public void throwIfMemberAlreadyInTeam(Long teamId, Member member){
        if(teamMemberRepository.existsByTeamTeamIdAndMemberId(teamId, member.getId())){
            throw new TeamAlreadyJoinException();
        }
    }

    public void deleteTeamMemberByTeamId(Long teamId){
        teamMemberRepository.deleteByTeamTeamId(teamId);
    }

    @Transactional(readOnly = true)
    public boolean checkMemberIsTeamManager(Long teamId, Member member){
        Optional<TeamMember> teamMemberOptional = teamMemberRepository.findTeamMemberByTeamTeamIdAndMemberId(teamId, member.getId());

        if(teamMemberOptional.isEmpty()){
            return false;
        }

        return teamMemberOptional.get().getIsTeamManager();
    }

    @Transactional(readOnly = true)
    public TeamMember getTeamManagerByTeamId(Long teamId) {
        return teamMemberRepository.findFirstTeamManagerByTeamId(teamId)
                .orElseThrow(TeamManagerNotFoundException::new);
    }

    public void deleteByTeamTeamIdAndMember(Long teamId, Member member){
        teamMemberRepository.deleteByTeamTeamIdAndMember(teamId, member);
    }

    public void deleteByTeamTeamId(Long teamId) {
        teamMemberRepository.deleteByTeamTeamId(teamId);
    }

    public void deleteByMemberId(Long memberId){
        teamMemberRepository.deleteByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public void validateTeamMember(Long teamId, Member member) {
        if (!teamMemberRepository.existsByTeamTeamIdAndMemberId(teamId, member.getId())) {
            throw new TeamMemberInvalidException();
        }
    }
}
