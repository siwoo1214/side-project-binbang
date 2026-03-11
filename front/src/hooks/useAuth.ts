import { useState, useCallback } from 'react';
import { login as loginApi, logout as logoutApi, signup as signupApi } from '../api/authApi';
import type { LoginRequest, SignupRequest } from '../types/auth';

/**
 * 인증 상태 관리 훅
 *
 * [관리하는 것]
 * - 로그인 / 로그아웃 / 회원가입 함수
 * - 현재 인증 여부 (localStorage 기반)
 * - 로딩/에러 상태
 */
export const useAuth = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // localStorage에 토큰이 있으면 로그인 상태
  const isAuthenticated = !!localStorage.getItem('accessToken');

  // 로그인
  const login = useCallback(async (data: LoginRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await loginApi(data);
      // 토큰 + memberId 저장
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      if (response.memberId) {
        localStorage.setItem('memberId', String(response.memberId));
      }
      return response;
    } catch (err: any) {
      const message = err.response?.data?.message || '로그인에 실패했습니다';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 로그아웃
  const logout = useCallback(async () => {
    setIsLoading(true);
    try {
      await logoutApi();
    } catch {
      // 서버 에러가 있어도 클라이언트 토큰은 삭제
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('memberId');
      setIsLoading(false);
    }
  }, []);

  // 회원가입
  const signup = useCallback(async (data: SignupRequest) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await signupApi(data);
      return response;
    } catch (err: any) {
      const message = err.response?.data?.message || '회원가입에 실패했습니다';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  return {
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    signup,
  };
};
