package site.codemonster.comon.domain.teamRecruit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitCreateRequest;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitInviteRequest;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitUpdateRequest;
import site.codemonster.comon.domain.teamRecruit.dto.response.TeamRecruitCreateResponse;
import site.codemonster.comon.domain.teamRecruit.dto.response.TeamRecruitGetResponse;
import site.codemonster.comon.domain.teamRecruit.dto.response.TeamRecruitParticularResponse;
import site.codemonster.comon.domain.teamRecruit.dto.response.TeamRecruitUpdateResponse;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static site.codemonster.comon.domain.teamRecruit.controller.TeamRecruitResponseEnum.*;

@Controller
@RequestMapping("/api/v1/recruitments")
@RequiredArgsConstructor
public class TeamRecruitController {

    private final TeamRecruitService teamRecruitService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamRecruitCreateResponse>> createTeamRecruitment(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid TeamRecruitCreateRequest teamRecruitCreateRequest
    ) {

        TeamRecruit teamRecruit = teamRecruitService.createTeamRecruit(teamRecruitCreateRequest, member);

        TeamRecruitCreateResponse teamRecruitCreateResponse = TeamRecruitCreateResponse.of(teamRecruit);

        return ResponseEntity.status(TEAM_RECRUIT_CREATE.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse(teamRecruitCreateResponse, TEAM_RECRUIT_CREATE.getMessage()));
    }

    @GetMapping
    public ResponseEntity<?> getTeamRecruitmentList(
            @RequestParam(name = "status", defaultValue = "all") String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<TeamRecruitGetResponse> responsePage = teamRecruitService.findTeamRecruitmentWithPage(pageable, status);

        return ResponseEntity.status(TEAM_RECRUIT_GET_PAGINATION.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(responsePage, TEAM_RECRUIT_GET_PAGINATION.getMessage()));
    }

    @GetMapping("/{recruitId}")
    public ResponseEntity<?> getParticularTeamRecruitId(
            @PathVariable("recruitId") Long recruitId,
            @AuthenticationPrincipal Member member
    ){

        TeamRecruitParticularResponse response = teamRecruitService.findTeamRecruitParticular(recruitId, member);

        return ResponseEntity.status(TEAM_RECRUIT_GET_PAGINATION.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, TEAM_RECRUIT_GET_PAGINATION.getMessage()));
    }

    @PatchMapping("/{recruitId}")
    public ResponseEntity<?> changeTeamRecruitStatus(
            @PathVariable("recruitId") Long recruitId,
            @AuthenticationPrincipal Member member
    ){
        teamRecruitService.changeTeamRecruitStatus(recruitId, member);

        return ResponseEntity.status(TEAM_RECRUIT_CHANGE_STATUS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(null, TEAM_RECRUIT_CHANGE_STATUS.getMessage()));
    }

    @PutMapping("/{recruitId}")
    public ResponseEntity<?> updateTeamRecruit(
            @PathVariable("recruitId") Long recruitId,
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid TeamRecruitUpdateRequest teamRecruitUpdateRequest
    ){
        teamRecruitService.updateTeamRecruit(recruitId, member, teamRecruitUpdateRequest);

        TeamRecruitUpdateResponse response = new TeamRecruitUpdateResponse(recruitId);

        return ResponseEntity.status(TEAM_RECRUIT_UPDATE.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, TEAM_RECRUIT_UPDATE.getMessage()));
    }

    @DeleteMapping("/{recruitId}")
    public ResponseEntity<?> deleteTeamRecruit(
            @PathVariable("recruitId") Long recruitId,
            @AuthenticationPrincipal Member member
    ){

        teamRecruitService.deleteByRecruitId(recruitId, member);

        return ResponseEntity.status(TEAM_RECRUIT_DELETE.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(null, TEAM_RECRUIT_DELETE.getMessage()));
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteTeamMember(
            @RequestBody @Valid TeamRecruitInviteRequest teamRecruitInviteRequest,
            @AuthenticationPrincipal Member member
    ){

        teamRecruitService.invite(teamRecruitInviteRequest, member);

        return ResponseEntity.status(TEAM_RECRUIT_INVITE.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(null, TEAM_RECRUIT_INVITE.getMessage()));
    }
}
