package site.codemonster.comon.domain.problem.collector;

import site.codemonster.comon.domain.problem.dto.request.ProblemInfoRequest;
import site.codemonster.comon.domain.problem.dto.response.ProblemInfoResponse;

public interface ProblemCollector {

    ProblemInfoResponse collectProblemInfo(ProblemInfoRequest request);

    boolean isValidProblemId(String problemId);
}
