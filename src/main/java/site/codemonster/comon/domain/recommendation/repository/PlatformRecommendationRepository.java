package site.codemonster.comon.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;

import java.util.List;

@Repository
public interface PlatformRecommendationRepository extends JpaRepository<PlatformRecommendation, Long> {

    List<PlatformRecommendation> findByTeamRecommendation(TeamRecommendation teamRecommendation);

    @Modifying(clearAutomatically = true)
    @Query("delete from PlatformRecommendation pr where pr.teamRecommendation.id = :teamRecommendationId")
    void deleteByTeamRecommendationId(Long teamRecommendationId);
}
