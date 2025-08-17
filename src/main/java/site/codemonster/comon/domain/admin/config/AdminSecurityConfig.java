package site.codemonster.comon.domain.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 관리자 시스템용 보안 설정
 * 기존 SecurityConfig와 분리하여 관리
 */
@Configuration
public class AdminSecurityConfig {

    /**
     * 관리자 비밀번호 암호화용 PasswordEncoder
     * BCrypt 사용 (보안성 높음)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
