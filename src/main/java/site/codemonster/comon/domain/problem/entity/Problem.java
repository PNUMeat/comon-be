package site.codemonster.comon.domain.problem.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.request.ProblemUpdateRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(
        name = "problem",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"platform", "platform_problem_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProblemStep problemStep;


    @Column(columnDefinition = "TEXT")
    private String url;


    public Problem(ProblemInfoRequest problemInfoRequest) {
        this.platform = problemInfoRequest.getPlatform();
        this.platformProblemId = problemInfoRequest.getPlatformProblemId();
        this.title = problemInfoRequest.getTitle();
        this.problemStep = problemInfoRequest.getProblemStep();
        this.url = problemInfoRequest.getUrl();
    }

    public Problem(Platform platform, String platformProblemId, String title, ProblemStep problemStep, String url) {
        this.platform = platform;
        this.platformProblemId = platformProblemId;
        this.title = title;
        this.problemStep = problemStep;
        this.url = url;
    }

    public void updateProblem(ProblemUpdateRequest problemUpdateRequest) {
        this.title = problemUpdateRequest.title();
        this.problemStep = problemUpdateRequest.problemStep();
        this.url = problemUpdateRequest.url();
    }

}
