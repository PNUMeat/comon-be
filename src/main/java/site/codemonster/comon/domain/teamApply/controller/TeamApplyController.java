package site.codemonster.comon.domain.teamApply.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.teamApply.dto.request.TeamApplyCreateRequest;
import site.codemonster.comon.domain.teamApply.dto.request.TeamApplyUpdateRequest;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamApply.service.TeamApplyService;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitLowService;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static site.codemonster.comon.domain.teamApply.controller.TeamApplyResponseEnum.*;

@Controller
@RequestMapping("/api/v1/apply")
@RequiredArgsConstructor
public class TeamApplyController {
    private final TeamRecruitService teamRecruitService;
    private final TeamMemberService teamMemberService;
    private final TeamApplyService teamApplyService;
    private final TeamRecruitLowService teamRecruitLowService;

    @PostMapping
    public ResponseEntity<?> createTeamApply(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid TeamApplyCreateRequest teamApplyCreateRequest
    ){
        TeamRecruit foundTeamRecruit = teamRecruitLowService.findByTeamRecruitIdOrThrow(teamApplyCreateRequest.recruitmentId());

        if(foundTeamRecruit.existsTeam()){
            Team team = foundTeamRecruit.getTeam();
            teamMemberService.throwIfMemberAlreadyInTeam(team.getTeamId(), member);
        }

        teamApplyService.createTeamApply(teamApplyCreateRequest, foundTeamRecruit, member);

        return ResponseEntity.status(TEAM_APPLY_CREATE.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponseWithMessage(TEAM_APPLY_CREATE.getMessage()));
    }

    @DeleteMapping("/{applyId}")
    public ResponseEntity<?> deleteTeamApply(
            @AuthenticationPrincipal Member member,
            @PathVariable("applyId") Long applyId
    ) {
        TeamApply teamApply = teamApplyService.findTeamApplyByIdOrThrow(applyId);

        teamApplyService.deleteTeamApply(member, teamApply);

        return ResponseEntity.status(TEAM_APPLY_DELETE.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(TEAM_APPLY_DELETE.getMessage()));
    }

    @PatchMapping("/{applyId}")
    public ResponseEntity<?> updateTeamApply(
            @AuthenticationPrincipal Member member,
            @PathVariable("applyId") Long applyId,
            @RequestBody @Valid TeamApplyUpdateRequest teamApplyUpdateRequest
    ) {
        TeamApply teamApply = teamApplyService.findTeamApplyByIdOrThrow(applyId);

        teamApplyService.updateTeamApply(member, teamApply, teamApplyUpdateRequest.teamApplyBody());

        return ResponseEntity.status(TEAM_APPLY_UPDATE.getStatusCode()).contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(TEAM_APPLY_UPDATE.getMessage()));
    }
}
