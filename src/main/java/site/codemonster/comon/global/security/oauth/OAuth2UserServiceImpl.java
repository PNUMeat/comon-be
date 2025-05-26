package site.codemonster.comon.global.security.oauth;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.global.security.oauth.oauthResponse.KakaoResponse;
import site.codemonster.comon.global.security.oauth.oauthResponse.OAuth2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static site.codemonster.comon.domain.auth.constant.AuthConstant.KAKAO;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        if (registrationId.equals(KAKAO)) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String memberUniqueId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        Optional<Member> memberOptional = memberRepository.findByMemberUniqueId(memberUniqueId);

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();

            return new OAuth2UserImpl(member);
        }

        Member member = new Member(oAuth2Response.getEmail(), memberUniqueId, "ROLE_USER");

        memberRepository.save(member);

        return new OAuth2UserImpl(member);
    }
}
