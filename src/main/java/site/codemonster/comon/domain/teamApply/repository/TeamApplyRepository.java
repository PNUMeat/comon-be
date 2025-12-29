package site.codemonster.comon.domain.teamApply.repository;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamApplyRepository extends JpaRepository<TeamApply, Long> {

    @Query("SELECT ta FROM TeamApply ta " +
            "JOIN FETCH ta.member " +
            "WHERE ta.teamRecruit.teamRecruitId = :teamRecruitId " +
            "ORDER BY " +
            "CASE WHEN ta.member.id = :memberId THEN 0 ELSE 1 END, " +
            "ta.createdDate DESC")
    List<TeamApply> findTeamAppliesWithAuthorFirst(@Param("teamRecruitId") Long teamRecruitId, @Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true)
    @Query(
            "DELETE FROM TeamApply ta WHERE ta.teamRecruit.teamRecruitId = :teamRecruitId"
    )
    void deleteTeamAppliesByTeamRecruitId(@Param("teamRecruitId") Long teamRecruitId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TeamApply ta " +
            "WHERE ta.teamRecruit.teamRecruitId " +
            "IN :teamRecruitIds")
    void deleteTeamAppliesByTeamRecruitIds(@Param("teamRecruitIds") List<Long> teamRecruitIds);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TeamApply ta " +
            "WHERE ta.member.id = :memberId")
    void deleteTeamAppliesByMemberId(@Param("memberId") Long memberId);


}
