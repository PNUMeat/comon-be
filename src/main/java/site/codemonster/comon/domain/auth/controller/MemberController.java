package site.codemonster.comon.domain.auth.controller;

import site.codemonster.comon.domain.auth.dto.request.MemberProfileCreateRequest;
import site.codemonster.comon.domain.auth.dto.request.MemberProfileUpdateRequest;
import site.codemonster.comon.domain.auth.dto.response.MemberInfoResponse;
import site.codemonster.comon.domain.auth.dto.response.MemberProfileResponse;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.domain.team.dto.response.TeamAbstractResponse;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;
import site.codemonster.comon.global.security.annotation.LoginMember;
import site.codemonster.comon.global.util.cookie.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static site.codemonster.comon.global.response.ResponseMessageEnum.MEMBER_DELETE_SUCCESS;

@Trace
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TeamMemberService teamMemberService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createMemberProfile(
        @RequestBody @Valid MemberProfileCreateRequest memberProfileCreateRequest,
        @LoginMember Member member
    ){
        memberService.createMemberProfile(memberProfileCreateRequest, member);

        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponseWithDate("회원을 성공적으로 등록했습니다."));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateMemberProfile(
        @RequestBody @Valid MemberProfileUpdateRequest memberProfileUpdateRequest,
        @LoginMember Member member
    ){
        Member updatedMember = memberService.updateMemberProfile(memberProfileUpdateRequest, member);

        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(MemberProfileResponse.of(updatedMember)));
    }

    @GetMapping("/own-profile")
    public ResponseEntity<ApiResponse<?>> getOwnProfile(@LoginMember Member member){

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(MemberProfileResponse.of(member)));
    }

    @GetMapping("/profile/{uuid}")
    public ResponseEntity<ApiResponse<?>> getMemberProfile(@PathVariable("uuid") String uuid){

        Member findMember = memberService.getMemberByUUID(uuid);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(MemberProfileResponse.of(findMember)));
    }


    @GetMapping("/info")
    public ResponseEntity<ApiResponse<?>> getMemberInfo(@LoginMember Member member) {

        List<TeamMember> teamMemberAndTeamByMember = teamMemberService.getTeamMemberAndTeamByMember(member);

        List<TeamAbstractResponse> teamAbstractResponses = teamMemberAndTeamByMember.stream()
                .map(TeamMember::getTeam)
                .map(TeamAbstractResponse::of)
                .toList();

        MemberInfoResponse memberInfoResponse = MemberInfoResponse.from(member, teamAbstractResponses);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(memberInfoResponse));
    }

    @DeleteMapping()
    public ResponseEntity<ApiResponse<?>> deleteMember(@LoginMember Member member, HttpServletResponse response){
        memberService.deleteMember(member.getId());

        CookieUtils.clearCookie(response);

        return ResponseEntity.status(MEMBER_DELETE_SUCCESS.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiResponse.successResponseWithMessage(MEMBER_DELETE_SUCCESS.getMessage()));
    }
}
