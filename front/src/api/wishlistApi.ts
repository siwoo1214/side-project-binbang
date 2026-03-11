import axiosInstance from './axiosInstance';
import type { WishlistItem, WishlistToggleResponse } from '../types/wishlist';

// POST /api/wishlist/toggle - 위시리스트 추가/삭제 토글 (로그인 필요)
export const toggleWishlist = async (
    accommodationId: number
): Promise<WishlistToggleResponse> => {
    const response = await axiosInstance.post('/wishlist/toggle', { accommodationId });
    return response.data;
};

// GET /api/wishlist - 내 위시리스트 목록 조회 (로그인 필요)
export const getMyWishlists = async (): Promise<WishlistItem[]> => {
    const response = await axiosInstance.get('/wishlist');
    return response.data;
};