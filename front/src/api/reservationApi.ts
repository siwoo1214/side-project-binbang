import axiosInstance from './axiosInstance';
import type { ReservationCreateRequest, ReservationResponse, ReservationListResponse} from '../types/reservation';

// POST /api/reservation - 예약 생성 (로그인 필요)
export const createReservation = async (
  data: ReservationCreateRequest
): Promise<ReservationResponse> => {
  const response = await axiosInstance.post('/reservation', data);
  return response.data;
};

// GET /api/reservation/my - 내 예약 목록 조회 (로그인 필요)
// status 파라미터 없으면 전체 조회, 있으면 상태별 조회
export const getMyReservations = async (
  status?: 'RESERVED' | 'CANCELLED' | 'COMPLETED'
): Promise<ReservationListResponse[]> => {
  const response = await axiosInstance.get('/reservation/my', {
    params: status ? { status } : {},
  });
  return response.data;
};

// DELETE /api/reservation/{id} - 예약 취소 (로그인 필요)
export const cancelReservation = async (
  reservationId: number
): Promise<ReservationResponse> => {
  const response = await axiosInstance.delete(`/reservation/${reservationId}`);
  return response.data;
};