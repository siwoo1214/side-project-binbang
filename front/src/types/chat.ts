// =============================
// 채팅 관련 타입 (백엔드 DTO 기반)
// =============================

// 채팅방 응답 (GET /api/chat/rooms, GET /api/chat/rooms/{id})
export interface ChatRoom {
  chatRoomId: number;
  reservationId: number;
  accommodationName: string;
  hostId: number;
  hostName: string;
  guestId: number;
  guestName: string;
  lastMessage: string | null;
  lastMessageTime: string | null;
  unreadCount: number;
  createdAt: string;
}

// 메시지 타입
export type MessageType = 'TEXT' | 'IMAGE' | 'SYSTEM';

// 채팅 메시지 응답 (GET /api/chat/rooms/{id}/messages)
export interface ChatMessage {
  messageId: number;
  chatRoomId: number;
  senderId: number;
  senderName: string;
  messageType: MessageType;
  content: string;
  imageUrl: string | null;
  isRead: boolean;
  createdAt: string;
}

// WebSocket으로 보낼 메시지 요청
export interface ChatMessageRequest {
  chatRoomId: number;
  senderId: number;
  content: string;
  messageType: MessageType;
}
