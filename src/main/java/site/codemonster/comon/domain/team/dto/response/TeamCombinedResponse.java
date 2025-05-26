package site.codemonster.comon.domain.team.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record TeamCombinedResponse(
        List<MyTeamResponse>myTeams,
        Page<TeamAllResponse> allTeams) {
}