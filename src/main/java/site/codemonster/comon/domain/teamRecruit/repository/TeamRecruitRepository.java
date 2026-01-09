package site.codemonster.comon.domain.teamRecruit.repository;

import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRecruitRepository extends JpaRepository<TeamRecruit, Long> {

    @Query(
            "SELECT tr FROM TeamRecruit tr "
          + "JOIN FETCH tr.member "
          + "LEFT JOIN FETCH tr.team "
          + "LEFT JOIN FETCH tr.images "
          + "ORDER BY tr.isRecruiting DESC, tr.createdDate DESC"
    )
    Page<TeamRecruit> findAllWithPaging(Pageable pageable);

    @Query(
            "SELECT tr FROM TeamRecruit tr "
          + "JOIN FETCH tr.member "
          + "LEFT JOIN FETCH tr.team "
          + "LEFT JOIN FETCH tr.images "
          + "WHERE tr.isRecruiting = TRUE "
          + "ORDER BY tr.createdDate DESC"
    )
    Page<TeamRecruit> findByIsRecruitingTrueWithPaging(Pageable pageable);

    @Query(
            "SELECT tr FROM TeamRecruit tr "
          + "JOIN FETCH tr.member "
          + "LEFT JOIN FETCH tr.team "
          + "LEFT JOIN FETCH tr.images "
          + "WHERE tr.isRecruiting = FALSE "
          + "ORDER BY tr.createdDate DESC"
    )
    Page<TeamRecruit> findByIsRecruitingFalseWithPaging(Pageable pageable);

    @Query(
            "SELECT tr FROM TeamRecruit tr "
            + "JOIN FETCH tr.member "
            + "LEFT JOIN FETCH tr.team "
            + "LEFT JOIN FETCH tr.images "
            + "WHERE tr.teamRecruitId = :teamRecruitId"
    )
    Optional<TeamRecruit> findTeamRecruitByTeamRecruitIdWithRelation(@Param("teamRecruitId") Long teamRecruitId);

    @Query("SELECT tr.teamRecruitId " +
            "FROM TeamRecruit tr " +
            "WHERE tr.member.id = :memberId")
    List<Long> findIdsByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TeamRecruit tr " +
            "WHERE tr.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TeamRecruit tr " +
            "WHERE tr.teamRecruitId = :teamRecruitId")
    void deleteById(@Param("teamRecruitId") Long teamRecruitId);

    Optional<TeamRecruit> findByTeam(Team team);
}
