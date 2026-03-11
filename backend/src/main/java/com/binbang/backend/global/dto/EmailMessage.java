package com.binbang.backend.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 이메일 발송용 메시지 DTO
 * - RabbitMQ를 통해 전송됨
 * - JSON 형태로 직렬화됨
 * - serializable -> 이거 직렬화 가능해요
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {

    // 이메일 유형
    private EmailType emailType;
    // 수신자 이메일
    private String to;
    // 이메일 제목
    private String subject;
    // 예약 ID
    private Long reservationId;
    // 숙소 이름
    private String accommodationName;
    // 게스트 이름
    private String guestName;
    // 호스트 이름
    private String hostName;
    // 체크인 날짜
    private String checkInDate;
    // 체쿠아웃 날짜
    private String checkOutDate;
    // 총 금액
    private Long totalPrice;
    // 투숙 인원
    private Integer guestCount;

    /**
     * 이메일 유형 Enum
     */
    public enum EmailType {
        RESERVATION_CONFIRMATION,    // 게스트 예약 확인
        NEW_RESERVATION_NOTIFICATION, // 호스트 새 예약 알림
        CANCELLATION_NOTIFICATION     // 예약 취소 알림
    }

}
