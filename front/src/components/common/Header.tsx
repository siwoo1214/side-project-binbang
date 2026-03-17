import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../hooks/useNotification';
import type { NotificationItem } from '../../types/notification';
import styles from './Header.module.css';

// 알림 타입별 아이콘
const NOTIFICATION_ICONS: Record<string, string> = {
  NEW_RESERVATION: '🏠',
  RESERVATION_CONFIRMED: '✅',
  RESERVATION_CANCELLED: '❌',
  RESERVATION_COMPLETED: '🎉',
};

// 수신 시각 상대 표시 (몇 분 전, 몇 시간 전)
const formatRelativeTime = (isoString: string): string => {
  const diff = Date.now() - new Date(isoString).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return '방금';
  if (minutes < 60) return `${minutes}분 전`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}시간 전`;
  return `${Math.floor(hours / 24)}일 전`;
};

export default function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();
  const { notifications, unreadCount, markAsRead, markAllAsRead, clearAll } = useNotification();

  // 알림 드롭다운 열림 상태
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // 모바일 사이드 메뉴 열림 상태
  const [menuOpen, setMenuOpen] = useState(false);

  // 드롭다운 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    await logout();
    setMenuOpen(false);
    navigate('/');
    window.location.reload();
  };

  // 모바일 메뉴 닫기
  const closeMenu = () => setMenuOpen(false);

  // 벨 클릭: 드롭다운 토글 + 열 때 모두 읽음 처리
  const handleBellClick = () => {
    setIsOpen(prev => {
      if (!prev) markAllAsRead();
      return !prev;
    });
  };

  // 알림 항목 클릭: 개별 읽음 처리 + 드롭다운 닫기
  const handleNotificationClick = (notification: NotificationItem) => {
    markAsRead(notification.id);
    setIsOpen(false);
  };

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        {/* 로고 */}
        <Link to="/" className={styles.logo}>빈방</Link>

        <div className={styles.center} />

        {/* 네비게이션 */}
        <nav className={styles.nav}>
          {isAuthenticated ? (
            <>
              <Link to="/accommodations/register" className={styles.registerButton}>
                + 숙소 등록
              </Link>
              <Link to="/accommodations/my" className={styles.navLink}>내 숙소</Link>
              <Link to="/reservations/my" className={styles.navLink}>내 예약</Link>
              <Link to="/wishlist" className={styles.navLink}>위시리스트</Link>
              <Link to="/chat" className={styles.navLink}>채팅</Link>

              {/* ── 알림 벨 ── */}
              <div className={styles.notificationWrap} ref={dropdownRef}>
                <button
                  className={styles.bellButton}
                  onClick={handleBellClick}
                  aria-label="알림"
                >
                  🔔
                  {unreadCount > 0 && (
                    <span className={styles.badge}>
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </button>

                {/* ── 알림 드롭다운 ── */}
                {isOpen && (
                  <div className={styles.dropdown}>
                    <div className={styles.dropdownHeader}>
                      <span className={styles.dropdownTitle}>알림</span>
                      {notifications.length > 0 && (
                        <button className={styles.clearButton} onClick={clearAll}>
                          전체 삭제
                        </button>
                      )}
                    </div>

                    <div className={styles.notificationList}>
                      {notifications.length === 0 ? (
                        <div className={styles.emptyNotification}>
                          <span>🔕</span>
                          <p>새로운 알림이 없어요</p>
                        </div>
                      ) : (
                        notifications.map(notification => (
                          <div
                            key={notification.id}
                            className={`${styles.notificationItem} ${
                              !notification.isRead ? styles.notificationUnread : ''
                            }`}
                            onClick={() => handleNotificationClick(notification)}
                          >
                            <span className={styles.notificationIcon}>
                              {NOTIFICATION_ICONS[notification.notificationType] ?? '📬'}
                            </span>
                            <div className={styles.notificationContent}>
                              <p className={styles.notificationTitle}>
                                {notification.title}
                              </p>
                              <p className={styles.notificationText}>
                                {notification.content}
                              </p>
                              <span className={styles.notificationTime}>
                                {formatRelativeTime(notification.receivedAt)}
                              </span>
                            </div>
                            {!notification.isRead && (
                              <span className={styles.unreadDot} />
                            )}
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                )}
              </div>

              <button onClick={handleLogout} className={styles.logoutButton}>
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className={styles.loginButton}>로그인</Link>
              <Link to="/signup" className={styles.signupButton}>회원가입</Link>
            </>
          )}
        </nav>

        {/* ── 햄버거 버튼 (모바일 전용) ── */}
        <button
          className={styles.hamburger}
          onClick={() => setMenuOpen(prev => !prev)}
          aria-label="메뉴 열기"
        >
          {menuOpen ? '✕' : '☰'}
        </button>
      </div>

      {/* ── 모바일 사이드 오버레이 ── */}
      {menuOpen && (
        <div className={styles.overlay} onClick={closeMenu} />
      )}

      {/* ── 모바일 사이드 메뉴 ── */}
      <div className={`${styles.sideMenu} ${menuOpen ? styles.sideMenuOpen : ''}`}>
        <div className={styles.sideMenuHeader}>
          <span className={styles.sideMenuLogo}>빈방</span>
          <button className={styles.sideMenuClose} onClick={closeMenu}>✕</button>
        </div>

        {isAuthenticated ? (
          <nav className={styles.sideMenuNav}>
            <Link to="/accommodations/register" className={styles.sideMenuHighlight} onClick={closeMenu}>
              + 숙소 등록
            </Link>
            <Link to="/accommodations/my" className={styles.sideMenuLink} onClick={closeMenu}>
              🏠 내 숙소
            </Link>
            <Link to="/reservations/my" className={styles.sideMenuLink} onClick={closeMenu}>
              📋 내 예약
            </Link>
            <Link to="/wishlist" className={styles.sideMenuLink} onClick={closeMenu}>
              ❤️ 위시리스트
            </Link>
            <Link to="/chat" className={styles.sideMenuLink} onClick={closeMenu}>
              💬 채팅
            </Link>
            <div className={styles.sideMenuDivider} />
            <button onClick={handleLogout} className={styles.sideMenuLogout}>
              로그아웃
            </button>
          </nav>
        ) : (
          <nav className={styles.sideMenuNav}>
            <Link to="/login" className={styles.sideMenuLink} onClick={closeMenu}>
              로그인
            </Link>
            <Link to="/signup" className={styles.sideMenuHighlight} onClick={closeMenu}>
              회원가입
            </Link>
          </nav>
        )}
      </div>
    </header>
  );
}
