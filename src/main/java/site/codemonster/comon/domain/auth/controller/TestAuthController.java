package site.codemonster.comon.domain.auth.controller;

import site.codemonster.comon.global.error.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestAuthController {

    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> authorization() {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("인증된 회원입니다."));
    }

    @GetMapping("/no-auth")
    public ResponseEntity<ApiResponse<Void>> noAuthorization() {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage("인증 필요없는 컨트롤러 입니다."));
    }

}
