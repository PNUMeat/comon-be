package PNUMEAT.Backend.domain.teamMember.repository;

import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.team t " +
            "JOIN FETCH t.teamMembers " +
            "WHERE tm.member.id = :memberId " +
            "ORDER BY tm.teamInfoId DESC")
    List<TeamMember> findByMemberIdOrderByTeamInfoIdDesc(@Param("memberId") Long memberId);

    boolean existsByTeamAndMember(Team team, Member member);

    boolean existsByTeamTeamIdAndMemberId(Long teamId, Long memberId);
}
