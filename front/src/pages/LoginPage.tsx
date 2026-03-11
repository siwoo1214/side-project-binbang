import { useState } from 'react';
import { Link, useNavigate, Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import styles from './LoginPage.module.css';

/**
 * 로그인 페이지
 *
 * [연결된 백엔드 API]
 * POST /api/auth/login
 * body: { email, password }
 * response: { accessToken, refreshToken, tokenType, expiresIn }
 */
export default function LoginPage() {
  const navigate = useNavigate();
  const { login, isLoading, error, isAuthenticated } = useAuth();

  // 이미 로그인된 상태면 홈으로 리다이렉트
  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const [form, setForm] = useState({ email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    // 입력 시 해당 필드 에러 초기화
    if (fieldErrors[name as keyof typeof fieldErrors]) {
      setFieldErrors(prev => ({ ...prev, [name]: undefined }));
    }
  };

  const validate = () => {
    const errors: typeof fieldErrors = {};
    if (!form.email) errors.email = '이메일을 입력해주세요';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errors.email = '이메일 형식이 올바르지 않습니다';
    if (!form.password) errors.password = '비밀번호를 입력해주세요';
    return errors;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }
    try {
      await login(form);
      navigate('/'); // 로그인 성공 → 홈으로
    } catch {
      // 에러는 useAuth의 error 상태로 표시됨
    }
  };

  // OAuth2 소셜 로그인
  // 백엔드 주소를 직접 지정 (프록시 경유 X, 브라우저가 백엔드로 직접 이동)
  const BACKEND_URL = 'http://localhost:8080';

  const handleGoogleLogin = () => {
    window.location.href = `${BACKEND_URL}/oauth2/authorization/google`;
  };

  const handleKakaoLogin = () => {
    window.location.href = `${BACKEND_URL}/oauth2/authorization/kakao`;
  };

  return (
    <div className={styles.container}>
      {/* 왼쪽: 브랜드 패널 */}
      <div className={styles.brandPanel}>
        <div className={styles.brandContent}>
          <Link to="/" className={styles.logo}>빈방</Link>
          <p className={styles.brandTagline}>
            당신의 완벽한<br />
            <em>빈 방</em>을 찾아드려요
          </p>
          <div className={styles.brandDeco} aria-hidden="true">
            <span>🏡</span>
          </div>
        </div>
      </div>

      {/* 오른쪽: 로그인 폼 */}
      <div className={styles.formPanel}>
        <div className={styles.formContainer}>
          <div className={styles.formHeader}>
            <h1 className={styles.formTitle}>다시 오셨군요</h1>
            <p className={styles.formSub}>로그인하고 여행을 시작하세요</p>
          </div>

          {/* 서버 에러 메시지 */}
          {error && (
            <div className={styles.errorBanner} role="alert">
              <span>⚠️</span> {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            {/* 이메일 */}
            <div className={styles.fieldGroup}>
              <label htmlFor="email" className={styles.label}>이메일</label>
              <input
                id="email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="example@email.com"
                className={`${styles.input} ${fieldErrors.email ? styles.inputError : ''}`}
                autoComplete="email"
                autoFocus
              />
              {fieldErrors.email && (
                <span className={styles.fieldError}>{fieldErrors.email}</span>
              )}
            </div>

            {/* 비밀번호 */}
            <div className={styles.fieldGroup}>
              <label htmlFor="password" className={styles.label}>비밀번호</label>
              <input
                id="password"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                placeholder="비밀번호를 입력하세요"
                className={`${styles.input} ${fieldErrors.password ? styles.inputError : ''}`}
                autoComplete="current-password"
              />
              {fieldErrors.password && (
                <span className={styles.fieldError}>{fieldErrors.password}</span>
              )}
            </div>

            {/* 로그인 버튼 */}
            <button
              type="submit"
              className={styles.submitButton}
              disabled={isLoading}
            >
              {isLoading ? (
                <span className={styles.spinner} aria-label="로그인 중" />
              ) : '로그인'}
            </button>
          </form>

          {/* 구분선 */}
          <div className={styles.divider}>
            <span>또는</span>
          </div>

          {/* 소셜 로그인 */}
          <div className={styles.socialButtons}>
            <button
              type="button"
              className={`${styles.socialButton} ${styles.googleButton}`}
              onClick={handleGoogleLogin}
            >
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
                <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.615z" fill="#4285F4"/>
                <path d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" fill="#34A853"/>
                <path d="M3.964 10.707c-.18-.54-.282-1.117-.282-1.707s.102-1.167.282-1.707V4.961H.957C.347 6.175 0 7.55 0 9s.348 2.825.957 4.039l3.007-2.332z" fill="#FBBC05"/>
                <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.961L3.964 7.293C4.672 5.166 6.656 3.58 9 3.58z" fill="#EA4335"/>
              </svg>
              Google로 로그인
            </button>

            <button
              type="button"
              className={`${styles.socialButton} ${styles.kakaoButton}`}
              onClick={handleKakaoLogin}
            >
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
                <path fillRule="evenodd" clipRule="evenodd" d="M9 0.75C4.444 0.75 0.75 3.686 0.75 7.313c0 2.306 1.516 4.326 3.806 5.491l-.972 3.619a.281.281 0 0 0 .431.306L8.1 14.02c.293.03.593.044.9.044 4.556 0 8.25-2.936 8.25-6.563C17.25 3.686 13.556.75 9 .75z" fill="#3B1E08"/>
              </svg>
              카카오로 로그인
            </button>
          </div>

          {/* 회원가입 링크 */}
          <p className={styles.signupPrompt}>
            아직 계정이 없으신가요?{' '}
            <Link to="/signup" className={styles.signupLink}>회원가입</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
