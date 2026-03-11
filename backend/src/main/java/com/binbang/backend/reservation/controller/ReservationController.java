package com.binbang.backend.reservation.controller;

import com.binbang.backend.reservation.dto.request.ReservationCreateRequest;
import com.binbang.backend.reservation.dto.response.ReservationListResponse;
import com.binbang.backend.reservation.dto.response.ReservationResponse;
import com.binbang.backend.reservation.entity.ReservationStatus;
import com.binbang.backend.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 예약 REST API Controller
 * - 예약 생성, 조회, 취소
 * - 게스트용 API
 * - 호스트용 API
 */
@Slf4j
@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 생성
     *
     * POST /api/reservation
     *
     * @param email 로그인한 회원 정보 (Spring Security)
     * @param request 예약 생성 요청
     * @return 생성된 예약 정보
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ReservationCreateRequest request) {


        log.info("예약 생성 요청: email={}, accommodationId={}, checkIn={}, checkOut={}",
                email, request.getAccommodationId(), request.getCheckInDate(), request.getCheckOutDate());

        ReservationResponse response = reservationService.createReservation(email, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 예약 목록 조회 (게스트용)
     *
     * GET /api/reservation/my
     * GET /api/reservation/my?status=RESERVED
     *
     * @param email 로그인한 회원 정보
     * @param status 예약 상태 (선택사항)
     * @return 내 예약 목록
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReservationListResponse>> getMyReservations(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) ReservationStatus status) {

        log.info("내 예약 목록 조회: email={}, status={}", email, status);

        List<ReservationListResponse> reservations;

        if (status != null) {
            // 특정 상태의 예약만 조회
            reservations = reservationService.getMyReservationsByStatus(email, status);
        } else {
            // 전체 예약 조회
            reservations = reservationService.getMyReservations(email);
        }

        return ResponseEntity.ok(reservations);
    }

    /**
     * 예약 상세 조회
     *
     * GET /api/reservation/{reservationId}
     *
     * @param email 로그인한 회원 정보
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationDetail(
            @AuthenticationPrincipal String email,
            @PathVariable Long reservationId) {

        log.info("예약 상세 조회: email={}, reservationId={}", email, reservationId);

        ReservationResponse response = reservationService.getReservationDetail(email, reservationId);

        return ResponseEntity.ok(response);
    }

    /**
     * 예약 취소
     *
     * DELETE /api/reservation/{reservationId}
     *
     * @param email 로그인한 회원 정보
     * @param reservationId 취소할 예약 ID
     * @return 취소된 예약 정보
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @AuthenticationPrincipal String email,
            @PathVariable Long reservationId) {

        log.info("예약 취소 요청: email={}, reservationId={}", email, reservationId);

        ReservationResponse response = reservationService.cancleReservation(email, reservationId);

        return ResponseEntity.ok(response);
    }

    /**
     * 호스트용 예약 관리 - 내 숙소의 예약 목록 조회
     *
     * GET /api/reservation/host/{accommodationId}
     * GET /api/reservation/host/{accommodationId}?status=RESERVED
     *
     * @param email 로그인한 호스트 정보
     * @param accommodationId 숙소 ID
     * @param status 예약 상태 (선택사항)
     * @return 해당 숙소의 예약 목록
     */
    @GetMapping("/host/{accommodationId}")
    public ResponseEntity<List<ReservationListResponse>> getHostReservations(
            @AuthenticationPrincipal String email,
            @PathVariable Long accommodationId,
            @RequestParam(required = false) ReservationStatus status) {

        log.info("호스트 예약 목록 조회: email={}, accommodationId={}, status={}",
                email, accommodationId, status);

        List<ReservationListResponse> reservations;

        if (status != null) {
            // 특정 상태의 예약만 조회
            reservations = reservationService.getHostReservationsByStatus(email, accommodationId, status);
        } else {
            // 전체 예약 조회
            reservations = reservationService.getHostReservations(email, accommodationId);
        }

        return ResponseEntity.ok(reservations);
    }

    /**
     * 예약 완료 처리 (수동)
     *
     * PUT /api/reservation/{reservationId}/complete
     *
     * @param reservationId 완료 처리할 예약 ID
     * @return 완료 처리된 예약 정보
     */
    @PutMapping("/{reservationId}/complete")
    public ResponseEntity<ReservationResponse> completeReservation(
            @PathVariable Long reservationId) {

        log.info("예약 완료 처리 요청: reservationId={}", reservationId);

        ReservationResponse response = reservationService.completeReservation(reservationId);

        return ResponseEntity.ok(response);
    }

    /**
     * 체크아웃 날짜 지난 예약 일괄 완료 처리 (관리자용)
     *
     * PUT /api/reservation/complete-expired
     *
     * @return 완료 처리된 예약 개수
     */
    @PutMapping("/complete-expired")
    public ResponseEntity<String> completeExpiredReservations() {

        log.info("체크아웃 날짜 지난 예약 일괄 완료 처리 요청");

        int count = reservationService.completeExpiredReservations();

        return ResponseEntity.ok(String.format("완료 처리된 예약: %d건", count));
    }
}