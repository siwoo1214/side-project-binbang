import axiosInstance from './axiosInstance';
import type { AccommodationDetail, AccommodationListItem, AccommodationListParams, PageResponse } from '../types/accommodation';

/**
 * 숙소 관련 API 함수
 * 백엔드 AccommodationController.java 기반
 */

// GET /api/accommodation/{id} - 숙소 상세 조회
export const getAccommodationDetail = async (id: number): Promise<AccommodationDetail> => {
  const response = await axiosInstance.get(`/accommodation/${id}`);
  return response.data;
};

// GET /api/accommodation/list - 숙소 목록 조회 (필터링 + 페이징)
export const getAccommodationList = async (
  params: AccommodationListParams = {}
): Promise<PageResponse<AccommodationListItem>> => {
  const response = await axiosInstance.get('/accommodation/list', { params });
  return response.data;
};

// GET /api/accommodation/my - 내가 등록한 숙소 목록 조회 (로그인 필요)
export const getMyAccommodations = async (): Promise<AccommodationListItem[]> => {
  const response = await axiosInstance.get('/accommodation/my');
  return response.data;
};
