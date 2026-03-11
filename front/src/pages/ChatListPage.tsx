import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/common/Header';
import { getMyChatRooms } from '../api/chatApi';
import type { ChatRoom } from '../types/chat';
import styles from './ChatListPage.module.css';

// 날짜 포맷 (오늘이면 시간, 아니면 날짜)
const formatTime = (dateStr: string | null): string => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const isToday = date.toDateString() === now.toDateString();
  if (isToday) {
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
  }
  return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
};

export default function ChatListPage() {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState<ChatRoom[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 현재 로그인한 사용자 판별용 (memberId를 localStorage에서 읽거나 없으면 null)
  const myMemberId = Number(localStorage.getItem('memberId')) || null;

  useEffect(() => {
    const fetch = async () => {
      try {
        const data = await getMyChatRooms();
        setRooms(data);
      } catch {
        setError('채팅 목록을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  return (
    <div className={styles.page}>
      <Header />

      <main className={styles.main}>
        <div className={styles.container}>
          <h1 className={styles.title}>채팅</h1>

          {loading && (
            <div className={styles.skeletonList}>
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className={styles.skeletonItem}>
                  <div className={styles.skeletonAvatar} />
                  <div className={styles.skeletonContent}>
                    <div className={styles.skeletonLine} style={{ width: '40%' }} />
                    <div className={styles.skeletonLine} style={{ width: '70%' }} />
                  </div>
                </div>
              ))}
            </div>
          )}

          {!loading && error && (
            <div className={styles.empty}>
              <p className={styles.emptyIcon}>⚠️</p>
              <p>{error}</p>
            </div>
          )}

          {!loading && !error && rooms.length === 0 && (
            <div className={styles.empty}>
              <p className={styles.emptyIcon}>💬</p>
              <p className={styles.emptyText}>아직 채팅이 없어요</p>
              <p className={styles.emptySub}>숙소를 예약하면 호스트와 채팅할 수 있어요</p>
            </div>
          )}

          {!loading && !error && rooms.length > 0 && (
            <ul className={styles.list}>
              {rooms.map(room => {
                // 내가 호스트인지 게스트인지에 따라 상대방 이름 결정
                const isHost = myMemberId === room.hostId;
                const partnerName = isHost ? room.guestName : room.hostName;
                const roleLabel = isHost ? '게스트' : '호스트';

                return (
                  <li key={room.chatRoomId}>
                    <button
                      className={styles.roomItem}
                      onClick={() => navigate(`/chat/${room.chatRoomId}`)}
                    >
                      {/* 아바타 */}
                      <div className={styles.avatar}>
                        <span>{partnerName.charAt(0)}</span>
                      </div>

                      {/* 내용 */}
                      <div className={styles.content}>
                        <div className={styles.topRow}>
                          <span className={styles.partnerName}>{partnerName}</span>
                          <span className={styles.roleTag}>{roleLabel}</span>
                          {room.lastMessageTime && (
                            <span className={styles.time}>{formatTime(room.lastMessageTime)}</span>
                          )}
                        </div>
                        <p className={styles.accommodationName}>{room.accommodationName}</p>
                        <p className={styles.lastMessage}>
                          {room.lastMessage ?? '메시지가 없습니다'}
                        </p>
                      </div>

                      {/* 안읽은 메시지 뱃지 */}
                      {room.unreadCount > 0 && (
                        <div className={styles.badge}>{room.unreadCount}</div>
                      )}
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </main>
    </div>
  );
}
