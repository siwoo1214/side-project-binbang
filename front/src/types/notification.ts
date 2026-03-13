// =============================
// 알림 관련 타입
// 백엔드 NotificationMessage.java 기반
// =============================

// 백엔드 NotificationType enum과 동일
export type NotificationType =
  | 'NEW_RESERVATION'        // 새 예약 (호스트용)
  | 'RESERVATION_CONFIRMED'  // 예약 확정 (게스트용)
  | 'RESERVATION_CANCELLED'  // 예약 취소
  | 'RESERVATION_COMPLETED'; // 예약 완료

// 백엔드 NotificationMessage 구조와 동일
export interface NotificationMessage {
  notificationType: NotificationType;
  memberId: number;
  title: string;
  content: string;
  reservationId: number;
  accommodationId: number;
  createdAt?: string; // 백엔드에서 LocalDateTime으로 내려옴
}

// 프론트에서 관리하는 알림 항목 (수신 시각 + 읽음 여부 추가)
export interface NotificationItem extends NotificationMessage {
  id: string;         // 프론트에서 생성하는 고유 ID
  receivedAt: string; // 수신 시각 (ISO string)
  isRead: boolean;    // 읽음 여부
}
