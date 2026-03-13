import { useState, useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import type { NotificationItem, NotificationMessage } from '../types/notification';

/**
 * 전역 알림 WebSocket 훅
 *
 * [역할]
 * - 로그인 상태일 때 WebSocket 연결
 * - /topic/notifications/{memberId} 구독
 * - 수신된 알림을 state로 관리 (최대 20개)
 *
 * [사용 위치]
 * - Header.tsx (모든 페이지에 공통으로 렌더링됨)
 *
 * [전체 흐름]
 * 게스트가 예약
 *   → ReservationService → MessageProducer → RabbitMQ
 *   → NotificationConsumer → WebSocketService
 *   → /topic/notifications/{호스트memberId}
 *   → 이 훅의 subscribe 콜백 실행
 *   → notifications 상태 업데이트
 *   → Header 뱃지 / 드롭다운 UI 반영
 */
export const useNotification = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const stompRef = useRef<Client | null>(null);

  // localStorage에서 로그인 정보 읽기
  const accessToken = localStorage.getItem('accessToken');
  const memberId = Number(localStorage.getItem('memberId')) || 0;

  // 읽지 않은 알림 개수
  const unreadCount = notifications.filter(n => !n.isRead).length;

  // 새 알림 추가
  const addNotification = useCallback((message: NotificationMessage) => {
    const newItem: NotificationItem = {
      ...message,
      id: String(Date.now()),
      receivedAt: new Date().toISOString(),
      isRead: false,
    };
    // 최신 순으로 앞에 추가, 최대 20개 유지
    setNotifications(prev => [newItem, ...prev].slice(0, 20));
  }, []);

  // 특정 알림 읽음 처리
  const markAsRead = useCallback((id: string) => {
    setNotifications(prev =>
      prev.map(n => n.id === id ? { ...n, isRead: true } : n)
    );
  }, []);

  // 모든 알림 읽음 처리 (드롭다운 열 때 호출)
  const markAllAsRead = useCallback(() => {
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
  }, []);

  // 알림 전체 삭제
  const clearAll = useCallback(() => {
    setNotifications([]);
  }, []);

  // WebSocket 연결 (로그인 상태일 때만)
  useEffect(() => {
    // 비로그인이거나 memberId가 없으면 연결하지 않음
    if (!accessToken || !memberId) return;

    const client = new Client({
      brokerURL: `${import.meta.env.VITE_WS_URL}/ws`,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('[알림] WebSocket 연결 완료, memberId:', memberId);

        // 백엔드 WebSocketService.sendNotification()이 이 경로로 전송
        client.subscribe(`/topic/notifications/${memberId}`, (frame) => {
          try {
            const message: NotificationMessage = JSON.parse(frame.body);
            console.log('[알림] 수신:', message);
            addNotification(message);
          } catch (e) {
            console.error('[알림] 메시지 파싱 실패:', e);
          }
        });
      },
      onDisconnect: () => {
        console.log('[알림] WebSocket 연결 해제');
      },
      onStompError: (frame) => {
        console.error('[알림] STOMP 오류:', frame);
      },
    });

    client.activate();
    stompRef.current = client;

    // 컴포넌트 언마운트 시 연결 해제
    return () => {
      client.deactivate();
      stompRef.current = null;
    };
  }, [accessToken, memberId, addNotification]);

  return {
    notifications,
    unreadCount,
    markAsRead,
    markAllAsRead,
    clearAll,
  };
};
