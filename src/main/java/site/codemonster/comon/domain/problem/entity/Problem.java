package site.codemonster.comon.domain.problem.entity;

import lombok.Setter;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Setter
@Table(
        name = "problem",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"platform", "platform_problem_id"})
        }
)
public class Problem extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private String platformProblemId;

    @Column(nullable = false)
    private String title;

    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String url;

    private String tags; // 문제 유형

    @Builder
    public Problem(Platform platform, String platformProblemId, String title, String difficulty, String url, String tags) {
        this.platform = platform;
        this.platformProblemId = platformProblemId;
        this.title = title;
        this.difficulty = difficulty;
        this.url = url;
        this.tags = tags;
    }

    protected Problem() {
    }
}
