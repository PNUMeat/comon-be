package site.codemonster.comon.domain.recommendation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(uniqueConstraints={
                @UniqueConstraint(
                        name="day_of_week_team_recommendation_id",
                        columnNames={"day_of_week", "team_recommendation_id"}
                )
})
public class TeamRecommendationDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_recommendation_id")
    private TeamRecommendation teamRecommendation;

    public TeamRecommendationDay(DayOfWeek dayOfWeek, TeamRecommendation teamRecommendation) {
        this.dayOfWeek = dayOfWeek;
        this.teamRecommendation = teamRecommendation;
    }
}
