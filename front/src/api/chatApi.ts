import axiosInstance from './axiosInstance';
import type { ChatRoom, ChatMessage } from '../types/chat';

// GET /api/chat/rooms - 내 채팅방 목록
export const getMyChatRooms = async (): Promise<ChatRoom[]> => {
  const response = await axiosInstance.get('/chat/rooms');
  return response.data;
};

// GET /api/chat/reservations/{reservationId}/room - 예약으로 채팅방 생성/조회
export const getOrCreateChatRoom = async (reservationId: number): Promise<ChatRoom> => {
  const response = await axiosInstance.get(`/chat/reservations/${reservationId}/room`);
  return response.data;
};

// GET /api/chat/rooms/{chatRoomId}/messages - 메시지 목록 조회
export const getChatMessages = async (
  chatRoomId: number,
  page = 0,
  size = 50
): Promise<ChatMessage[]> => {
  const response = await axiosInstance.get(`/chat/rooms/${chatRoomId}/messages`, {
    params: { page, size },
  });
  return response.data;
};
