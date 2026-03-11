import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import styles from './Header.module.css';

/**
 * 공통 헤더 컴포넌트
 *
 * [로그인 상태별 메뉴 분기]
 * 비로그인: 로그인 버튼
 * 로그인:   숙소 등록 | 내 예약 | 위시리스트 | 로그아웃
 */
export default function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate('/');
    // 로그아웃 후 페이지 새로고침 (인증 상태 즉시 반영)
    window.location.reload();
  };

  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        {/* 로고 */}
        <Link to="/" className={styles.logo}>빈방</Link>

        {/* 검색바 슬롯 - 필요한 페이지에서 override 가능하도록 비워둠 */}
        <div className={styles.center} />

        {/* 네비게이션 */}
        <nav className={styles.nav}>
          {isAuthenticated ? (
            <>
              <Link to="/accommodations/register" className={styles.registerButton}>
                + 숙소 등록
              </Link>
              <Link to="/reservations/my" className={styles.navLink}>내 예약</Link>
              <Link to="/wishlist" className={styles.navLink}>위시리스트</Link>
              <Link to="/chat" className={styles.navLink}>채팅</Link>
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
      </div>
    </header>
  );
}
