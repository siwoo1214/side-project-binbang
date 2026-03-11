package com.binbang.backend.reservation.service;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationStatus;
import com.binbang.backend.accommodation.exception.AccommodationNotFoundException;
import com.binbang.backend.accommodation.repository.AccommodationRepository;
import com.binbang.backend.chat.service.ChatService;
import com.binbang.backend.global.dto.EmailMessage;
import com.binbang.backend.global.dto.NotificationMessage;
import com.binbang.backend.global.exception.CustomException;
import com.binbang.backend.global.service.MessageProducer;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.exception.MemberNotFoundException;
import com.binbang.backend.member.repository.MemberRepository;
import com.binbang.backend.reservation.dto.request.ReservationCreateRequest;
import com.binbang.backend.reservation.dto.response.ReservationListResponse;
import com.binbang.backend.reservation.dto.response.ReservationResponse;
import com.binbang.backend.reservation.entity.Reservation;
import com.binbang.backend.reservation.entity.ReservationStatus;
import com.binbang.backend.reservation.exception.AccommodationNotAvailableException;
import com.binbang.backend.reservation.exception.InvalidReservationDateException;
import com.binbang.backend.reservation.exception.ReservationAlreadyCancelledException;
import com.binbang.backend.reservation.exception.ReservationNotFoundException;
import com.binbang.backend.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final AccommodationRepository accommodationRepository;
    private final MessageProducer messageProducer;
    private final ChatService chatService;

    /**
     * 예약 생성
     *
     * @param email 로그인한 회원 이메일 (JWT 토큰에서 추출)
     * @param request 예약 생성 요청 정보
     * @return 생성된 예약 정보
     */
    @Transactional
    public ReservationResponse createReservation(
            String email,
            ReservationCreateRequest request
    ){

        // 1. 회원조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new MemberNotFoundException(email+"님을 찾을 수 없습니다"));

        // 2. 숙소조회
        Accommodation accommodation = accommodationRepository.findById(request.getAccommodationId())
                .orElseThrow(()-> new AccommodationNotFoundException(request.getAccommodationId()));

        // 3. 예약 유효성 검증
        validateReservation(member, accommodation, request.getCheckInDate(), request.getCheckOutDate());

        // 4. 가격 계산
        Long totalPrice = calculateTotalPrice(accommodation.getPrice(), request.getCheckInDate(),request.getCheckOutDate());

        // 5. 예약 생성
        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setAccommodation(accommodation);
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setPersonnel(request.getGuestCount());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(ReservationStatus.RESERVED);

        // 6. 저장
        Reservation saveReservation = reservationRepository.save(reservation);

        // 7. 채팅방 자동 생성 (예약 완료 즉시 호스트-게스트 채팅방 개설)
        chatService.createChatRoomForReservation(saveReservation);
        log.info("채팅방 자동 생성: reservationId={}", saveReservation.getReservationId());

        EmailMessage guestEmail = buildGuestConfirmationEmail(saveReservation);
        NotificationMessage guestNotification = buildGuestConfirmationNotification(saveReservation);
        messageProducer.sendReservationConfirmation(guestEmail, guestNotification);

        EmailMessage hostEmail = buildHostNewReservationEmail(saveReservation);
        NotificationMessage hostNotification = buildHostNewReservationNotification(saveReservation);
        messageProducer.sendNewReservationNotification(hostEmail, hostNotification);

        // 7. 응답 반환
        return ReservationResponse.from(saveReservation);

    }

    /**
     * 내 예약 목록 조회 (게스트용)
     *
     * @param email 로그인한 회원 이메일 (JWT 토큰에서 추출)
     * @return 내 예약 목록
     */
    @Transactional
    public List<ReservationListResponse> getMyReservations(
            String email
    ){
        Long memberId = getMemberIdByEmail(email);

        List<Reservation> reservations = reservationRepository
                .findByMember_MemberIdOrderByReservedAtDesc(memberId);

        return reservations.stream()
                .map(ReservationListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 상태의 내 예약 목록 조회
     *
     * @param email 로그인한 회원 이메일
     * @param status 예약 상태
     * @return 특정 상태의 예약 목록
     */
    @Transactional
    public List<ReservationListResponse> getMyReservationsByStatus(String email, ReservationStatus status) {
        Long memberId = getMemberIdByEmail(email);

        List<Reservation> reservations = reservationRepository
                .findByMember_MemberIdAndStatusOrderByReservedAtDesc(memberId, status);

        log.info("내 예약 목록 조회 (상태별): email={}, status={}, count={}",
                email, status, reservations.size());

        return reservations.stream()
                .map(ReservationListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 예약 상세 조회
     *
     * @param email 로그인한 회원 이메일
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @Transactional
    public ReservationResponse getReservationDetail(
            String email, Long reservationId){
        Long memberId = getMemberIdByEmail(email);

        // 자신이 예약만 조회
        Reservation reservation = reservationRepository
                .findByReservationIdAndMember_MemberId(reservationId, memberId)
                .orElseThrow(()->new ReservationNotFoundException(reservationId));

        return ReservationResponse.from(reservation);
    }

    /**
     * 예약 취소
     *
     * @param email 로그인한 회원 이메일
     * @param reservationId 취소할 예약 ID
     * @return 취소된 예약 정보
     */
    @Transactional
    public ReservationResponse cancleReservation(String email, Long reservationId){
        Long memberId = getMemberIdByEmail(email);

        // 자신이 예약만 조회
        Reservation reservation = reservationRepository
                .findByReservationIdAndMember_MemberId(reservationId, memberId)
                .orElseThrow(()->new ReservationNotFoundException(reservationId));

        // 2. 이미 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyCancelledException(reservationId);
        }

        // 3. 이미 완료된 예약은 취소 불가
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "이미 완료된 예약은 취소할 수 없습니다");
        }

        // 4. 예약 취소
        reservation.setStatus(ReservationStatus.CANCELLED);

        // 호스트에게 취소 알림
        EmailMessage hostCancelEmail = buildHostCancellationEmail(reservation);
        NotificationMessage hostCancelNotification = buildHostCancellationNotification(reservation);
        messageProducer.sendCancellationNotification(hostCancelEmail, hostCancelNotification);

        // 게스트에게 취소 확인
        EmailMessage guestCancelEmail = buildGuestCancellationEmail(reservation);
        NotificationMessage guestCancelNotification = buildGuestCancellationNotification(reservation);
        messageProducer.sendCancellationNotification(guestCancelEmail, guestCancelNotification);

        log.info("예약 취소 완료: email={}, reservationId={}", email, reservationId);

        return ReservationResponse.from(reservation);

    }

    /**
     * 호스트용 예약 관리 - 내 숙소의 모든 예약 조회
     *
     * @param email 로그인한 호스트 이메일
     * @param accommodationId 숙소 ID
     * @return 해당 숙소의 예약 목록
     */
    @Transactional
    public List<ReservationListResponse> getHostReservations(String email, Long accommodationId) {
        Long hostMemberId = getMemberIdByEmail(email);

        // 1. 숙소 조회
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new AccommodationNotFoundException(accommodationId));

        // 2. 자신의 숙소인지 확인 (권한 체크)
        if (!accommodation.getMember().getMemberId().equals(hostMemberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "해당 숙소의 예약을 조회할 권한이 없습니다");
        }

        // 3. 숙소의 예약 목록 조회
        List<Reservation> reservations = reservationRepository
                .findByAccommodation_AccommodationIdOrderByReservedAtDesc(accommodationId);

        log.info("호스트 예약 목록 조회: email={}, accommodationId={}, count={}",
                email, accommodationId, reservations.size());

        return reservations.stream()
                .map(ReservationListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 호스트용 - 특정 상태의 예약만 조회
     *
     * @param email 로그인한 호스트 이메일
     * @param accommodationId 숙소 ID
     * @param status 예약 상태
     * @return 특정 상태의 예약 목록
     */
    @Transactional
    public List<ReservationListResponse> getHostReservationsByStatus(
            String email, Long accommodationId, ReservationStatus status) {

        Long hostMemberId = getMemberIdByEmail(email);

        // 1. 숙소 조회 및 권한 체크
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new AccommodationNotFoundException(accommodationId));

        if (!accommodation.getMember().getMemberId().equals(hostMemberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "해당 숙소의 예약을 조회할 권한이 없습니다");
        }

        // 2. 상태별 예약 조회
        List<Reservation> reservations = reservationRepository
                .findByAccommodation_AccommodationIdAndStatus(accommodationId, status);

        log.info("호스트 예약 목록 조회 (상태별): email={}, accommodationId={}, status={}, count={}",
                email, accommodationId, status, reservations.size());

        return reservations.stream()
                .map(ReservationListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 예약 완료 처리 (체크아웃 날짜가 지난 예약)
     * - 스케줄러나 수동으로 호출
     *
     * @param reservationId 예약 ID
     * @return 완료 처리된 예약 정보
     */
    @Transactional
    public ReservationResponse completeReservation(Long reservationId) {
        // 1. 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        // 2. 이미 완료된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "이미 완료된 예약입니다: " + reservationId);
        }

        // 3. 취소된 예약은 완료 처리 불가
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "취소된 예약은 완료 처리할 수 없습니다: " + reservationId);
        }

        // 4. 체크아웃 날짜가 지났는지 확인
        if (reservation.getCheckOutDate().isAfter(LocalDate.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "체크아웃 날짜가 지나지 않은 예약입니다: " + reservationId);
        }

        // 5. 예약 완료 처리
        reservation.setStatus(ReservationStatus.COMPLETED);

        log.info("예약 완료 처리: reservationId={}, checkOutDate={}",
                reservationId, reservation.getCheckOutDate());

        return ReservationResponse.from(reservation);
    }

    /**
     * 체크아웃 날짜가 지난 모든 예약 자동 완료 처리
     * - 스케줄러에서 매일 실행
     *
     * @return 완료 처리된 예약 개수
     */
    @Transactional
    public int completeExpiredReservations() {
        LocalDate today = LocalDate.now();

        // 체크아웃 날짜가 지났고, 상태가 RESERVED인 예약들 조회
        List<Reservation> expiredReservations = reservationRepository
                .findByMember_MemberIdAndStatusOrderByReservedAtDesc(null, ReservationStatus.RESERVED)
                .stream()
                .filter(r -> r.getCheckOutDate().isBefore(today))
                .collect(Collectors.toList());

        // 완료 처리
        expiredReservations.forEach(reservation -> {
            reservation.setStatus(ReservationStatus.COMPLETED);
            log.info("자동 완료 처리: reservationId={}, checkOutDate={}",
                    reservation.getReservationId(), reservation.getCheckOutDate());
        });

        int count = expiredReservations.size();
        log.info("체크아웃 날짜 지난 예약 자동 완료: count={}", count);

        return count;
    }


    //---------------유틸리티 메소드----------------

    // 예약 유혀성 검증
    private void validateReservation(Member member, Accommodation accommodation,
                                     LocalDate checkInDate, LocalDate checkOutDate){

        // 1. 날짜 유효성 검증
        if(checkInDate.isAfter(checkOutDate) || checkInDate.isEqual(checkOutDate)){
            throw new InvalidReservationDateException(checkInDate,checkOutDate);
        }

        // 2. 과거 날짜 체크
        if(checkInDate.isBefore(LocalDate.now())){
            throw new InvalidReservationDateException(checkInDate);
        }

        // 3. 숙소 상태 확인 (예약 가능한 상태인지)
        if (accommodation.getStatus() != AccommodationStatus.OPEN) {
            throw new AccommodationNotAvailableException();
        }

        // 4. 내꺼는 예약 못함
        if(accommodation.getMember().getMemberId().equals(member.getMemberId())){
            throw new CustomException(HttpStatus.BAD_REQUEST,"자신의 숙소는 예약할 수 없습니다");
        }

        // 5. 날짜 중복 체크
        boolean hasConflict = reservationRepository.existsOverlappingReservation(
                accommodation.getAccommodationId(),
                checkInDate,
                checkOutDate,
                ReservationStatus.RESERVED
        );

        if(hasConflict){
            throw new AccommodationNotAvailableException(
                    accommodation.getAccommodationId(), checkInDate, checkOutDate
            );
        }
        log.info("예약 유효성 검증 통과: accommodationId={}, checkIn={}, checkOut={}",
                accommodation.getAccommodationId(), checkInDate, checkOutDate);
    }

    /**
     * 총 가격 계산
     */
    private Long calculateTotalPrice(Long pricePerNight, LocalDate checkInDate, LocalDate checkOutDate){
        return pricePerNight*ChronoUnit.DAYS.between(checkInDate,checkOutDate);
    }

    /**
     * email로 memberId 조회 (헬퍼 메서드)
     *
     * @param email 회원 이메일
     * @return 회원 ID
     */
    private Long getMemberIdByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email))
                .getMemberId();
    }

    // 게스트용 예약 확인 이메일 메시지 생성
    private EmailMessage buildGuestConfirmationEmail(Reservation reservation){
        return EmailMessage.builder()
                .emailType(EmailMessage.EmailType.RESERVATION_CONFIRMATION)
                .to(reservation.getMember().getEmail())
                .subject("[빈방] 예약이 완료되었습니다 - "+reservation.getAccommodation())
                .reservationId(reservation.getReservationId())
                .accommodationName(reservation.getAccommodation().getName())
                .guestName(reservation.getMember().getName())
                .hostName(reservation.getAccommodation().getMember().getName())
                .checkInDate(formatDate(reservation.getCheckInDate()))
                .checkOutDate(formatDate(reservation.getCheckOutDate()))
                .totalPrice(reservation.getTotalPrice())
                .guestCount(reservation.getPersonnel())
                .build();
    }

    /**
     * 게스트용 예약 확인 알림 메시지 생성
     */
    private NotificationMessage buildGuestConfirmationNotification(Reservation reservation) {
        return NotificationMessage.builder()
                .notificationType(NotificationMessage.NotificationType.RESERVATION_CONFIRMED)
                .memberId(reservation.getMember().getMemberId())
                .title("예약이 완료되었습니다")
                .content(reservation.getAccommodation().getName() + " 예약이 확정되었습니다")
                .reservationId(reservation.getReservationId())
                .accommodationId(reservation.getAccommodation().getAccommodationId())
                .build();
    }

    /**
     * 호스트용 새 예약 알림 이메일 메시지 생성
     */
    private EmailMessage buildHostNewReservationEmail(Reservation reservation) {
        return EmailMessage.builder()
                .emailType(EmailMessage.EmailType.NEW_RESERVATION_NOTIFICATION)
                .to(reservation.getAccommodation().getMember().getEmail())
                .subject("[빈방] 새로운 예약이 접수되었습니다 - " + reservation.getAccommodation().getName())
                .reservationId(reservation.getReservationId())
                .accommodationName(reservation.getAccommodation().getName())
                .guestName(reservation.getMember().getName())
                .hostName(reservation.getAccommodation().getMember().getName())
                .checkInDate(formatDate(reservation.getCheckInDate()))
                .checkOutDate(formatDate(reservation.getCheckOutDate()))
                .totalPrice(reservation.getTotalPrice())
                .guestCount(reservation.getPersonnel())
                .build();
    }

    /**
     * 호스트용 새 예약 알림 메시지 생성
     */
    private NotificationMessage buildHostNewReservationNotification(Reservation reservation) {
        return NotificationMessage.builder()
                .notificationType(NotificationMessage.NotificationType.NEW_RESERVATION)
                .memberId(reservation.getAccommodation().getMember().getMemberId())
                .title("새로운 예약이 접수되었습니다")
                .content(reservation.getMember().getName() + "님이 " +
                        reservation.getAccommodation().getName() + "을 예약했습니다")
                .reservationId(reservation.getReservationId())
                .accommodationId(reservation.getAccommodation().getAccommodationId())
                .build();
    }

    /**
     * 호스트용 예약 취소 이메일 메시지 생성
     */
    private EmailMessage buildHostCancellationEmail(Reservation reservation) {
        return EmailMessage.builder()
                .emailType(EmailMessage.EmailType.CANCELLATION_NOTIFICATION)
                .to(reservation.getAccommodation().getMember().getEmail())
                .subject("[빈방] 예약이 취소되었습니다 - " + reservation.getAccommodation().getName())
                .reservationId(reservation.getReservationId())
                .accommodationName(reservation.getAccommodation().getName())
                .guestName(reservation.getMember().getName())
                .hostName(reservation.getAccommodation().getMember().getName())
                .checkInDate(formatDate(reservation.getCheckInDate()))
                .checkOutDate(formatDate(reservation.getCheckOutDate()))
                .totalPrice(reservation.getTotalPrice())
                .guestCount(reservation.getPersonnel())
                .build();
    }

    /**
     * 호스트용 예약 취소 알림 메시지 생성
     */
    private NotificationMessage buildHostCancellationNotification(Reservation reservation) {
        return NotificationMessage.builder()
                .notificationType(NotificationMessage.NotificationType.RESERVATION_CANCELLED)
                .memberId(reservation.getAccommodation().getMember().getMemberId())
                .title("예약이 취소되었습니다")
                .content(reservation.getMember().getName() + "님이 예약을 취소했습니다")
                .reservationId(reservation.getReservationId())
                .accommodationId(reservation.getAccommodation().getAccommodationId())
                .build();
    }

    /**
     * 게스트용 예약 취소 이메일 메시지 생성
     */
    private EmailMessage buildGuestCancellationEmail(Reservation reservation) {
        return EmailMessage.builder()
                .emailType(EmailMessage.EmailType.CANCELLATION_NOTIFICATION)
                .to(reservation.getMember().getEmail())
                .subject("[빈방] 예약 취소가 완료되었습니다 - " + reservation.getAccommodation().getName())
                .reservationId(reservation.getReservationId())
                .accommodationName(reservation.getAccommodation().getName())
                .guestName(reservation.getMember().getName())
                .hostName(reservation.getAccommodation().getMember().getName())
                .checkInDate(formatDate(reservation.getCheckInDate()))
                .checkOutDate(formatDate(reservation.getCheckOutDate()))
                .totalPrice(reservation.getTotalPrice())
                .guestCount(reservation.getPersonnel())
                .build();
    }

    /**
     * 게스트용 예약 취소 알림 메시지 생성
     */
    private NotificationMessage buildGuestCancellationNotification(Reservation reservation) {
        return NotificationMessage.builder()
                .notificationType(NotificationMessage.NotificationType.RESERVATION_CANCELLED)
                .memberId(reservation.getMember().getMemberId())
                .title("예약 취소가 완료되었습니다")
                .content(reservation.getAccommodation().getName() + " 예약이 취소되었습니다")
                .reservationId(reservation.getReservationId())
                .accommodationId(reservation.getAccommodation().getAccommodationId())
                .build();
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
