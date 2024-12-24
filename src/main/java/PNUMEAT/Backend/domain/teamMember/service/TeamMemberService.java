package PNUMEAT.Backend.domain.teamMember.service;

import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import PNUMEAT.Backend.domain.teamMember.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TeamMemberService {
    public final TeamMemberRepository teamMemberRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    public List<TeamMember> getTeamMembersByMember(Member member){
        return teamMemberRepository.findByMemberIdOrderByTeamInfoIdDesc(member.getId());
    }

}
