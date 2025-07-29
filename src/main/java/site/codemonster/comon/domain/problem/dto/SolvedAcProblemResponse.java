package site.codemonster.comon.domain.problem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolvedAcProblemResponse {
    private Integer problemId;

    private String titleKo;

    private Integer level;
}
