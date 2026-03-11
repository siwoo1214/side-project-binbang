// =============================
// 카테고리 / 지역 타입
// =============================

// GET /api/address/search 응답
export interface AddressResponse {
  addressName: string;   // 전체 주소
  latitude: number;      // 위도
  longitude: number;     // 경도
  regionName: string;    // 구/군 이름
}

// GET /api/categories 응답
export interface Category {
  categoryId: number;
  name: string;
}

// GET /api/regions/top, /api/regions/{id}/children 응답
export interface RegionResponse {
  regionId: number;
  name: string;
  depth: number;
  parentName: string | null;
}

// =============================
// 숙소 등록 요청 타입 (백엔드 AccommodationRegisterDto 기반)
// =============================

export interface AccommodationFacilityRequest {
  bedrooms: number;
  bathrooms: number;
  beds: number;
  petAllowed: boolean;
  parkingAvailable: boolean;
  hasBbq: boolean;
  hasWifi: boolean;
}

export interface AccommodationPolicyRequest {
  refundPolicy: string;
  houseRules: string;
  petAllowed: boolean;
  parkingAvailable: boolean;
  maxGuests: number;
  additionalGuestFee: number;
}

export interface AccommodationRegisterRequest {
  name: string;
  price: number;
  description: string;
  address: string;
  latitude: number | null;
  longitude: number | null;
  checkInTime: string;   // "HH:mm:ss" 형식 (LocalTime)
  checkOutTime: string;
  categoryId: number;
  regionName: string;
  facility: AccommodationFacilityRequest;
  policy: AccommodationPolicyRequest;
}
