package site.codemonster.comon.domain.problem.dto.response;

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
public class ProblemInfoResponse {

    private Platform platform;
    private String platformProblemId;
    private String title;
    private ProblemStep problemStep;
    private String url;

    private boolean isDuplicate;

    // AJAX 응답용 필드들
    private boolean success;
    private String errorMessage;
}
