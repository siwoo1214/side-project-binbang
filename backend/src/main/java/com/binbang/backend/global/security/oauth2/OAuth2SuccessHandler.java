package com.binbang.backend.global.security.oauth2;

import com.binbang.backend.global.jwt.JwtTokenProvider;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // vite는 5173포트를 쓰기 때문에 기존 3000에서 수정해야함
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. OAuth2User 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = extractEmail(oAuth2User);

        log.info("OAuth2 로그인 성공: {}", email);

        // DB에서 회원 정보 조회해서 role 가져오기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다: " + email));
        String role = member.getRole().name();
        // 2. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(email,role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // 3. Refresh Token을 Redis에 저장
        redisTemplate.opsForValue().set(
                "RT:" + email,
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        log.info("JWT 토큰 발급 완료: {}", email);

        // 4. 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("memberId", member.getMemberId())
                .build()
                .toUriString();

        log.info("리다이렉트: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 이메일 추출 헬퍼 메서드 (Google, Kakao 모두 처리)
     * kakao와 google이 응답하는 형태가 다르기 때문에 email의 위치도 다르다
     *
     * Kakao 응답 구조
     * {
     *   "id": 4723577307,
     *   "kakao_account": {
     *     "email": "parksiwoo1214@naver.com"  // ← 여기 있음!
     *   }
     * }
     *
     * google 응답 구조
     * {
     *   "sub": "123456789",
     *   "email": "user@gmail.com"  // ← 최상위에 있음
     * }
     */

    private String extractEmail(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Google: 최상위에 email 있음
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }

        // Kakao: kakao_account.email
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                return (String) kakaoAccount.get("email");
            }
        }

        throw new RuntimeException("OAuth2 사용자 정보에서 이메일을 찾을 수 없습니다.");
    }

}
