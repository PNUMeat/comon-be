package site.codemonster.comon.domain.recommendation.entity;

import lombok.AllArgsConstructor;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "recommendation_history")
public class RecommendationHistory extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private LocalDate recommendedAt;

    protected RecommendationHistory() {}

    @Builder
    public RecommendationHistory(Team team, Problem problem, LocalDate recommendedAt) {
        this.team = team;
        this.problem = problem;
        this.recommendedAt = recommendedAt;
    }

    public static RecommendationHistory of(Team team, Problem problem, LocalDate date) {
        return RecommendationHistory.builder()
                .team(team)
                .problem(problem)
                .recommendedAt(date)
                .build();
    }
}
