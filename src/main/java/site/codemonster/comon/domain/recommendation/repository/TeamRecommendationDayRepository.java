package site.codemonster.comon.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendationDay;

public interface TeamRecommendationDayRepository extends JpaRepository<TeamRecommendationDay, Long> {
    @Modifying(clearAutomatically = true)
    @Query("delete from TeamRecommendationDay trd where trd.teamRecommendation.id = :teamRecommendationId")
    void deleteByTeamRecommendationId(Long teamRecommendationId);
}
