// =============================
// 숙소 관련 타입 (백엔드 DTO 기반)
// =============================

// GET /api/accommodation/list 응답 - 카드 목록용
export interface AccommodationListItem {
  accommodationId: number;
  name: string;
  price: number;
  regionName: string;       // 지역명 (예: 서울, 제주)
  categoryName: string;     // 카테고리명 (예: 펜션, 호텔)
  thumbnailUrl: string | null;  // 대표 이미지 (없으면 null)
}

// Spring Page 응답 래퍼 타입 (백엔드 Page<T> 구조 그대로)
export interface PageResponse<T> {
  content: T[];           // 실제 데이터 목록
  totalElements: number;  // 전체 항목 수
  totalPages: number;     // 전체 페이지 수
  number: number;         // 현재 페이지 번호 (0-based)
  size: number;           // 페이지 크기
  first: boolean;         // 첫 페이지 여부
  last: boolean;          // 마지막 페이지 여부
}

// GET /api/accommodation/{id} 응답 - 상세 페이지용
export interface AccommodationDetail {
  accommodationId: number;
  name: string;
  price: number;
  description: string;
  address: string;
  latitude: number;
  longitude: number;
  checkInTime: string;   // 'HH:mm:ss' 형식
  checkOutTime: string;  // 'HH:mm:ss' 형식
  categoryName: string;
  regionName: string;
  hostName: string;
  imageUrls: string[];   // 이미지 URL 목록 (sortOrder 순)
  // 시설 정보
  bedrooms: number | null;
  bathrooms: number | null;
  beds: number | null;
  petAllowed: boolean;
  parkingAvailable: boolean;
  hasBbq: boolean;
  hasWifi: boolean;
}

// 숙소 목록 조회 쿼리 파라미터
export interface AccommodationListParams {
  categoryId?: number;
  minBedrooms?: number;
  minBathrooms?: number;
  minBeds?: number;
  petAllowed?: boolean;
  parkingAvailable?: boolean;
  hasBbq?: boolean;
  hasWifi?: boolean;
  keyword?: string;
  regionId?: number;
  page?: number;
  size?: number;
}
