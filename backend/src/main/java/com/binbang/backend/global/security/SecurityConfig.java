package com.binbang.backend.global.security;

import com.binbang.backend.global.jwt.JwtAuthenticationFilter;
import com.binbang.backend.global.security.oauth2.CustomOAuth2UserService;
import com.binbang.backend.global.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 설정 클래스
 * - URL별 접근 권한 설정
 * - JWT 기반 인증 설정 (세션 사용 안 함)
 * - 비밀번호 암호화 설정
 * - CORS 설정
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // spring security 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                // csfr 보호 비활성화 (jwt 사용 시 필요없음)
                .csrf(csrf->csrf.disable())
                // CORS 설정 (WebSocket용 추가)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))  // 개발 환경에서는 비활성화
                // Frame Options 비활성화 (WebSocket용)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                // jwt는 세션 사용 안함
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth->auth
                        // 인증 관련 엔드포인트 (로그인 필요 안내 포함)
                        .requestMatchers("/api/auth/**").permitAll()

                        // OAuth2 로그인 관련
                        .requestMatchers("/login/**", "/oauth2/**").permitAll()

                        // WebSocket 관련 엔드포인트 (중요!)
                        .requestMatchers("/ws/**").permitAll()           // WebSocket 엔드포인트
                        .requestMatchers("/app/**").permitAll()          // STOMP 메시지 전송
                        .requestMatchers("/topic/**").permitAll()        // STOMP 구독
                        .requestMatchers("/queue/**").permitAll()        // STOMP 개인 메시지

                        // 테스트 페이지
                        .requestMatchers("/websocket-test.html").permitAll()
                        .requestMatchers("/chat-test.html").permitAll()

                        // 주소 검색 API
                        .requestMatchers("/api/address/**").permitAll()

                        // 지역 조회 API
                        .requestMatchers("/api/regions/**").permitAll()

                        // 숙소 API - 목록/상세 조회는 퍼블릭, 등록/수정/삭제는 인증 필요
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/accommodation/**").permitAll()

                        // 카테고리 조회 API (퍼블릭)
                        .requestMatchers("/api/categories/**").permitAll()

                        // S3 테스트
                        .requestMatchers("/api/s3/test/**").permitAll()

                        // 채팅 API (인증 필요)
                        // /api/chat/** 는 anyRequest().authenticated() 에 포함되므로 별도 설정 불필요

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // jwt쓰기 때문에 form 로그인 비활성화
                .formLogin(form->form.disable())
                // HTTP Basic 인증 비활성화
                .httpBasic(basic->basic.disable())
                // OAuth2 로그인 설정 추가
                .oauth2Login(o->o
                        // Spring Security가 자동 생성하는 기본 로그인 페이지 비활성화
                        // (API 서버에서는 로그인 페이지 HTML이 필요 없고, 의도치 않은 302 리다이렉트의 원인)
                        .loginPage("/api/auth/login-required")
                        .userInfoEndpoint(end->end
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                // 인증 실패 시 302 리다이렉트 대신 401 JSON 반환
                // (oauth2Login이 있으면 기본적으로 로그인 페이지로 리다이렉트하기 때문)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"인증이 필요합니다\"}");
                        })
                );

        return http.build();
    }

}
