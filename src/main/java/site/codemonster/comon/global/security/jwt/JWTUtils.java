package site.codemonster.comon.global.security.jwt;


import site.codemonster.comon.global.error.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static site.codemonster.comon.domain.auth.constant.AuthConstant.*;

@Component
@Getter
public class JWTUtils {
    private final SecretKey secretKey;
//    private final Long ACCESS_TOKEN_TIME = 60 * 25 * 1000L;
    private final Long ACCESS_TOKEN_TIME = 10 * 1000L; // 트러블 슈팅 위해서 10초로 수정
    private final Long REFRESH_TOKEN_TIME = 60 * 60 * 24 * 1000L;

    public JWTUtils(@Value("${spring.jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String generateAccessToken(String uuid, String role) {

        return Jwts.builder()
                .claim(CATEGORY, ACCESS_TOKEN)
                .claim(UUID, uuid)
                .claim(ROLE, role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String uuid, String role) {

        return Jwts.builder()
                .claim(CATEGORY, REFRESH_TOKEN)
                .claim(UUID, uuid)
                .claim(ROLE, role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_TIME))
                .signWith(secretKey)
                .compact();
    }

    public JWTInformation getJWTInformation(String token) {
        return JWTInformation.from(getCategory(token), getUUID(token), getRole(token));
    }

    public Optional<ErrorCode> validationToken(String token) {
        try{
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return Optional.empty();
        }catch (ExpiredJwtException e) {
            return Optional.of(ErrorCode.TOKEN_EXPIRED_ERROR);
        }catch (Exception e) {
            return Optional.of(ErrorCode.TOKEN_ERROR);
        }
    }

    private String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("category", String.class);
    }

    private String getUUID(String uuid) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(uuid).getPayload()
                .get("uuid", String.class);

    }

    private String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
