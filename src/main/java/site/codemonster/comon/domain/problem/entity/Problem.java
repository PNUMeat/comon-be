package site.codemonster.comon.domain.problem.entity;

import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(
        name = "problem",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"platform", "platform_problem_id"})
        }
)public class Problem extends TimeStamp {
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

    @Builder
    public Problem(Platform platform, String platformProblemId, String title, String difficulty, String url) {
        this.platform = platform;
        this.platformProblemId = platformProblemId;
        this.title = title;
        this.difficulty = difficulty;
        this.url = url;
    }

    protected Problem(){
    }

    public void updateProblemInfo(String title, String difficulty, String url) {
        if (title != null) {
            this.title = title;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (url != null) {
            this.url = url;
        }
    }
}
