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

    @Setter
    @Column(nullable = false)
    private Boolean autoRecommendationEnabled = false;

    @Setter
    @Column(nullable = false)
    private Integer recommendationAt = 9; // 추천 시간 (기본 9시)

    @Setter
    @Column(nullable = false)
    private Integer recommendDays = 0; // 비트마스킹으로 요일 저장

    @Setter
    @Column(nullable = false)
    private Integer totalProblemCount = 0;

    protected TeamRecommendation() {}

    @Builder
    public TeamRecommendation(Team team, Boolean autoRecommendationEnabled,
                              Integer recommendationAt, Integer totalProblemCount) {
        this.team = team;
        this.autoRecommendationEnabled = autoRecommendationEnabled;
        this.recommendationAt = recommendationAt;
        this.totalProblemCount = totalProblemCount;
    }

    public void setRecommendationDays(Set<DayOfWeek> days) {
        this.recommendDays = DateUtils.convertDaysToBitMask(days);
    }

    public Set<DayOfWeek> getRecommendationDays() {
        return DateUtils.convertBitMaskToDays(this.recommendDays);
    }

    public void updateInitialSettings(TeamRecommendationRequest request) {
        this.autoRecommendationEnabled = request.autoRecommendationEnabled();
        this.recommendationAt = request.recommendationAt();
        this.totalProblemCount = calculateTotalProblemCount(request.platformSettings());
        this.setRecommendationDays(request.recommendDays());
    }

    private Integer calculateTotalProblemCount(List<TeamRecommendationRequest.PlatformRecommendationSetting> settings) {
        return settings.stream()
                .filter(TeamRecommendationRequest.PlatformRecommendationSetting::enabled)
                .mapToInt(TeamRecommendationRequest.PlatformRecommendationSetting::problemCount)
                .sum();
    }
}
