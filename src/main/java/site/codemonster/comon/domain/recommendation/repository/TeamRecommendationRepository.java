package site.codemonster.comon.domain.recommendation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.team.entity.Team;

import java.util.Optional;

public interface TeamRecommendationRepository extends JpaRepository<TeamRecommendation, Long> {

    Optional<TeamRecommendation> findByTeam(Team team);

    List<TeamRecommendation> findByAutoRecommendationEnabledTrue();
}
