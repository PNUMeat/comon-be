package site.codemonster.comon.domain.team.repository;

import site.codemonster.comon.domain.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("SELECT t FROM Team t "
            + "LEFT JOIN FETCH t.teamRecruit")
    Page<Team> findAllWithPagination(Pageable pageable);

    Page<Team> findByTeamNameContaining(String keyword, Pageable pageable);
    @Query("SELECT t FROM Team t "
            + "JOIN TeamMember tm ON t.teamId = tm.team.teamId "
            + "WHERE tm.member.id = :managerId AND tm.isTeamManager = TRUE")
    List<Team> findByTeamManagerId(@Param("managerId") Long managerId);

    @Query("SELECT t FROM Team t "
            + "JOIN FETCH t.teamMembers "
            + "WHERE t.teamId = :teamId")
    Optional<Team> findTeamsByTeamIdWithTeamMembers(@Param("teamId") Long teamId);

    @Query(
            "SELECT t FROM Team t "
            + "LEFT JOIN FETCH t.teamRecruit "
            + "WHERE t.teamId = :teamId"
    )
    Optional<Team> findTeamByTeamIdWithTeamRecruit(@Param("teamId") Long teamId);

    @Query("select t from Team t join fetch t.teamRecommendation where t.teamId = :teamId")
    Optional<Team> findByTeamIdWithTeamRecommendation(Long teamId);
}
