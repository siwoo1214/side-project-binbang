package com.binbang.backend.member.repository;

import com.binbang.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
}
