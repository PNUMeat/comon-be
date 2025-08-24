package site.codemonster.comon.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.team.entity.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRecommendationRepository extends JpaRepository<TeamRecommendation, Long> {

    /**
     * 팀으로 추천 설정 조회
     */
    Optional<TeamRecommendation> findByTeam(Team team);

    /**
     * 팀 ID로 플랫폼 설정까지 함께 조회 (N+1 방지) - 핵심!
     */
    @Query("SELECT tr FROM TeamRecommendation tr " +
            "LEFT JOIN FETCH tr.platformRecommendations " +
            "WHERE tr.team.teamId = :teamId")
    Optional<TeamRecommendation> findByTeamIdWithPlatforms(@Param("teamId") Long teamId);

    /**
     * 자동 추천이 활성화된 팀들의 추천 설정 조회
     */
    @Query("SELECT tr FROM TeamRecommendation tr " +
            "LEFT JOIN FETCH tr.platformRecommendations " +
            "WHERE tr.autoRecommendationEnabled = true")
    List<TeamRecommendation> findAllEnabledRecommendationsWithPlatforms();
}
