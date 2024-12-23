package PNUMEAT.Backend.domain.team.controller;

import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.team.dto.request.TeamAnnouncementRequest;
import PNUMEAT.Backend.domain.team.dto.request.TeamJoinRequest;
import PNUMEAT.Backend.domain.team.dto.request.TeamRequest;
import PNUMEAT.Backend.domain.team.dto.response.MyTeamResponse;
import PNUMEAT.Backend.domain.team.dto.response.TeamAllResponse;
import PNUMEAT.Backend.domain.team.dto.response.TeamCombinedResponse;
import PNUMEAT.Backend.domain.team.dto.response.TeamJoinResponse;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.service.TeamService;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import PNUMEAT.Backend.global.error.dto.response.ApiResponse;
import PNUMEAT.Backend.global.security.annotation.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static PNUMEAT.Backend.global.response.ResponseMessageEnum.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTeam(@ModelAttribute @Valid TeamRequest teamRequest,
                                                      @RequestPart(value = "image", required = false) MultipartFile image,
                                                      @LoginMember Member member){
        teamService.createTeam(teamRequest, image, member);

        return ResponseEntity.status(TEAM_CREATED_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponseWithMessage(TEAM_CREATED_SUCCESS.getMessage()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTeams(@RequestParam(name = "sort", defaultValue = "recent") String sort,
                                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                                      @RequestParam(name = "size", defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<Team> teams = teamService.getAllTeams(pageable);

        Page<TeamAllResponse> teamAllResponses = teams.map(TeamAllResponse::of);

        return ResponseEntity.status(TEAM_TOTAL_DETAILS_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(teamAllResponses));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyTeam(@LoginMember Member member){
        List<Team> teamMembers =  teamService.getMyTeam(member);

        List<MyTeamResponse> myTeamResponse = teamMembers.stream()
                .map(MyTeamResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.status(MY_TEAM_DETAILS_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(myTeamResponse));
    }

    @GetMapping("/combined")
    public ResponseEntity<ApiResponse<?>> getCombinedTeamsInfo(@RequestParam(name = "sort", defaultValue = "recent") String sort,
                                                           @RequestParam(name = "page", defaultValue = "0") int page,
                                                           @RequestParam(name = "size", defaultValue = "6") int size,
                                                           @LoginMember Member member) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<Team> teams = teamService.getAllTeams(pageable);
        Page<TeamAllResponse> teamAllResponses = teams.map(TeamAllResponse::of);

        List<Team> myTeams = teamService.getMyTeam(member);
        List<MyTeamResponse> myTeamResponses = myTeams.stream()
                .map(MyTeamResponse::of)
                .collect(Collectors.toList());

        TeamCombinedResponse teamCombinedResponse = new TeamCombinedResponse(myTeamResponses, teamAllResponses);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(teamCombinedResponse));
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<ApiResponse<?>> joinTeam(@PathVariable("teamId") Long teamId,
                                                    @RequestBody TeamJoinRequest teamJoinRequest,
                                                    @LoginMember Member member){
        TeamMember teamMember = teamService.joinTeam(member, teamJoinRequest.password(), teamId);

        TeamJoinResponse teamJoinResponse = TeamJoinResponse.of(teamMember);

        return ResponseEntity.status(TEAM_JOIN_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(teamJoinResponse));
    }

    @PatchMapping("/{teamId}/announcement")
    public ResponseEntity<ApiResponse<?>> updateTeamAnnouncement(@PathVariable("teamId") Long teamId,
                                                                 @RequestBody @Valid TeamAnnouncementRequest teamAnnouncementRequest,
                                                                 @LoginMember Member member) {
        teamService.updateTeamAnnouncement(member, teamAnnouncementRequest.teamAnnouncement(), teamId);

        return ResponseEntity.status(TEAM_ANNOUNCEMENT_UPDATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(TEAM_ANNOUNCEMENT_UPDATE_SUCCESS.getMessage()));
    }
}


