import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * OAuth2 소셜 로그인 리다이렉트 처리 페이지
 *
 * 흐름:
 * 1. 백엔드 OAuth2SuccessHandler가 이 URL로 리다이렉트
 *    → http://localhost:3000/oauth2/redirect?accessToken=...&refreshToken=...&memberId=...
 * 2. 이 페이지에서 URL 쿼리 파라미터에서 토큰을 꺼내 localStorage에 저장
 * 3. 홈으로 이동
 */
export default function OAuth2RedirectPage() {
  const navigate = useNavigate();

  useEffect(() => {
    // URL에서 쿼리 파라미터 추출
    const params = new URLSearchParams(window.location.search);
    const accessToken  = params.get('accessToken');
    const refreshToken = params.get('refreshToken');
    const memberId     = params.get('memberId');

    if (accessToken && refreshToken) {
      // 토큰 저장
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      if (memberId) {
        localStorage.setItem('memberId', memberId);
      }
      // 홈으로 이동
      navigate('/', { replace: true });
    } else {
      // 토큰이 없으면 로그인 페이지로
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  // 리다이렉트 처리 중 잠깐 보이는 화면
  return (
    <div style={{
      height: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      flexDirection: 'column',
      gap: '1rem',
      fontFamily: 'Noto Sans KR, sans-serif',
      color: '#666',
    }}>
      <div style={{
        width: '2rem',
        height: '2rem',
        border: '3px solid #f0ece8',
        borderTop: '3px solid #C8614A',
        borderRadius: '50%',
        animation: 'spin 0.8s linear infinite',
      }} />
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      <p>로그인 처리 중...</p>
    </div>
  );
}
