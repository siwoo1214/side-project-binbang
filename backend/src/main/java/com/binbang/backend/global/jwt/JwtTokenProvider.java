package com.binbang.backend.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 - 로그인 성공 시 Access Token, Refresh Token 생성
 * JWT 토큰 검증 - 토큰이 유효한지, 만료되었는지 확인
 * 토큰에서 정보 추출 - 토큰에서 사용자 이메일, 권한 가져오기
 */

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ){
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     * @param 사용자 email,role
     * @return 생성된 access token
     */
    public String createAccessToken(String email, String role){
        Date now = new Date();
        Date expiration = new Date(now.getTime()+accessTokenExpiration);
        // 지금 시간에서 만료기간 설정한거 더해서 만료기간 설정

        return Jwts.builder()
                .subject(email)  //누구의 토큰인가
                .claim("role",role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey,Jwts.SIG.HS256) //비밀키+서명 알고리즘으로 사인함
                .compact();
    }

    /**
     * Refresh Token 생성
     * @param 사용자 email,role
     * @return 생성된 refresh token
     */
    public String createRefreshToken(String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("type", "refresh")               // Refresh Token 표시
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰에서 사용자 이메일 추출
    public String getEmailFromToken(String token){
        return parseClaims(token).getSubject();
    }

    // 토큰에서 권한(role) 추출
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 토큰 유효성 검증
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 토큰입니다: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("서명이 유효하지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("토큰이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }

    // 토큰 파싱 (claims 추출)
    // secret key로 인증 후 클레임들을 갖고옴
    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
