package com.binbang.backend.global.security.oauth2;

import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.entity.MemberRole;
import com.binbang.backend.member.entity.MemberStatus;
import com.binbang.backend.member.entity.ProviderType;
import com.binbang.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{

        // 1. OAuth2 제공자로부터 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 어떤 OAuth2 제공자인지 확인 (google,kakao)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 로그인 : registration id={}",registrationId);

        // 3. OAuth2Info 객체 생성 (google/kakao 구분)
        OAuth2UserInfo oAuth2UserInfo = null;
        if(registrationId.equals("google")){
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 제공자 입니다");
        }

        // 4. 사용자 정보 추출
        String providerId = oAuth2UserInfo.getProviderId();
        String provider = oAuth2UserInfo.getProvider();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        log.info("OAuth2 사용자 정보 - email: {}, name: {}, provider: {}, providerId: {}",
                email, name, provider, providerId);

        // 5. 이메일로 기존 회원 찾기
        Member member = memberRepository.findByEmail(email)
                .orElse(null); //없는게 문제된는게 아니니까 그냥 null로

        if (member == null) {
            // 6-1. 신규 회원 등록
            log.info("신규 OAuth2 회원 등록: {}", email);
            member = new Member();
            member.setEmail(email);
            member.setName(name);
            member.setProvider(ProviderType.valueOf(provider.toUpperCase()));
            member.setProviderId(providerId);
            member.setRole(MemberRole.USER);
            member.setStatus(MemberStatus.ACTIVE);
            memberRepository.save(member);
        } else {
            // 6-2. 기존 회원 정보 업데이트
            log.info("기존 OAuth2 회원 로그인: {}", email);
            member.updateFromOAuth2(name);
            memberRepository.save(member);
        }

        // 7. OAuth2User 객체 반환 (Spring Security가 사용)
        return oAuth2User;
    }

}
