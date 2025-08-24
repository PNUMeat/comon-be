package site.codemonster.comon.domain.recommendation.entity;

import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
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

    @OneToMany(mappedBy = "teamRecommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatformRecommendation> platformRecommendations = new ArrayList<>();

    protected TeamRecommendation() {}

    @Builder
    public TeamRecommendation(Team team, Boolean autoRecommendationEnabled,
                              Integer recommendationAt, Integer totalProblemCount) {
        this.team = team;
        this.autoRecommendationEnabled = autoRecommendationEnabled;
        this.recommendationAt = recommendationAt;
        this.totalProblemCount = totalProblemCount;
    }

    public void setRecommendationDay(DayOfWeek dayOfWeek, boolean enabled) {
        int dayBit = 1 << (dayOfWeek.getValue() - 1);
        if (enabled) {
            this.recommendDays |= dayBit;
        } else {
            this.recommendDays &= ~dayBit;
        }
    }

    public void setRecommendationDays(Set<DayOfWeek> days) {
        this.recommendDays = 0; // 초기화
        if (days != null) {
            days.forEach(day -> setRecommendationDay(day, true));
        }
    }

    public Set<DayOfWeek> getRecommendationDays() {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            int dayBit = 1 << (day.getValue() - 1);
            if ((this.recommendDays & dayBit) != 0) {
                days.add(day);
            }
        }
        return days;
    }

    public void resetRecommendationSetting() {
        this.autoRecommendationEnabled = false;
        this.recommendationAt = 9;
        this.recommendDays = 0;
        this.totalProblemCount = 0;
        this.platformRecommendations.clear();
    }

    public void addPlatformRecommendation(PlatformRecommendation platformRecommendation) {
        this.platformRecommendations.add(platformRecommendation);
        platformRecommendation.setTeamRecommendation(this);
    }

    public void replacePlatformRecommendations(List<PlatformRecommendation> newPlatformRecommendations) {
        this.platformRecommendations.clear();
        newPlatformRecommendations.forEach(this::addPlatformRecommendation);
    }
}
