import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import Header from '../components/common/Header';
import { getChatMessages } from '../api/chatApi';
import axiosInstance from '../api/axiosInstance';
import type { ChatMessage, ChatRoom } from '../types/chat';
import styles from './ChatRoomPage.module.css';

// 날짜 구분선용 포맷
const formatDateLabel = (dateStr: string): string => {
  const date = new Date(dateStr);
  const now = new Date();
  const isToday = date.toDateString() === now.toDateString();
  if (isToday) return '오늘';
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

// 시간 포맷 (오전/오후 HH:mm)
const formatTime = (dateStr: string): string => {
  return new Date(dateStr).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

export default function ChatRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();

  const [room, setRoom] = useState<ChatRoom | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [connected, setConnected] = useState(false);

  const stompRef = useRef<Client | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  // localStorage에서 내 memberId 읽기
  const myMemberId = Number(localStorage.getItem('memberId')) || 0;
  const accessToken = localStorage.getItem('accessToken') || '';

  // ── 초기 데이터 로드 (채팅방 정보 + 이전 메시지) ──
  useEffect(() => {
    if (!roomId) return;
    const init = async () => {
      try {
        const [roomData, msgData] = await Promise.all([
          axiosInstance.get(`/chat/rooms/${roomId}`).then(r => r.data),
          getChatMessages(Number(roomId)),
        ]);
        setRoom(roomData);
        // 서버는 최신순으로 내려오므로 역순으로 표시
        setMessages([...msgData].reverse());
      } catch {
        // 권한 없거나 존재하지 않는 방
        navigate('/chat');
      } finally {
        setLoading(false);
      }
    };
    init();
  }, [roomId, navigate]);

  // ── WebSocket 연결 ──
  useEffect(() => {
    if (!roomId || !accessToken) return;

    const client = new Client({
      // 순수 WebSocket 연결 (백엔드 .withSockJS() 제거됨)
      brokerURL: `${import.meta.env.VITE_WS_URL}/ws`,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true);

        // 내 개인 큐 구독 (백엔드가 /queue/chat/{memberId} 로 전송)
        client.subscribe(`/queue/chat/${myMemberId}`, (frame) => {
          const msg: ChatMessage = JSON.parse(frame.body);
          // 현재 채팅방 메시지만 추가
          if (msg.chatRoomId === Number(roomId)) {
            setMessages(prev => [...prev, msg]);
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
    });

    client.activate();
    stompRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [roomId, accessToken, myMemberId]);

  // ── 새 메시지 오면 자동 스크롤 ──
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // ── 메시지 전송 ──
  const sendMessage = useCallback(() => {
    if (!input.trim() || !stompRef.current?.connected || !roomId) return;

    const payload = {
      chatRoomId: Number(roomId),
      senderId: myMemberId,
      content: input.trim(),
      messageType: 'TEXT',
    };

    // WebSocket으로 전송 → 서버가 저장 후 상대방에게 push
    stompRef.current.publish({
      destination: '/app/chat/send',
      body: JSON.stringify(payload),
    });

    // 내 화면에는 즉시 낙관적 업데이트
    const optimistic: ChatMessage = {
      messageId: Date.now(),  // 임시 ID
      chatRoomId: Number(roomId),
      senderId: myMemberId,
      senderName: '나',
      messageType: 'TEXT',
      content: input.trim(),
      imageUrl: null,
      isRead: false,
      createdAt: new Date().toISOString(),
    };
    setMessages(prev => [...prev, optimistic]);
    setInput('');
  }, [input, roomId, myMemberId]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // ── 날짜 구분선 표시 여부 판단 ──
  const shouldShowDateLabel = (idx: number): boolean => {
    if (idx === 0) return true;
    const cur = new Date(messages[idx].createdAt).toDateString();
    const prev = new Date(messages[idx - 1].createdAt).toDateString();
    return cur !== prev;
  };

  if (loading) return (
    <div className={styles.page}>
      <Header />
      <div className={styles.loadingWrap}>
        <div className={styles.loadingSpinner} />
      </div>
    </div>
  );

  const partnerName = room
    ? (myMemberId === room.hostId ? room.guestName : room.hostName)
    : '채팅';

  return (
    <div className={styles.page}>
      <Header />

      {/* ── 채팅방 헤더 ── */}
      <div className={styles.chatHeader}>
        <button className={styles.backBtn} onClick={() => navigate('/chat')}>←</button>
        <div className={styles.chatHeaderInfo}>
          <span className={styles.chatPartner}>{partnerName}</span>
          {room && <span className={styles.chatAccommodation}>{room.accommodationName}</span>}
        </div>
        <div className={`${styles.connectionDot} ${connected ? styles.dotOnline : styles.dotOffline}`} />
      </div>

      {/* ── 메시지 목록 ── */}
      <div className={styles.messageList}>
        {messages.length === 0 && (
          <div className={styles.emptyChat}>
            <p>💬</p>
            <p>대화를 시작해보세요!</p>
          </div>
        )}

        {messages.map((msg, idx) => {
          const isMine = msg.senderId === myMemberId;
          return (
            <div key={msg.messageId}>
              {/* 날짜 구분선 */}
              {shouldShowDateLabel(idx) && (
                <div className={styles.dateLabel}>
                  <span>{formatDateLabel(msg.createdAt)}</span>
                </div>
              )}

              {/* 메시지 버블 */}
              <div className={`${styles.messageRow} ${isMine ? styles.rowMine : styles.rowOther}`}>
                {!isMine && (
                  <div className={styles.senderAvatar}>
                    {msg.senderName.charAt(0)}
                  </div>
                )}
                <div className={styles.bubbleWrap}>
                  {!isMine && (
                    <span className={styles.senderName}>{msg.senderName}</span>
                  )}
                  <div className={`${styles.bubble} ${isMine ? styles.bubbleMine : styles.bubbleOther}`}>
                    {msg.content}
                  </div>
                  <span className={styles.messageTime}>{formatTime(msg.createdAt)}</span>
                </div>
              </div>
            </div>
          );
        })}

        <div ref={bottomRef} />
      </div>

      {/* ── 입력창 ── */}
      <div className={styles.inputArea}>
        <textarea
          className={styles.input}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="메시지를 입력하세요 (Enter로 전송)"
          rows={1}
          disabled={!connected}
        />
        <button
          className={styles.sendBtn}
          onClick={sendMessage}
          disabled={!connected || !input.trim()}
        >
          전송
        </button>
      </div>

      {!connected && (
        <div className={styles.disconnectedBanner}>
          연결 중... 잠시 후 자동으로 재연결됩니다
        </div>
      )}
    </div>
  );
}
