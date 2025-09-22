package site.codemonster.comon.domain.recommendation.entity;

import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.dto.request.PlatformRecommendationRequest;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Table(name = "platform_recommendation")
public class PlatformRecommendation extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_recommendation_id", nullable = false)
    private TeamRecommendation teamRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProblemStep problemStep;

    @Column(nullable = false)
    private Integer problemCount;

    protected PlatformRecommendation() {}

    public PlatformRecommendation(TeamRecommendation teamRecommendation, PlatformRecommendationRequest platformRecommendationRequest) {
        this.teamRecommendation = teamRecommendation;
        this.platform = platformRecommendationRequest.platform();
        this.problemStep = platformRecommendationRequest.problemStep();
        this.problemCount = platformRecommendationRequest.problemCount();
    }

    public PlatformRecommendation(TeamRecommendation teamRecommendation, Platform platform, ProblemStep problemStep, Integer problemCount) {
        this.teamRecommendation = teamRecommendation;
        this.platform = platform;
        this.problemStep = problemStep;
        this.problemCount = problemCount;
    }
}
