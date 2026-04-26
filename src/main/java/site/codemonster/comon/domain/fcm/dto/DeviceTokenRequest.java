package site.codemonster.comon.domain.fcm.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
        @NotBlank
        String token
) {
}
