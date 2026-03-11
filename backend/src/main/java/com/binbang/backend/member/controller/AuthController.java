package com.binbang.backend.member.controller;

import com.binbang.backend.member.dto.request.LoginRequest;
import com.binbang.backend.member.dto.request.SignupRequest;
import com.binbang.backend.member.dto.response.AuthResponse;
import com.binbang.backend.member.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 관련 API 컨트롤러
 * - 회원가입, 로그인, 로그아웃, 토큰 재발급
 */

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     * POST /api/auth/signup
     *
     * @param request 회원가입 요청 (이메일, 비밀번호, 이름, 전화번호)
     * @return 성공 메시지
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(
            @Valid
            @RequestBody
            SignupRequest request
            ) {
        String message = authService.signup(request);

        Map<String, String> response = new HashMap<>();
        response.put("message",message);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인 API
     * POST /api/auth/login
     *
     * @param request 로그인 요청 (이메일, 비밀번호)
     * @return JWT 토큰 (Access Token, Refresh Token)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ){
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃 API
     * POST /api/auth/logout
     *
     * @param email 현재 로그인한 사용자 이메일 (JWT에서 추출)
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal String email
    ){
        authService.logout(email);
        Map<String, String> resp = new HashMap<>();
        resp.put("message","로그아웃 되었습니다.");

        return ResponseEntity.ok(resp);
    }

    /**
     * Access Token 재발급 API
     * POST /api/auth/refresh
     *
     * @param request Refresh Token 요청
     * @return 새로운 Access Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody
            Map<String, String> request
    ){
        String refreshToken = request.get("refreshToken");

        if(refreshToken==null || refreshToken.isBlank()){
            throw new RuntimeException("Refresh Token이 필요합니다");
        }

        AuthResponse resp = authService.refrehAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

    /**
     * 현재 로그인한 사용자 정보 조회 (테스트용)
     * GET /api/auth/me
     *
     * @param email 현재 로그인한 사용자 이메일
     * @return 사용자 이메일
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(
            @AuthenticationPrincipal String email
    ) {
        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("message", "인증된 사용자입니다");

        return ResponseEntity.ok(response);
    }

}
