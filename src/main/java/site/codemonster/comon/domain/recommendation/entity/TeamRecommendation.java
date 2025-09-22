package site.codemonster.comon.domain.recommendation.entity;

import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import site.codemonster.comon.global.util.dateUtils.DateUtils;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Table(name = "team_recommendation")
public class TeamRecommendation extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, unique = true)
    private Team team;

    @Column(nullable = false)
    private Integer recommendationAt = 9; // 추천 시간 (기본 9시)

    @OneToMany(mappedBy = "teamRecommendation")
    private List<TeamRecommendationDay> teamRecommendationDays;

    @OneToMany(mappedBy = "teamRecommendation")
    private List<PlatformRecommendation>  platformRecommendations;

    protected TeamRecommendation() {}

    public TeamRecommendation(Team team, Integer recommendationAt) {
        this.team = team;
        this.recommendationAt = recommendationAt;
    }
}
