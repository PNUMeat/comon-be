package site.codemonster.comon.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.team.entity.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRecommendationRepository extends JpaRepository<TeamRecommendation, Long> {

    Optional<TeamRecommendation> findByTeam(Team team);

    boolean existsByTeam(Team team);


    @Query("select tr from TeamRecommendation tr join fetch tr.teamRecommendationDays")
    List<TeamRecommendation> findAllWithRecommendationDays();


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from TeamRecommendation t where t.team.teamId = :teamId")
    void deleteByTeamId(Long teamId);
}
