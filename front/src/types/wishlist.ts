// =============================
// 위시리스트 관련 타입 (백엔드 DTO 기반)
// =============================

// POST /api/wishlist/toggle 응답
// 백엔드: WishlistToggleResponse.java
export interface WishlistToggleResponse {
    isWishlisted: boolean; // true: 추가됨, false: 삭제됨
    message: string;       // ex) "제주 펜션이(가) 위시리스트에 추가되었습니다"
}

// GET /api/wishlist 응답 (배열로 반환됨)
// 백엔드: WishlistResponse.java
export interface WishlistItem {
    listId: number;
    accommodationId: number;
    accommodationName: string;
    price: number;
    address: string;
    categoryName: string;
    checkInTime: string;  // 'HH:mm:ss' 형식 (백엔드 LocalTime → JSON string)
    checkOutTime: string; // 'HH:mm:ss' 형식
}