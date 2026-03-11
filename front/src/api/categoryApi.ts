import axiosInstance from './axiosInstance';
import type { Category, RegionResponse, AccommodationRegisterRequest, AddressResponse } from '../types/category';

// GET /api/categories - 카테고리 전체 조회
export const getCategories = async (): Promise<Category[]> => {
  const response = await axiosInstance.get('/categories');
  return response.data;
};

// GET /api/regions/top - 최상위 지역 조회 (서울, 경기, 제주 등)
export const getTopRegions = async (): Promise<RegionResponse[]> => {
  const response = await axiosInstance.get('/regions/top');
  return response.data;
};

// GET /api/address/search?query=주소 - 주소 검색 (카카오 주소 API)
export const searchAddress = async (query: string): Promise<AddressResponse[]> => {
  const response = await axiosInstance.get('/address/search', { params: { query } });
  return response.data;
};

// GET /api/regions/{id}/children - 하위 지역 조회
export const getChildRegions = async (regionId: number): Promise<RegionResponse[]> => {
  const response = await axiosInstance.get(`/regions/${regionId}/children`);
  return response.data;
};

// POST /api/accommodation/register - 숙소 등록
// 백엔드 AccommodationResponse 반환 (accommodationId 포함)
export const registerAccommodation = async (
  data: AccommodationRegisterRequest
): Promise<{ accommodationId: number; name: string }> => {
  const response = await axiosInstance.post('/accommodation/register', data);
  return response.data;
};

// POST /api/accommodation/{id}/images - 숙소 이미지 업로드
// multipart/form-data 형식으로 전송 (JSON 아님!)
export const uploadAccommodationImages = async (
  accommodationId: number,
  images: File[]
): Promise<void> => {
  const formData = new FormData();
  images.forEach(image => formData.append('images', image));
  await axiosInstance.post(`/accommodation/${accommodationId}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
