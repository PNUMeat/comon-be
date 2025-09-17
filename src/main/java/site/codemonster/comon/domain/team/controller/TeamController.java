package site.codemonster.comon.domain.team.controller;

import site.codemonster.comon.domain.article.dto.request.CalenderSubjectRequest;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberService;
import site.codemonster.comon.domain.team.dto.request.*;
import site.codemonster.comon.domain.team.dto.response.*;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.service.TeamRecruitService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;
import site.codemonster.comon.global.security.annotation.LoginMember;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static site.codemonster.comon.global.response.ResponseMessageEnum.*;

@Trace
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final ArticleService articleService;
    private final TeamMemberService teamMemberService;
    private final MemberService memberService;
    private final TeamRecruitService teamRecruitService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTeam(
            @RequestBody @Valid TeamRequest teamRequest,
            @LoginMember Member manager
    ){
        List<String> memberUuids = teamRequest.teamMemberUuids();

        List<Member> applyMembers = new ArrayList<>();
        if(memberUuids != null){
            for (String memberUuid : memberUuids) {
                applyMembers.add(memberService.getMemberByUUID(memberUuid));
            }
        }

        if(teamRequest.teamRecruitId() != null){
            TeamRecruit teamRecruit = teamRecruitService.findByTeamRecruitIdOrThrow(teamRequest.teamRecruitId());
            teamRecruitService.isAuthorOrThrow(teamRecruit, manager);
        }

        Team createdTeam = teamService.createTeam(teamRequest, manager, applyMembers, teamRequest.teamRecruitId());

        TeamCreateResponse teamCreateResponse = TeamCreateResponse.of(createdTeam);

        return ResponseEntity.status(TEAM_CREATED_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse(teamCreateResponse, TEAM_CREATED_SUCCESS.getMessage()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTeams(
            @RequestParam(name = "sort", defaultValue = "recent") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<Team> teams = teamService.getAllTeamsUsingPaging(pageable);

        Page<TeamAllResponse> teamAllResponses = teams.map(teamService::getTeamAllResponse);

        return ResponseEntity.status(TEAM_TOTAL_DETAILS_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(teamAllResponses));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllTeamsForAdmin() {
        List<Team> teams = teamService.getAllTeams();

        List<TeamSimpleResponse> teamResponses = teams.stream()
                .map(TeamSimpleResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(teamResponses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> getAllTeamsByKeyword(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "sort", defaultValue = "recent") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<Team> teams = teamService.getAllTeamsByKeywordUsingPaging(pageable,keyword);
        Page<TeamAllResponse> teamAllResponses = teams.map(teamService::getTeamAllResponse);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(teamAllResponses));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyTeam(@LoginMember Member member){
        List<Team> teamMembers =  teamService.getMyTeams(member);

        List<MyTeamResponse> myTeamResponse = teamMembers.stream()
                .map(teamService::getMyTeamResponse)
                .collect(Collectors.toList());

        return ResponseEntity.status(MY_TEAM_DETAILS_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(myTeamResponse));
    }

    @GetMapping("/my-page")
    public ResponseEntity<ApiResponse<?>> getMyTeamAtMyPage(@LoginMember Member member){
        List<TeamMember> teamMembers =  teamMemberService.getTeamMemberAndTeamByMember(member);

        List<MyTeamMyPageResponse> myTeamMyPageResponse = teamMembers.stream()
                .map(MyTeamMyPageResponse::of)
                .collect(Collectors.toList());

        return ResponseEntity.status(MY_TEAM_DETAILS_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(myTeamMyPageResponse));
    }

    @GetMapping("/combined")
    public ResponseEntity<ApiResponse<?>> getCombinedTeamsInfo(
            @RequestParam(name = "sort", defaultValue = "recent") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            @LoginMember Member member
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<Team> teams = teamService.getAllTeamsUsingPaging(pageable);
        Page<TeamAllResponse> teamAllResponses = teams.map(teamService::getTeamAllResponse);

        List<Team> myTeams = teamService.getMyTeams(member);
        List<MyTeamResponse> myTeamResponses = myTeams.stream()
                .map(teamService::getMyTeamResponse)
                .collect(Collectors.toList());

        TeamCombinedResponse teamCombinedResponse = new TeamCombinedResponse(myTeamResponses, teamAllResponses);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(teamCombinedResponse));
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<ApiResponse<?>> joinTeam(
            @PathVariable("teamId") Long teamId,
            @RequestBody TeamJoinRequest teamJoinRequest,
            @LoginMember Member member
    ){
        TeamMember teamMember = teamService.joinTeam(member, teamJoinRequest.password(), teamId);

        TeamJoinResponse teamJoinResponse = TeamJoinResponse.of(teamMember);

        return ResponseEntity.status(TEAM_JOIN_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(teamJoinResponse,TEAM_JOIN_SUCCESS.getMessage()));
    }

    @PatchMapping("/{teamId}/announcement")
    public ResponseEntity<ApiResponse<?>> updateTeamAnnouncement(
            @PathVariable("teamId") Long teamId,
            @RequestBody @Valid TeamAnnouncementRequest teamAnnouncementRequest,
            @LoginMember Member member
    ) {
        teamService.updateTeamAnnouncement(member, teamAnnouncementRequest.teamAnnouncement(), teamId);

        return ResponseEntity.status(TEAM_ANNOUNCEMENT_UPDATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(TEAM_ANNOUNCEMENT_UPDATE_SUCCESS.getMessage()));
    }

    @GetMapping("/{teamId}/team-page")
    public ResponseEntity<ApiResponse<?>> getCalenderInformation(
            @LoginMember Member member,
            @ModelAttribute @Valid CalenderSubjectRequest calenderSubjectRequest,
            @PathVariable("teamId") Long teamId
    ) {
        TeamPageResponse subjectArticlesUsingCalender = articleService.getSubjectArticlesUsingCalender(
                member,
                teamId,
                calenderSubjectRequest
        );

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(subjectArticlesUsingCalender));
    }

    @DeleteMapping("/{teamId}/members/me")
    public ResponseEntity<ApiResponse<?>> removeMemberFromTeam(
            @LoginMember Member member,
            @PathVariable("teamId") Long teamId
    ){
        teamMemberService.removeTeamMember(member, teamId);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(TEAM_LEAVE_SUCCESS.getMessage()));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<ApiResponse<?>> editTeamInfo(
            @RequestBody @Valid TeamInfoEditRequest teamInfoEditRequest,
            @LoginMember Member member,
            @PathVariable("teamId") Long teamId
    ){
         Team updatedTeam = teamService.updateTeamInfo(teamInfoEditRequest, member, teamId);

        return ResponseEntity.status(TEAM_EDIT_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(teamService.getTeamInfoResponse(updatedTeam)));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<ApiResponse<?>> deleteTeam(
            @PathVariable("teamId") Long teamId,
            @LoginMember Member member
    ) {
        Team team = teamService.getTeamByTeamId(teamId);
        teamService.deleteTeamByOwner(member, team);

        return ResponseEntity.status(TEAM_DELETE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(TEAM_DELETE_SUCCESS.getMessage()));
    }


    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<?>> getTeamInfo(
            @PathVariable("teamId") Long teamId,
            @LoginMember Member member
    ){
        Team team = teamService.getTeamInfo(teamId, member);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(teamService.getTeamInfoResponse(team)));
    }

    @PostMapping("/{teamId}/team-manager")
    public ResponseEntity<ApiResponse<?>> addTeamManager(
            @LoginMember Member member,
            @RequestBody @Valid TeamManagerUpdateRequest teamManagerUpdateRequest,
            @PathVariable("teamId") Long teamId
    ){
        Boolean addTeamManager = teamMemberService.addTeamManager(teamId, member, teamManagerUpdateRequest.memberInfo());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("팀장 등록 성공"));
    }

    @PostMapping("/{teamId}/team-manager/demotion")
    public ResponseEntity<ApiResponse<?>> demoteTeamManager(
            @LoginMember Member member,
            @RequestBody @Valid TeamManagerUpdateRequest teamManagerUpdateRequest,
            @PathVariable("teamId") Long teamId
    ){
        Boolean demoteTeamManager = teamMemberService.demoteTeamManager(teamId, member, teamManagerUpdateRequest.memberInfo());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("팀장 강등 성공"));
    }

    @PutMapping("/{teamId}/team-manager")
    public ResponseEntity<ApiResponse<?>> transferTeamManager(
            @LoginMember Member member,
            @RequestBody @Valid TeamManagerUpdateRequest teamManagerUpdateRequest,
            @PathVariable("teamId") Long teamId
    ){
        Boolean transferTeamManager = teamMemberService.transferTeamManager(teamId, member, teamManagerUpdateRequest.memberInfo());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("팀장 위임 성공"));
    }

    @PostMapping("/{teamId}/remove/team-member")
    public ResponseEntity<ApiResponse<?>> removeTeamMember(
            @LoginMember Member member,
            @RequestBody @Valid TeamManagerUpdateRequest teamManagerUpdateRequest,
            @PathVariable("teamId") Long teamId
    ){
        teamMemberService.removeMemberByManger(teamId, member, teamManagerUpdateRequest.memberInfo());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("팀원 내보내기 성공"));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<?>> getTeamMembers(
            @LoginMember Member member,
            @PathVariable("teamId") Long teamId
    ){
        List<TeamMemberResponse> teamMemberResponses = teamMemberService.getTeamMembersByTeamId(teamId, member)
                .stream()
                .map(teamMemberService::getTeamMemberResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithData(teamMemberResponses));
    }
}


