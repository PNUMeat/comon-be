package site.codemonster.comon.domain.util;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import site.codemonster.comon.domain.auth.entity.Member;

import java.util.Collections;
import java.util.List;

public final class TestSecurityContextInjector {

    public static void inject(Member member) {

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(member.getRole()));

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(member, null, authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }
}
