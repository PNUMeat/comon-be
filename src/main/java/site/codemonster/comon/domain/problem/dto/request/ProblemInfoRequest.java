package site.codemonster.comon.domain.problem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemInfoRequest {

    private Platform platform;
    private String platformProblemId;
    private String title;
    private String url;
    private ProblemStep problemStep;

    // AJAX 응답용 필드들
    private boolean success;
    private String errorMessage;
}
