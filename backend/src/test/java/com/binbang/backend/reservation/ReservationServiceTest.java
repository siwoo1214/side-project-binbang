package com.binbang.backend.reservation;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationStatus;
import com.binbang.backend.accommodation.exception.AccommodationNotFoundException;
import com.binbang.backend.accommodation.repository.AccommodationRepository;
import com.binbang.backend.chat.service.ChatService;
import com.binbang.backend.global.exception.CustomException;
import com.binbang.backend.global.service.MessageProducer;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.exception.MemberNotFoundException;
import com.binbang.backend.member.repository.MemberRepository;
import com.binbang.backend.reservation.dto.request.ReservationCreateRequest;
import com.binbang.backend.reservation.dto.response.ReservationResponse;
import com.binbang.backend.reservation.entity.Reservation;
import com.binbang.backend.reservation.entity.ReservationStatus;
import com.binbang.backend.reservation.exception.AccommodationNotAvailableException;
import com.binbang.backend.reservation.exception.InvalidReservationDateException;
import com.binbang.backend.reservation.exception.ReservationAlreadyCancelledException;
import com.binbang.backend.reservation.exception.ReservationNotFoundException;
import com.binbang.backend.reservation.repository.ReservationRepository;
import com.binbang.backend.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ReservationService 단위 테스트
 *
 * [테스트 전략]
 * - DB, 외부 API 없이 순수 비즈니스 로직만 검증
 * - 의존성은 Mockito @Mock으로 가짜 객체 주입
 * - @InjectMocks가 가짜 객체들을 ReservationService에 자동 주입
 *
 * [테스트 구조 - @Nested로 기능별 그룹화]
 * - CreateReservationTest : 예약 생성 관련 케이스
 * - CancelReservationTest : 예약 취소 관련 케이스
 * - PriceCalculationTest  : 가격 계산 관련 케이스
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    // ===== Mock 객체 선언 =====
    // @Mock: 실제 구현체 대신 가짜 객체 생성
    // 메서드 호출 시 기본값(null, false, 0 등) 반환
    @Mock private ReservationRepository reservationRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private AccommodationRepository accommodationRepository;
    @Mock private MessageProducer messageProducer;
    @Mock private ChatService chatService;

    // @InjectMocks: 위 @Mock 객체들을 생성자/필드 주입으로 자동 주입
    @InjectMocks
    private ReservationService reservationService;

    // ===== 공통 테스트 데이터 =====
    private Member host;
    private Member guest;
    private Accommodation accommodation;

    /**
     * 각 테스트 실행 전에 공통 데이터 초기화
     * - 매 테스트마다 새로 생성해서 테스트 간 데이터 오염 방지
     */
    @BeforeEach
    void setUp() {
        // 호스트 (숙소 소유자)
        host = new Member();
        host.setMemberId(1L);
        host.setEmail("host@test.com");
        host.setName("테스트호스트");
        host.setPhone("010-1111-1111");

        // 게스트 (예약자)
        guest = new Member();
        guest.setMemberId(2L);
        guest.setEmail("guest@test.com");
        guest.setName("테스트게스트");
        guest.setPhone("010-2222-2222");

        // 숙소 (호스트 소유, OPEN 상태, 1박 100,000원)
        accommodation = new Accommodation();
        accommodation.setAccommodationId(10L);
        accommodation.setMember(host);
        accommodation.setName("테스트펜션");
        accommodation.setAddress("제주도 테스트로 123");
        accommodation.setPrice(100_000L);
        accommodation.setStatus(AccommodationStatus.OPEN);
    }

    // ======================================================
    // 1. 예약 생성 테스트 그룹
    // ======================================================
    @Nested
    @DisplayName("예약 생성 테스트")
    class CreateReservationTest {

        @Test
        @DisplayName("[성공] 정상적인 예약 생성")
        void createReservation_success() {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L,
                    LocalDate.now().plusDays(1),  // 내일 체크인
                    LocalDate.now().plusDays(4),  // 4일 후 체크아웃 → 3박
                    2
            );

            given(memberRepository.findByEmail("guest@test.com"))
                    .willReturn(Optional.of(guest));
            given(accommodationRepository.findById(10L))
                    .willReturn(Optional.of(accommodation));
            given(reservationRepository.existsOverlappingReservation(any(), any(), any(), any()))
                    .willReturn(false);
            given(reservationRepository.save(any(Reservation.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(chatService).createChatRoomForReservation(any());
            doNothing().when(messageProducer).sendReservationConfirmation(any(), any());
            doNothing().when(messageProducer).sendNewReservationNotification(any(), any());

            // when
            ReservationResponse result = reservationService.createReservation("guest@test.com", request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalPrice()).isEqualTo(300_000L); // 100,000 × 3박
            assertThat(result.getNights()).isEqualTo(3L);
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVED);
            assertThat(result.getGuestName()).isEqualTo("테스트게스트");
            assertThat(result.getAccommodationName()).isEqualTo("테스트펜션");
            verify(reservationRepository, times(1)).save(any(Reservation.class));
            verify(chatService, times(1)).createChatRoomForReservation(any());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 회원으로 예약 시도")
        void createReservation_fail_memberNotFound() {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2
            );

            given(memberRepository.findByEmail("nobody@test.com"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("nobody@test.com", request)
            ).isInstanceOf(MemberNotFoundException.class);

            // 회원 조회 실패 시 숙소 조회, 저장은 실행되면 안 됨
            verify(accommodationRepository, never()).findById(any());
            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 숙소로 예약 시도")
        void createReservation_fail_accommodationNotFound() {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    999L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2
            );

            given(memberRepository.findByEmail("guest@test.com"))
                    .willReturn(Optional.of(guest));
            given(accommodationRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("guest@test.com", request)
            ).isInstanceOf(AccommodationNotFoundException.class);

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("[실패] 체크인 날짜가 체크아웃 날짜보다 늦음")
        void createReservation_fail_checkInAfterCheckOut() {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L,
                    LocalDate.now().plusDays(5), // 체크인이 더 늦음
                    LocalDate.now().plusDays(2), // 체크아웃이 더 이름
                    2
            );

            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(accommodationRepository.findById(10L)).willReturn(Optional.of(accommodation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("guest@test.com", request)
            ).isInstanceOf(InvalidReservationDateException.class);
        }

        @Test
        @DisplayName("[실패] 체크인과 체크아웃이 같은 날짜 (0박)")
        void createReservation_fail_sameDate() {
            // given
            LocalDate sameDate = LocalDate.now().plusDays(3);
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L, sameDate, sameDate, 2 // 체크인 = 체크아웃
            );

            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(accommodationRepository.findById(10L)).willReturn(Optional.of(accommodation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("guest@test.com", request)
            ).isInstanceOf(InvalidReservationDateException.class);
        }

        @Test
        @DisplayName("[실패] 과거 날짜로 체크인 시도")
        void createReservation_fail_pastCheckIn() {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L,
                    LocalDate.now().minusDays(1), // 어제 (과거)
                    LocalDate.now().plusDays(2),
                    2
            );

            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(accommodationRepository.findById(10L)).willReturn(Optional.of(accommodation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("guest@test.com", request)
            ).isInstanceOf(InvalidReservationDateException.class);
        }

        @Test
        @DisplayName("[실패] 숙소 상태가 OPEN이 아님 (CLOSED)")
        void createReservation_fail_accommodationNotOpen() {
            // given
            accommodation.setStatus(AccommodationStatus.CLOSE);

            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2
            );

            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(accommodationRepository.findById(10L)).willReturn(Optional.of(accommodation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("guest@test.com", request)
            ).isInstanceOf(AccommodationNotAvailableException.class);
        }

        @Test
        @DisplayName("[실패] 자기 소유 숙소를 본인이 예약 시도")
        void createReservation_fail_ownAccommodation() {
            // given - host가 자기 숙소(accommodation)를 예약하려는 상황
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 1
            );

            given(memberRepository.findByEmail("host@test.com")).willReturn(Optional.of(host));
            given(accommodationRepository.findById(10L)).willReturn(Optional.of(accommodation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("host@test.com", request)
            ).isInstanceOf(CustomException.class)
                    .hasMessageContaining("자신의 숙소는 예약할 수 없습니다");
        }

        @Test
        @DisplayName("[실패] 해당 날짜에 이미 예약이 존재함 (날짜 중복)")
        void createReservation_fail_dateConflict() {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2
            );

            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(accommodationRepository.findById(10L)).willReturn(Optional.of(accommodation));
            given(reservationRepository.existsOverlappingReservation(any(), any(), any(), any()))
                    .willReturn(true); // 중복 있음!

            // when & then
            assertThatThrownBy(() ->
                    reservationService.createReservation("guest@test.com", request)
            ).isInstanceOf(AccommodationNotAvailableException.class);

            // 중복이므로 save 절대 호출되면 안 됨
            verify(reservationRepository, never()).save(any());
        }
    }

    // ======================================================
    // 2. 예약 취소 테스트 그룹
    // ======================================================
    @Nested
    @DisplayName("예약 취소 테스트")
    class CancelReservationTest {

        private Reservation reservation;

        @BeforeEach
        void setUpReservation() {
            reservation = new Reservation();
            reservation.setReservationId(100L);
            reservation.setMember(guest);
            reservation.setAccommodation(accommodation);
            reservation.setCheckInDate(LocalDate.now().plusDays(5));
            reservation.setCheckOutDate(LocalDate.now().plusDays(7));
            reservation.setPersonnel(2);
            reservation.setTotalPrice(200_000L);
            reservation.setStatus(ReservationStatus.RESERVED);
        }

        @Test
        @DisplayName("[성공] 정상적인 예약 취소")
        void cancelReservation_success() {
            // given
            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(reservationRepository.findByReservationIdAndMember_MemberId(100L, 2L))
                    .willReturn(Optional.of(reservation));
            doNothing().when(messageProducer).sendCancellationNotification(any(), any());

            // when
            ReservationResponse result = reservationService.cancleReservation("guest@test.com", 100L);

            // then
            assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 예약 취소 시도")
        void cancelReservation_fail_notFound() {
            // given
            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(reservationRepository.findByReservationIdAndMember_MemberId(999L, 2L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    reservationService.cancleReservation("guest@test.com", 999L)
            ).isInstanceOf(ReservationNotFoundException.class);
        }

        @Test
        @DisplayName("[실패] 이미 취소된 예약을 다시 취소 시도")
        void cancelReservation_fail_alreadyCancelled() {
            // given
            reservation.setStatus(ReservationStatus.CANCELLED);

            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(reservationRepository.findByReservationIdAndMember_MemberId(100L, 2L))
                    .willReturn(Optional.of(reservation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.cancleReservation("guest@test.com", 100L)
            ).isInstanceOf(ReservationAlreadyCancelledException.class);
        }

        @Test
        @DisplayName("[실패] 이미 완료된 예약은 취소 불가")
        void cancelReservation_fail_alreadyCompleted() {
            // given
            reservation.setStatus(ReservationStatus.COMPLETED);

            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(reservationRepository.findByReservationIdAndMember_MemberId(100L, 2L))
                    .willReturn(Optional.of(reservation));

            // when & then
            assertThatThrownBy(() ->
                    reservationService.cancleReservation("guest@test.com", 100L)
            ).isInstanceOf(CustomException.class)
                    .hasMessageContaining("이미 완료된 예약은 취소할 수 없습니다");
        }
    }

    // ======================================================
    // 3. 가격 계산 테스트 그룹
    // ======================================================
    @Nested
    @DisplayName("가격 계산 테스트")
    class PriceCalculationTest {

        /**
         * 가격 계산은 private 메서드라 직접 호출 불가
         * → 예약 생성 결과의 totalPrice로 간접 검증
         */
        private void mockDependencies() {
            given(memberRepository.findByEmail("guest@test.com")).willReturn(Optional.of(guest));
            given(accommodationRepository.findById(10L)).willReturn(Optional.of(accommodation));
            given(reservationRepository.existsOverlappingReservation(any(), any(), any(), any()))
                    .willReturn(false);
            given(reservationRepository.save(any(Reservation.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            doNothing().when(chatService).createChatRoomForReservation(any());
            doNothing().when(messageProducer).sendReservationConfirmation(any(), any());
            doNothing().when(messageProducer).sendNewReservationNotification(any(), any());
        }

        @Test
        @DisplayName("1박 예약 가격 계산 (100,000 × 1박 = 100,000)")
        void price_oneNight() {
            mockDependencies();
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), 1
            );

            ReservationResponse result = reservationService.createReservation("guest@test.com", request);

            assertThat(result.getTotalPrice()).isEqualTo(100_000L);
            assertThat(result.getNights()).isEqualTo(1L);
        }

        @Test
        @DisplayName("7박 예약 가격 계산 (100,000 × 7박 = 700,000)")
        void price_sevenNights() {
            mockDependencies();
            ReservationCreateRequest request = new ReservationCreateRequest(
                    10L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(8), 2
            );

            ReservationResponse result = reservationService.createReservation("guest@test.com", request);

            assertThat(result.getTotalPrice()).isEqualTo(700_000L);
            assertThat(result.getNights()).isEqualTo(7L);
        }
    }
}