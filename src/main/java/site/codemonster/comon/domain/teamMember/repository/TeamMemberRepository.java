package site.codemonster.comon.domain.teamMember.repository;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.team t " +
            "JOIN FETCH t.teamMembers " +
            "WHERE tm.member.id = :memberId " +
            "ORDER BY tm.teamInfoId DESC")
    List<TeamMember> findByMemberIdOrderByTeamInfoIdDesc(@Param("memberId") Long memberId);

    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.team t " +
            "WHERE tm.member.id = :memberId " +
            "ORDER BY tm.teamInfoId DESC")
    List<TeamMember> findTeamMemberByMemberIdWithTeamDesc(@Param("memberId") Long memberId);

    boolean existsByTeamTeamIdAndMemberId(Long teamId, Long memberId);

    void deleteByTeamTeamIdAndMember(Long teamId, Member member);

    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.team.teamId = :teamId")
    void deleteByTeamTeamId(@Param("teamId") Long teamId);

    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    Optional<TeamMember> findTeamMemberByTeamTeamIdAndMemberId(Long teamId, Long memberId);

    @Query("SELECT tm FROM TeamMember tm " +
            "JOIN FETCH tm.member " +
            "WHERE tm.team.teamId = :teamId")
    List<TeamMember> findByTeamId(@Param("teamId") Long teamId);
}
