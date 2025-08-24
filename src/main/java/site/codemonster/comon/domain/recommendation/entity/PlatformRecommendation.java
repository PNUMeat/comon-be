package site.codemonster.comon.domain.recommendation.entity;

import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
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

    @Column(columnDefinition = "TEXT")
    private String difficulties;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(nullable = false)
    private Integer problemCount = 2;

    @Column(nullable = false)
    private Boolean enabled = false;

    protected PlatformRecommendation() {}

    @Builder
    public PlatformRecommendation(Platform platform, String difficulties, String tags,
                                  Integer problemCount, Boolean enabled) {
        this.platform = platform;
        this.difficulties = difficulties;
        this.tags = tags;
        this.problemCount = problemCount;
        this.enabled = enabled;
    }

    /**
     * 플랫폼 추천 설정 업데이트
     */
    public void updateSettings(String difficulties, String tags, Integer problemCount, Boolean enabled) {
        this.difficulties = difficulties;
        this.tags = tags;
        this.problemCount = problemCount;
        this.enabled = enabled;
    }
}
