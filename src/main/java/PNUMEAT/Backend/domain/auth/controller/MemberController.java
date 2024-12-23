package PNUMEAT.Backend.domain.auth.controller;

import PNUMEAT.Backend.domain.auth.dto.request.MemberProfileCreateRequest;
import PNUMEAT.Backend.domain.auth.dto.request.MemberProfileUpdateRequest;
import PNUMEAT.Backend.domain.auth.dto.response.MemberProfileResponse;
import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.auth.service.MemberService;
import PNUMEAT.Backend.global.error.dto.response.ApiResponse;
import PNUMEAT.Backend.global.security.annotation.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createMemberProfile(@ModelAttribute @Valid MemberProfileCreateRequest memberProfileCreateRequest,
                                                              @RequestPart(value = "image", required = false) MultipartFile image,
                                                              @LoginMember Member member){
        memberService.createMemberProfile(memberProfileCreateRequest, image, member);

        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse("회원을 성공적으로 등록했습니다."));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateMemberProfile(@ModelAttribute @Valid MemberProfileUpdateRequest memberProfileUpdateRequest,
                                                              @RequestPart(value = "image", required = false) MultipartFile image,
                                                              @LoginMember Member member){

        Member updatedMember = memberService.updateMemberProfile(memberProfileUpdateRequest, image, member);

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
}
