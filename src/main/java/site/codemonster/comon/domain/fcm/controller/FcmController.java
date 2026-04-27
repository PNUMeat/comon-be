package site.codemonster.comon.domain.fcm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.fcm.dto.DeviceTokenRequest;
import site.codemonster.comon.domain.fcm.service.FcmService;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/tokens")
    public ResponseEntity<Void> addToken(@Valid @RequestBody DeviceTokenRequest deviceTokenRequest,
                                         @AuthenticationPrincipal Member member){
        fcmService.addDeviceToken(member, deviceTokenRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
