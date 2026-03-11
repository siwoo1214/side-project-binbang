// =============================
// 예약 관련 타입 (백엔드 DTO 기반)
// =============================

// POST /api/reservation 요청 바디
export interface ReservationCreateRequest {
  accommodationId: number;
  checkInDate: string;   // 'YYYY-MM-DD' 형식
  checkOutDate: string;  // 'YYYY-MM-DD' 형식
  guestCount: number;
}

// 예약 상태 Enum
export type ReservationStatus = 'RESERVED' | 'CANCELLED' | 'COMPLETED';

// POST /api/reservation, GET /api/reservation/{id} 응답
export interface ReservationResponse {
  reservationId: number;
  checkInDate: string;
  checkOutDate: string;
  personnel: number;
  totalPrice: number;
  nights: number;
  status: ReservationStatus;
  reservedAt: string;
  accommodationId: number;
  accommodationName: string;
  accommodationAddress: string;
  checkInTime: string;
  checkOutTime: string;
  hostName: string;
  hostEmail: string;
  hostPhone: string;
  guestName: string;
  guestEmail: string;
  guestPhone: string;
}

// GET /api/reservation/my 응답 (목록용 간략 버전)
export interface ReservationListResponse {
  reservationId: number;
  checkInDate: string;
  checkOutDate: string;
  personnel: number;
  totalPrice: number;
  status: ReservationStatus;
  reservedAt: string;
  accommodationId: number;
  accommodationName: string;
  hostName: string;
  guestName: string;
}