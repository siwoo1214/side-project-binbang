import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import styles from './SignupPage.module.css';

/**
 * 회원가입 페이지
 *
 * [연결된 백엔드 API]
 * POST /api/auth/signup
 * body: { email, password, name, phone }
 * response: { message: "회원가입 성공" }
 *
 * [백엔드 검증 규칙]
 * - password: 영문 + 숫자 + 특수문자 포함
 * - phone: 01X-XXXX-XXXX 형식
 */
export default function SignupPage() {
  const navigate = useNavigate();
  const { signup, isLoading, error } = useAuth();

  const [form, setForm] = useState({
    email: '',
    password: '',
    passwordConfirm: '',
    name: '',
    phone: '',
  });

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [successMsg, setSuccessMsg] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    if (fieldErrors[name]) {
      setFieldErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const errors: Record<string, string> = {};

    if (!form.name.trim()) errors.name = '이름을 입력해주세요';

    if (!form.email) errors.email = '이메일을 입력해주세요';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errors.email = '이메일 형식이 올바르지 않습니다';

    if (!form.password) {
      errors.password = '비밀번호를 입력해주세요';
    } else if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).+$/.test(form.password)) {
      errors.password = '영문, 숫자, 특수문자를 모두 포함해야 합니다';
    } else if (form.password.length < 8) {
      errors.password = '비밀번호는 8자 이상이어야 합니다';
    }

    if (!form.passwordConfirm) errors.passwordConfirm = '비밀번호 확인을 입력해주세요';
    else if (form.password !== form.passwordConfirm) errors.passwordConfirm = '비밀번호가 일치하지 않습니다';

    if (!form.phone) {
      errors.phone = '전화번호를 입력해주세요';
    } else if (!/^01[016789]-?\d{3,4}-?\d{4}$/.test(form.phone)) {
      errors.phone = '올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)';
    }

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
      const { passwordConfirm, ...signupData } = form;
      await signup(signupData);
      setSuccessMsg('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
      setTimeout(() => navigate('/login'), 1500);
    } catch {
      // 에러는 useAuth의 error 상태로 표시
    }
  };

  return (
    <div className={styles.container}>
      {/* 상단 로고 */}
      <div className={styles.topBar}>
        <Link to="/" className={styles.logo}>빈방</Link>
        <Link to="/login" className={styles.loginLink}>로그인</Link>
      </div>

      {/* 중앙 카드 */}
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <h1 className={styles.title}>시작해볼까요</h1>
          <p className={styles.subtitle}>빈방에 오신 걸 환영해요</p>
        </div>

        {/* 서버 에러 */}
        {error && (
          <div className={styles.errorBanner} role="alert">
            <span>⚠️</span> {error}
          </div>
        )}

        {/* 성공 메시지 */}
        {successMsg && (
          <div className={styles.successBanner} role="status">
            <span>✅</span> {successMsg}
          </div>
        )}

        <form onSubmit={handleSubmit} className={styles.form} noValidate>
          {/* 이름 */}
          <div className={styles.fieldGroup}>
            <label htmlFor="name" className={styles.label}>이름</label>
            <input
              id="name"
              name="name"
              type="text"
              value={form.name}
              onChange={handleChange}
              placeholder="홍길동"
              className={`${styles.input} ${fieldErrors.name ? styles.inputError : ''}`}
              autoFocus
            />
            {fieldErrors.name && <span className={styles.fieldError}>{fieldErrors.name}</span>}
          </div>

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
            />
            {fieldErrors.email && <span className={styles.fieldError}>{fieldErrors.email}</span>}
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
              placeholder="영문, 숫자, 특수문자 포함 8자 이상"
              className={`${styles.input} ${fieldErrors.password ? styles.inputError : ''}`}
              autoComplete="new-password"
            />
            {fieldErrors.password && <span className={styles.fieldError}>{fieldErrors.password}</span>}
          </div>

          {/* 비밀번호 확인 */}
          <div className={styles.fieldGroup}>
            <label htmlFor="passwordConfirm" className={styles.label}>비밀번호 확인</label>
            <input
              id="passwordConfirm"
              name="passwordConfirm"
              type="password"
              value={form.passwordConfirm}
              onChange={handleChange}
              placeholder="비밀번호를 다시 입력하세요"
              className={`${styles.input} ${fieldErrors.passwordConfirm ? styles.inputError : ''}`}
              autoComplete="new-password"
            />
            {fieldErrors.passwordConfirm && (
              <span className={styles.fieldError}>{fieldErrors.passwordConfirm}</span>
            )}
          </div>

          {/* 전화번호 */}
          <div className={styles.fieldGroup}>
            <label htmlFor="phone" className={styles.label}>전화번호</label>
            <input
              id="phone"
              name="phone"
              type="tel"
              value={form.phone}
              onChange={handleChange}
              placeholder="010-1234-5678"
              className={`${styles.input} ${fieldErrors.phone ? styles.inputError : ''}`}
              autoComplete="tel"
            />
            {fieldErrors.phone && <span className={styles.fieldError}>{fieldErrors.phone}</span>}
          </div>

          <button
            type="submit"
            className={styles.submitButton}
            disabled={isLoading}
          >
            {isLoading ? (
              <span className={styles.spinner} aria-label="처리 중" />
            ) : '가입하기'}
          </button>
        </form>

        <p className={styles.loginPrompt}>
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className={styles.loginLink2}>로그인</Link>
        </p>
      </div>
    </div>
  );
}
