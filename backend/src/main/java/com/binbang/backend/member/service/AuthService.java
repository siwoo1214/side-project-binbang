package com.binbang.backend.member.service;

import com.binbang.backend.global.jwt.JwtTokenProvider;
import com.binbang.backend.member.dto.request.LoginRequest;
import com.binbang.backend.member.dto.request.SignupRequest;
import com.binbang.backend.member.dto.response.AuthResponse;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.entity.MemberRole;
import com.binbang.backend.member.entity.MemberStatus;
import com.binbang.backend.member.exception.DuplicateEmailException;
import com.binbang.backend.member.exception.InvalidPasswordException;
import com.binbang.backend.member.exception.InvalidTokenException;
import com.binbang.backend.member.exception.MemberNotFoundException;
import com.binbang.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * - 기존 RuntimeException 대신 내가 만든 CustomException 적용
 * 왜냐? 에러 종류를 명확하게 구분
 *       적절한 HTTP 상태 코드 사용
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Long expireIn = 900L;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    //회원가입
    @Transactional
    public String signup(SignupRequest request){
        // 1. 이메일 중복 체크
        if(memberRepository.existsByEmail(request.getEmail())){
            throw new DuplicateEmailException(request.getEmail());
        }

        // 2. Member 엔티티 생성
        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword())); // 비밀번호 암호화
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setRole(MemberRole.USER);  // 기본 권한: USER
        member.setStatus(MemberStatus.ACTIVE);  // 기본 상태: 활성

        // 3. DB에 저장
        memberRepository.save(member);

        log.info("회원가입 완료 : email={}",request.getEmail());
        return "회원가입이 완료되었습니다 email="+member.getEmail();
    }

    // 로그인
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request){

        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new MemberNotFoundException(request.getEmail()));

        // 2. 계정 상태 확인
        if(member.getStatus()!=MemberStatus.ACTIVE){
            throw new InvalidPasswordException();
        }

        // 3. 비밀번호 검증
        if(!passwordEncoder.matches(request.getPassword(),member.getPassword())){
            throw new RuntimeException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 4. 로그인 시 JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        // 5. refresh token을 Redis에 저장 (key:email, value:refresh token 값)
        redisTemplate.opsForValue().set(
                "RT:"+member.getEmail(), //Redis key값
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        log.info("jwt 발급 및 로그인 성공 : email={}",member.getEmail());

        return AuthResponse.of(member.getMemberId(), accessToken, refreshToken, expireIn);
    }

    /**
     * 로그아웃
     * @param email
     * Redis에서 키를 삭제해서 재발급을 몬하게 만드는것임
     */
    public void logout(String email){
        redisTemplate.delete("RT:"+email);
        log.info("로그아웃 완료=email:{}",email);
    }

    // access token 재발급
    @Transactional(readOnly = true)
    public AuthResponse refrehAccessToken(String refreshToken){

        // 1. Refresh Token 유효성 검증
        if(!jwtTokenProvider.validateToken(refreshToken)){
            throw new InvalidTokenException("유요하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token에서 email 추출
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 3. Redis에 저장된 Refresh Token과 비교
        String storedRefreshToken = redisTemplate.opsForValue().get("RT:"+email);
        if(storedRefreshToken==null || !storedRefreshToken.equals(refreshToken)){
            throw new InvalidTokenException("Refresh Token이 일치하지 않습니다");
        }

        // 4. 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("회원을 찾을 수 없습니다."));

        // 5. 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(
                member.getEmail(), member.getRole().name()
        );

        log.info("Access Token 재발급 완료=Access Token:{}",newAccessToken);

        return AuthResponse.of(member.getMemberId(), newAccessToken, refreshToken, expireIn);
    }

}
