package com.binbang.backend.global.jwt;

import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 * - 모든 HTTP 요청에서 JWT 토큰을 검증
 * - 유효한 토큰이면 Spring Security Context에 인증 정보 저장
 * - OncePerRequestFilter : 요청당 한 번만 실행되는 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException{

        try{
            // 1. Reqeust Header에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);

            // 2. 토큰이 있고 유효한지 검증
            if(token != null && jwtTokenProvider.validateToken(token)){

                // 3. 토큰에서 사용자 정보 추출
                String email = jwtTokenProvider.getEmailFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                // 4. Spring Security 인증 객체 생성
                // SimpleGrantedAuthority : Spring Security의 권한 표현 방식
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_"+role));

                // UsernamePasswordAuthenticationToken : Spring Security의 인증 토큰
                // principal : 인증된 사용자 (여기 케이스에서는 이메일)
                // credentials : 자격증명 (비밀번호 등, but JWT에서는 null)
                // authorities : 권한 목록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                // 요청의 세부 정보 설정 (IP 주소, 세션 ID 등)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 5. SecurityContext에 인증 정보 저장
                // 이후 컨트롤러에서 @AuthenticationPrincipal로 사용자 정보 접근 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공 : email=${}, role=${}",email,role);
            }

        }catch (Exception e){
            log.error("JWT 인증 실패 : ${}",e.getMessage());
        }

        // 6. 다음 필터로 요청 전달
        filterChain.doFilter(request,response);
    }

    // HTTP Request Header에서 JWT 토큰 추출
    private String extractTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        // "Bearer"로 시작하는지 확인하고 제거한 다음 토큰 부분만 추출
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }

}
